package com.gk.simpleworkoutjournal;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gk.datacontrol.DBClass;
import com.gk.datacontrol.ExerciseDataCursorLoader;
import com.gk.datacontrol.SetDataCursorLoader;

import static com.gk.simpleworkoutjournal.WorkoutDataAdapter.*;
import static com.gk.simpleworkoutjournal.WorkoutDataAdapter.APP_NAME;

public class WorkoutJournal extends Activity implements  OnItemClickListener, OnTouchListener, LoaderManager.LoaderCallbacks<Cursor> {
    public enum TriggerEvent { NONE, INIT, ADD, DELETE, CLICK, NOTEADD };
    public static final String APP_NAME = "SWJournal";

    public static final int EXERCISES = 0;
    public static final int SETS = 1;

    TriggerEvent setsUpTrigger;
    TriggerEvent exUpTrigger;

    LinearLayout notesLayout;

    AutoCompleteTextView exerciseTextView;
    EditText repsEdit, weightEdit;

    ListView currLv, exercisesLv, setsLv;
    TextView currNoteTv, exerciseNoteTv, setNoteTv;
    Cursor currCursor, allExCursor, allSetsCursor;
    WorkoutDataAdapter currAdapter, exerciseLogAdapter, setsLogAdapter;

    ImageButton switchBtn;
    boolean inContextMode;

    WJContext exercisesContextualMode;
    WJContext setsContextualMode;

    boolean notesShowed = false;
    DBClass dbmediator;

    SetDataCursorLoader setsListDataLoader;
    ExerciseDataCursorLoader exListDataLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_journal);

        // fetch all required UI items
        notesLayout = (LinearLayout) findViewById(R.id.notesLayout);

        setsLv = (ListView) findViewById(R.id.setsLv);
        setNoteTv = (TextView) findViewById(R.id.setNoteTv);

        exercisesLv = (ListView) findViewById(R.id.exercisesLv);
        exerciseNoteTv = (TextView) findViewById(R.id.exerciseNoteTv);

        exerciseTextView = (AutoCompleteTextView) findViewById(R.id.addExerciseACTV);

        repsEdit = (EditText) findViewById(R.id.editReps);
        weightEdit = (EditText) findViewById(R.id.editWeight);

        switchBtn = (ImageButton) findViewById(R.id.CancelBtn);

        // set notes touch listeners for exercise and set
        exerciseNoteTv.setOnTouchListener(this);

        // set click / touch listeners
        setsLv.setOnItemClickListener(this);
        exercisesLv.setOnItemClickListener(this);

        setsLv.setOnTouchListener(this);
        exercisesLv.setOnTouchListener(this);

        Log.d("WorkoutJournal", "onCreate :: creating adapter for exercises from db");
        dbmediator = new DBClass(this);
        dbmediator.open();
        //allExCursor = dbmediator.fetchExerciseHistory();

        exerciseLogAdapter = new WorkoutDataAdapter(this, allExCursor, WorkoutDataAdapter.Subject.EXERCISES);

        //fill the text view now
        exercisesLv.setAdapter(exerciseLogAdapter);

        //TODO: show appropriate sets
        exercisesContextualMode = new WJContext(this, Subject.EXERCISES);
        setsContextualMode = new WJContext(this, Subject.SETS);

        exercisesLv.setMultiChoiceModeListener( exercisesContextualMode );
        setsLv.setMultiChoiceModeListener( setsContextualMode );
        inContextMode = false;
        Log.d("WorkoutJournal", "INIT");
        initiateListUpdate( Subject.ALL, TriggerEvent.INIT );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(APP_NAME, "WorkoutJournal :: onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.workout_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(APP_NAME, "WorkoutJournal :: onOptionsItemSelected " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_moreinfo_icon:
                if (notesShowed) {
                    notesLayout.setVisibility(View.GONE);
                    notesShowed = false;
                } else {
                    notesLayout.setVisibility(View.VISIBLE);
                    notesShowed = true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        dbmediator.close();
        super.onDestroy();
    }

    /*
     * Contains logic for initiating updates of lists
     */
    public void initiateListUpdate( Subject trigSubj, TriggerEvent trigEvent ) {
        Log.v(APP_NAME, "WorkoutJournal :: initiateListUpdate subj: " + trigSubj.toString() + " caused by: " + trigEvent.toString());
        int subject = -1;

        switch ( trigSubj ) {
            case EXERCISES:
                exUpTrigger = trigEvent;
                subject = EXERCISES;
                break;

            case SETS:
                setsUpTrigger = trigEvent;
                subject = SETS;
                break;

            case ALL:
                exUpTrigger = trigEvent;
                setsUpTrigger = trigEvent;
                break;
        }

        if ( trigEvent == TriggerEvent.NOTEADD && trigSubj != Subject.EXERCISES && trigSubj != Subject.SETS ) {
            Log.v( APP_NAME, "WorkoutJournal :: initiateListUpdate : incoming parameters are messed" );
            return;
        }

        switch ( trigEvent ) {
            case INIT:
                getLoaderManager().initLoader( EXERCISES, null, this);
                getLoaderManager().initLoader( SETS, null, this);
                break;

            case CLICK:
                //no need to do anything with exercises, but should renew sets
                if ( trigSubj == Subject.SETS ) {
                    getLoaderManager().getLoader( SETS ).forceLoad();
                }
                break;

            case NOTEADD:
                getLoaderManager().getLoader( subject ).forceLoad();
                break;

            case ADD: // ex added - should renew both since focus changed. set added - only set lv to update
                if ( trigSubj == Subject.EXERCISES ) {
                    getLoaderManager().getLoader( EXERCISES ).forceLoad();

                    //set list behavior for add is the same as for click
                    setsUpTrigger = TriggerEvent.CLICK;
                    getLoaderManager().getLoader(SETS).forceLoad();
                }
                if ( trigSubj == Subject.SETS ) {
                    getLoaderManager().getLoader(SETS).forceLoad();
                }
                break;

            case DELETE: // set update may be not required if deleted ex is not current
                if ( trigSubj == Subject.EXERCISES ) {
                    getLoaderManager().getLoader( EXERCISES ).forceLoad();
                }
                if ( trigSubj == Subject.SETS ) {
                    getLoaderManager().getLoader(SETS).forceLoad();
                }

                break;
        }
    }

    public void onBackButtonPressed(View v) {

        Log.v(APP_NAME, "WorkoutJournal :: onBackButtonPressed()");
        if (exerciseTextView.getVisibility() == View.GONE) {
            exerciseTextView.setVisibility(View.VISIBLE);
            repsEdit.setVisibility(View.GONE);
            weightEdit.setVisibility(View.GONE);
            switchBtn.setImageResource(R.drawable.ic_custom_circledforward);

        } else if (exerciseLogAdapter.getIdxOfCurrent() != -1) {
            exerciseTextView.setVisibility(View.GONE);
            repsEdit.setVisibility(View.VISIBLE);
            weightEdit.setVisibility(View.VISIBLE);
            switchBtn.setImageResource(R.drawable.ic_custom_circledback);
        }
    }

    /*
     * In exercise edit mode: check if exercise text field is not empty 
     * and log exrcise. I exercise not exist in db -
     * add exercise to db
     * scroll listview  to last item
     * ans set is as checked
     */
    public void onAddButtonPressed(View v) {
        //we are trying  both to log exercise to log DB and to add it into exercise DB and list view

        if (exerciseTextView.getVisibility() == View.VISIBLE) {

            String incomingName = exerciseTextView.getText().toString();
            incomingName = incomingName.trim();

            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). Exercise in edit text: " + incomingName );

            if (incomingName.length() == 0) {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
                return;
            }

            dbmediator.addExercise(incomingName); // may fail since exercise is in db - it's ok
            if (dbmediator.logExercise(incomingName)) {
                //populate list view with renewed data
                exerciseLogAdapter.setIdxOfCurrent(exerciseLogAdapter.getCount()); //no need to decrement since item is not renewed in the list yet: count will be larger
                setsListDataLoader.renewTargetEx( incomingName );
                initiateListUpdate( Subject.EXERCISES, TriggerEvent.ADD );
            }

            //we are trying to add reps and weight
        } else {

            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed() Reps: " + repsEdit.getText() + " Weight: " + weightEdit.getText() + " Curr ex idx: " + exerciseLogAdapter.getIdxOfCurrent());
            String repString = repsEdit.getText().toString();
            String weiString = weightEdit.getText().toString();

            setsLogAdapter.setIdxOfCurrent(setsLogAdapter.getCount());  //no need to decrement since item is not renewed in the list yet: count will be larger

            if (repString.trim().length() == 0 || weiString.trim().length() == 0) {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
                return;
            }

            int newRep = Integer.parseInt(repString);
            Float newWei = Float.parseFloat(weiString);

            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). exercisesLv.getCheckedItemPosition() " + exercisesLv.getCheckedItemPosition() +
                    " allExCursor.getColumnIndex(DBClass.KEY_ID) " + exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_ID));

            //get entry for the current exercise
            Cursor tmpcs = (Cursor) exerciseLogAdapter.getItem(exerciseLogAdapter.getIdxOfCurrent());
            String exerciseName = tmpcs.getString(exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_EX_NAME));
            Long exerciseLogId = tmpcs.getLong(exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_ID));

            dbmediator.insertSet( exerciseName, exerciseLogId, newRep, newWei);

            //refresh cursor
            initiateListUpdate(Subject.SETS, TriggerEvent.ADD);

        }

        //set note (possible only for exercise!)
    }

    /*
     * 
     * 
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String noNoteHint = "";
        switch (view.getId()) {
            case R.id.exercise_entry_container:
                repsEdit.setText("");
                weightEdit.setText("");

                currLv = exercisesLv;
                currCursor = allExCursor;
                currNoteTv = exerciseNoteTv;
                currAdapter = exerciseLogAdapter;

                // obtain sets for this exercise
                // fetch new sets only if exercise entry changed
                if (exerciseLogAdapter.getIdxOfCurrent() != position) {

                    exerciseLogAdapter.setIdxOfCurrent(position);

                    exerciseLogAdapter.getCursor().moveToPosition( position );
                    String noteString = exerciseLogAdapter.getCursor().getString( exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_NOTE) );
                    if ( noteString == null || noteString.isEmpty()) {
                        exerciseNoteTv.setHint( getString(R.string.workout_exercise_newnote_hint) );
                        exerciseNoteTv.setText( "" );
                    } else {
                        exerciseNoteTv.setText( noteString );
                    }

                    //empty hint box for set since we have chosen other exercise
                    setNoteTv.setHint(getString(R.string.workout_set_no_note_hint));
                    setNoteTv.setText("");

                    //need to update sets according to new item
                    setsListDataLoader.renewTargetEx(exerciseLogAdapter.getCursor());
                    initiateListUpdate( Subject.SETS, TriggerEvent.CLICK );

                    exerciseLogAdapter.notifyDataSetChanged();

                    if (setsLv.getCount() != 0) {
                        int pos = syncPositionsBasedOnDate(exercisesLv, setsLv);
                        //setsLv.setSelection( pos ); // jump to last item
                        View v = setsLv.getChildAt(pos);
                        if (v != null) {
                            v.requestFocus();
                        }
                    }

                }

                exerciseTextView.setVisibility(View.GONE);
                repsEdit.setVisibility(View.VISIBLE);
                weightEdit.setVisibility(View.VISIBLE);
                switchBtn.setImageResource(R.drawable.ic_custom_circledback);
                break;
            case R.id.set_entry_container:
                exerciseTextView.setText("");

                currLv = setsLv;
                currCursor = allSetsCursor;
                currNoteTv = setNoteTv;
                currAdapter = setsLogAdapter;

                setsLogAdapter.setIdxOfCurrent(position);
                Cursor tmpcs;
                tmpcs = setsLogAdapter.getCursor();
                tmpcs.moveToPosition(position);
                //same code for exs and sets
                String noteSet =  tmpcs.getString( tmpcs.getColumnIndex(DBClass.KEY_NOTE));
                Log.v(APP_NAME, "WorkoutJournal :: onItemClick :: note : " + noteSet);
                if (noteSet == null || noteSet.isEmpty() ) {
                    setNoteTv.setHint( getString(R.string.workout_set_newnote_hint) );
                    setNoteTv.setText( "" );
                } else {
                    setNoteTv.setText(noteSet);
                }

                // show required exercise for selected date
                //if (setsLv.getCount() != 0 )  { no need to check count in exercises since it always must be  filled
                //PROBLEM possibly set if is not set at that moment
                setsLogAdapter.notifyDataSetChanged();

                int pos = syncPositionsBasedOnDate(setsLv, exercisesLv);
                //exercisesLv.setSelection( pos );// jump to last item
                View v = exercisesLv.getChildAt(pos);
                if (v != null) {
                    v.requestFocus();
                }

                break;
        }

        //remove line below
        Log.v(APP_NAME, "WorkoutJournal :: onItemClick , click position: " + position + " selected: " + currLv.getSelectedItemPosition() + " checked: " + exercisesLv.getCheckedItemPosition() + " current: " + currAdapter.getIdxOfCurrent());
        Log.v(APP_NAME, "WorkoutJournal :: onItemClick new current: " + currAdapter.getIdxOfCurrent() + " have note: " + 1);
    }

    /*
	 * Case if reps list view is touched - change edit panel for reps.
	 * otherwise do nothing (we will return to exercise mode of panel only when appropriate button is pressed
	 * 
	 */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //switch lower pane edits depending on touch target ( exercise list view or sets list view)
        if (event.getAction() == MotionEvent.ACTION_UP) {

            switch (view.getId()) {
                case R.id.setsLv:
                    if (exerciseLogAdapter.getIdxOfCurrent() != -1) {
                        exerciseTextView.setVisibility(View.GONE);
                        repsEdit.setVisibility(View.VISIBLE);
                        weightEdit.setVisibility(View.VISIBLE);
                        switchBtn.setImageResource(R.drawable.ic_custom_circledback);
                    }
                    break;

                case R.id.exercisesLv:

                    break;
            }
        }

        return false;
    }

    /*
	 * Get position of list view based on the time stamp of checked item in other list view.
	 */
    public int syncPositionsBasedOnDate(ListView primaryLv, ListView secondaryLv) {
        // move cursor to this position  in order to get values from there
        int srcPosition = primaryLv.getCheckedItemPosition();
        WorkoutDataAdapter primaryAdapter = (WorkoutDataAdapter) primaryLv.getAdapter();
        Cursor srcCursor = primaryAdapter.getCursor();

        WorkoutDataAdapter secondaryAdapter = (WorkoutDataAdapter) secondaryLv.getAdapter();
        Cursor dstCursor = secondaryAdapter.getCursor();

        //srcCursor.moveToPosition(srcPosition);
        Log.v(APP_NAME, "syncPositions: current src-dst pos (" + primaryAdapter.getIdxOfCurrent() + "-" + secondaryAdapter.getIdxOfCurrent() + ")");
        srcCursor.moveToPosition(primaryAdapter.getIdxOfCurrent());

        long baseDate = srcCursor.getLong(srcCursor.getColumnIndex(DBClass.KEY_TIME));   // and get  time
        long curDate; // =  dstCursor.getColumnIndex(DBClass.KEY_TIME);
        boolean found = false;
        int max = secondaryAdapter.getCount();
        int min = 0;
        int timeColumnIdx = dstCursor.getColumnIndex(DBClass.KEY_TIME);
        while (!found) {
            dstCursor.moveToPosition((min + max) / 2);
            curDate = dstCursor.getLong(timeColumnIdx);
            Log.v(APP_NAME, "syncPositions: search source date " + baseDate + " in dst cursor (range [" + min + "-" + max + "]). Current dst: " + dstCursor.getPosition() + " with date " + curDate);

            switch (setsLogAdapter.compareDates(curDate, baseDate)) {
                case 1:
                    max = dstCursor.getPosition();
                    break;
                case -1:
                    min = dstCursor.getPosition();
                    break;
                case 0:
                    found = true;
                    break;
            }
            // no reason to search if only few items left
            if ((max - min) < 3) break;
        }
        //if (min == setCur.getPosition()) return setCur.getCount()-1; // take last position
        Log.v(APP_NAME, "WorkoutJournal :: syncPositions : set to " + dstCursor.getPosition());
        return dstCursor.getPosition();
    }

    /*
    * will change current adapter to appropriate one
     */
    public void onNotesTap(View v) {
        Log.v(APP_NAME, "tapped notes");
        boolean haveRecepient = false;
        String headText = null;
        int isExercise = 0;
        Cursor noteParent = null;
        String targetId;

        switch (v.getId()) {
            case R.id.exerciseNoteTv:
                haveRecepient = true;
                currLv = exercisesLv;

                currAdapter = exerciseLogAdapter;
                noteParent = (Cursor) exerciseLogAdapter.getItem(exerciseLogAdapter.getIdxOfCurrent()); //check.

                Log.v(APP_NAME, "tapped notes: exercise section. current item: " + exerciseLogAdapter.getIdxOfCurrent());
                if (exerciseLogAdapter.getIdxOfCurrent() == -1) {

                    Log.v(APP_NAME, "tapped notes: doing nothing since no item selected");
                    return;
                }
                Log.v(APP_NAME, "tapped notes: exercise section. current item: " + exerciseLogAdapter.getIdxOfCurrent());

                headText = noteParent.getString(noteParent.getColumnIndex(DBClass.KEY_EX_NAME));
                targetId = noteParent.getString(noteParent.getColumnIndex(DBClass.KEY_EX_NAME));

                isExercise = 1;
                break;
            case R.id.setNoteTv:
                haveRecepient = true;
                currLv = setsLv;
                currAdapter = setsLogAdapter;
                noteParent = (Cursor) setsLogAdapter.getItem(setsLogAdapter.getIdxOfCurrent()); //check.

                //if no sets
                if (setsLogAdapter == null) {
                    Log.v(APP_NAME, "tapped notes: doing nothing since adapter dont exist");
                    return;
                }

                if (setsLogAdapter.getIdxOfCurrent() == -1) {
                    Log.v(APP_NAME, "tapped notes: doing nothing since no item selected");
                    return;
                }
                Log.v(APP_NAME, "tapped notes: sets section. current item: " + setsLogAdapter.getIdxOfCurrent());

                //if ( ((Cursor) setsLogAdapter.getItem(setsLogAdapter.getIdxOfCurrent())).getCount() == 0)
                //    return;// here need other check for case when no set selected (when navogating after exercise lv tapped)
                targetId=  noteParent.getString(noteParent.getColumnIndex(DBClass.KEY_ID));

                headText = exerciseLogAdapter.getCursor().getString( exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_EX_NAME)) + "  " +
                        setsLogAdapter.getCursor().getInt(setsLogAdapter.getCursor().getColumnIndex(DBClass.KEY_REPS)) + " : " +
                        setsLogAdapter.getCursor().getFloat(setsLogAdapter.getCursor().getColumnIndex(DBClass.KEY_WEIGHT));
                break;
            default:
                return;
        }

        if (haveRecepient && !(noteParent == null) && noteParent.getCount() != 0) {
            noteParent.moveToPosition(currAdapter.getIdxOfCurrent()); //need this? arent we get cursor with 1 item only?
            String note =  noteParent.getString(noteParent.getColumnIndex(DBClass.KEY_NOTE));
            Log.v(APP_NAME, " note: " + note);

            Intent dialogIntent = new Intent(this, NotesDialog.class);
            dialogIntent.putExtra("targetId", targetId);
            dialogIntent.putExtra("headText", headText);
            dialogIntent.putExtra("note", note);
            // id is exercise name or sets id

            startActivityForResult(dialogIntent, isExercise);
            Log.v(APP_NAME, "after  startActivity");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(APP_NAME, "onActivityResult");

        if (resultCode == RESULT_OK) {
            //currCursor.moveToPosition(currAdapter.getIdxOfCurrent());
            String note = data.getStringExtra("note");
            String targetId = data.getStringExtra("targetId");

            Log.v(APP_NAME, "onActivityResult once got item");

            switch (requestCode) {

                case 1: //exercise
                    Log.v(APP_NAME, "gonna insert note '" + note + "' for ex  " + exerciseLogAdapter.getCursor().getString(exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_EX_NAME)));
                    dbmediator.insertExerciseNote( targetId, note);
                    exerciseNoteTv.setText(note);

                    initiateListUpdate( Subject.EXERCISES, TriggerEvent.NOTEADD );

                    Log.v(APP_NAME, "onActivityResult once fetched exercise history after inserting note");

                    break;

                case 0: //set

                    dbmediator.insertSetNote( targetId, note);
                    setNoteTv.setText(note);

                    initiateListUpdate(Subject.SETS, TriggerEvent.NOTEADD);

                    break;
            }
            //currAdapter.changeCursor( currCursor );
            //currAdapter.notifyDataSetChanged();
        }
    }

    public void onContextButtonPressed(View contextualActionButton) {
        Log.v(APP_NAME, "WorkoutJournal :: onContextButtonPressed");

        WJContext cmcb;
        switch (currAdapter.getSubject()) {
            case EXERCISES:
                cmcb = exercisesContextualMode;
                break;

            case SETS:
                cmcb = setsContextualMode;
                break;

            default:
                Log.e(APP_NAME, "WorkoutJournal :: onContextButtonPressed. Weird state met");
                return;
        }

        switch (contextualActionButton.getId()) {
            case R.id.context_action_delete_ex:
                cmcb.onDeleteExPressed();
                break;

            case R.id.context_action_rename_edit_single:
                cmcb.onEditRenamePressed();
                break;

            case R.id.ctx_deleteLogEntriesBtn:
                cmcb.onDeleteLogEntriesPressed();
                break;

            case R.id.ctx_cancelBtn:
                cmcb.onCancelEditBtnPressed();
                break;

            case R.id.ctx_addEditedBtn:
                cmcb.onAddEditedBtnPressed();
                break;

            default:
                Log.e(APP_NAME, "WorkoutJournal :: onContextButtonPressed. handler for passed view is missing");
                return;
        }
    }

    /*
    @return 3 if no items left to operate on
            2 if idx of checked must be decremented.
            1
            0 if no extra actions required
     */
    public int adjustAfterExDeleted( int idxOfDeleted ) {
        Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted");
        int retCode = 0;

        // remember - these are values before adjustment - DB is already changed!
        int sumOfElements = exercisesLv.getCount();

        //if it was the only left element - need to report to handle checked items.
        if ( sumOfElements <= 1 ) {
            Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of very last item. Sum of items before deletion: "+sumOfElements+" deleted item idx: "+idxOfDeleted);
            exerciseLogAdapter.setIdxOfCurrent(-1);
            retCode = 3;
        }
        //if deleted item was last in a row - need to decrement current
        else if ( idxOfDeleted == sumOfElements-1 ) {
            Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of last in a row item.");

            exerciseLogAdapter.setIdxOfCurrent(idxOfDeleted - 1);
            retCode = 2;
        }
        else if ( idxOfDeleted == 0 ) {
            Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of first in a row item.");

            //selected is still the same. However, it's position may be changed now if one of prev entries was deleted or selected was last item in the list
            if ( idxOfDeleted < exerciseLogAdapter.getIdxOfCurrent() || exerciseLogAdapter.getIdxOfCurrent() == sumOfElements-1 ) {
                exerciseLogAdapter.setIdxOfCurrent(exerciseLogAdapter.getIdxOfCurrent() - 1);
            }

            retCode = 1;
        }

        //show renewed data for exercises
        initiateListUpdate( Subject.EXERCISES, TriggerEvent.DELETE );

        //make sure exercise edit is active
        exerciseTextView.setVisibility(View.VISIBLE);
        repsEdit.setVisibility(View.GONE);
        weightEdit.setVisibility(View.GONE);
        switchBtn.setImageResource(R.drawable.ic_custom_circledback);

        // obtain sets for new exercise
        // fetch new sets only if exercise entry changed

        //empty hint box for set since we have chosen other exercise
        setNoteTv.setHint(getString(R.string.workout_set_no_note_hint));
        setNoteTv.setText("");

        //no sets if no exercises
        if ( retCode == 3 )
        {
            setsLv.setAdapter(null);
            return retCode;
        }

        //update sets list view accordingly
        initiateListUpdate( Subject.SETS, TriggerEvent.DELETE );

        if (setsLv.getCount() != 0) {
            int pos = syncPositionsBasedOnDate(exercisesLv, setsLv);
            //setsLv.setSelection( pos ); // jump to last item
            View v = setsLv.getChildAt(pos);
            if (v != null) {
                v.requestFocus();
            }
        }

        return retCode;
    }

    public void moveToSelected( Subject subj, Cursor data, boolean needToScroll ) {
        TextView targetNoteView;
        WorkoutDataAdapter targetAdapter;
        ListView targetLv;
        String newNoteHint;


        switch ( subj ) {
            case EXERCISES:
                targetAdapter = exerciseLogAdapter;
                targetNoteView = exerciseNoteTv;
                targetLv = exercisesLv;

                newNoteHint = getString(R.string.workout_exercise_newnote_hint);

                //empty sets note since new exercise was selected
                //but what if new setes adapter have note? TODO
                setNoteTv.setText("");
                setNoteTv.setHint( R.string.workout_set_no_note_hint );
                break;

            case SETS:
                targetAdapter = setsLogAdapter;
                targetNoteView = setNoteTv;
                targetLv = setsLv;

                newNoteHint = getString(R.string.workout_set_newnote_hint);

                break;

            default:
                Log.e(APP_NAME,"renewNote :: unknown target subject :: subj: "+subj);
                return;
        }

        data.moveToPosition( targetAdapter.getIdxOfCurrent() );
        String noteString = data.getString( data.getColumnIndex(DBClass.KEY_NOTE) );
        if ( noteString == null || noteString.isEmpty()) {
            targetNoteView.setHint( newNoteHint );
            targetNoteView.setText("");
        } else {
            targetNoteView.setText( noteString );
        }

        if ( needToScroll ) {
            targetLv.smoothScrollToPosition( targetAdapter.getIdxOfCurrent() );
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(APP_NAME, "onCreateLoader :: Id " + id );
        if ( id == EXERCISES ) {
            return exListDataLoader = new ExerciseDataCursorLoader(this, dbmediator );
        } else {
            return setsListDataLoader = new SetDataCursorLoader(this, dbmediator, exerciseLogAdapter.getCursor() );
        }

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(APP_NAME, "WorkoutJournal :: onLoadFinished : id " + loader.getId() + " trigger: "+  ( ( loader.getId() == 0 ) ? exUpTrigger.toString() : setsUpTrigger.toString()) );


        if ( ( loader.getId() == EXERCISES && exUpTrigger == TriggerEvent.NONE ) || ( loader.getId() == SETS &&  setsUpTrigger == TriggerEvent.NONE ) ) {
            Log.e(APP_NAME, "WorkoutJournal :: onLoadFinished : cannot update since trigger event is not set" );
            return;
        }

        switch ( loader.getId() ) {
            case EXERCISES:
                exerciseLogAdapter.swapCursor( data );

                data.moveToPosition( exerciseLogAdapter.getIdxOfCurrent() );
                Log.v(APP_NAME, "exerciseLogAdapter.getIdxOfCurrent() "+exerciseLogAdapter.getIdxOfCurrent());
                //data.moveToFirst();

                //always empty notes box for sets since we lost focus from sets. Added: only when selected was changed/prev changed deleted
                setNoteTv.setText("");
                setNoteTv.setHint( R.string.workout_set_no_note_hint );

                // if add button clicked
                if ( exUpTrigger == TriggerEvent.ADD ) {

                    exerciseLogAdapter.getCursor().moveToPosition( exerciseLogAdapter.getIdxOfCurrent() );


                    DatabaseUtils.dumpCursor(data);
                    String noteString = data.getString( data.getColumnIndex(DBClass.KEY_NOTE) );
                    if ( noteString == null || noteString.isEmpty()) {
                        exerciseNoteTv.setHint( getString(R.string.workout_exercise_newnote_hint) );
                        exerciseNoteTv.setText("");
                    } else {
                        exerciseNoteTv.setText( noteString );
                    }

                    exercisesLv.smoothScrollToPosition( exerciseLogAdapter.getIdxOfCurrent() );
                }

                setsListDataLoader.renewTargetEx(exerciseLogAdapter.getCursor());
                exUpTrigger = TriggerEvent.NONE;
                break;

            case SETS:

                if ( data != null ) {

                    int current = 0;
                    boolean needPassCurrent = false;
                    if ( setsLogAdapter != null )  {
                        current = setsLogAdapter.getIdxOfCurrent();
                        needPassCurrent = true;
                    }

                    setsLv.setAdapter( setsLogAdapter = new WorkoutDataAdapter(this, data, WorkoutDataAdapter.Subject.SETS) );
                    if ( needPassCurrent ) setsLogAdapter.setIdxOfCurrent(current);

                    if ( setsUpTrigger == TriggerEvent.ADD ) {

                        setsLogAdapter.getCursor().moveToPosition( setsLogAdapter.getIdxOfCurrent() );


                        String noteString = data.getString( data.getColumnIndex(DBClass.KEY_NOTE) );
                        if ( noteString == null || noteString.isEmpty()) {
                            setNoteTv.setHint( getString(R.string.workout_exercise_newnote_hint) );
                            setNoteTv.setText("");
                        } else {
                            setNoteTv.setText( noteString );
                        }

                        setsLv.smoothScrollToPosition( setsLogAdapter.getIdxOfCurrent() ); //doesnt scroll!
                    }
                }

                setsUpTrigger = TriggerEvent.NONE;
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
