package com.gk.simpleworkoutjournal;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.CursorLoader;
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

import static com.gk.simpleworkoutjournal.WorkoutDataAdapter.*;

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
    WorkoutDataAdapter currAdapter, exerciseLogAdapter, setsAdapter;

    ImageButton switchBtn;
    boolean inContextMode;

    WJContext exercisesContextualMode;
    WJContext setsContextualMode;

    boolean notesShowed = false;
    DBClass dbmediator;

    WorkoutDataCursorLoader setsListDataLoader;
    WorkoutDataCursorLoader exListDataLoader;

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
        allExCursor = dbmediator.fetchExerciseHistory();

        exerciseLogAdapter = new WorkoutDataAdapter(this, allExCursor, WorkoutDataAdapter.Subject.EXERCISES);

        //fill the text view now
        exercisesLv.setAdapter(exerciseLogAdapter);

        //exercisesLv.setSelection(exerciseLogAdapter.getCount()-1);
        //exercisesLv.smoothScrollToPosition(exercisesLv.getCount() - 1);
        //exercisesLv.setItemChecked(exercisesLv.getCount() - 1, true); TODO: remove
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
    public void initiateListUpdate( Subject subj, TriggerEvent event ) {
        Log.v( APP_NAME, "WorkoutJournal :: initiateListUpdate subj: "+ subj.toString() + " caused by: " + event.toString() );
        int subject = -1;

        switch ( subj ) {
            case EXERCISES:
                exUpTrigger = event;
                subject = EXERCISES;
                break;

            case SETS:
                setsUpTrigger = event;
                subject = SETS;
                break;

            case ALL:
                exUpTrigger = event;
                setsUpTrigger = event;
                break;
        }

        if ( event == TriggerEvent.NOTEADD && subj != Subject.EXERCISES && subj != Subject.SETS ) {
            Log.v( APP_NAME, "WorkoutJournal :: initiateListUpdate : incoming parameters are messed" );
            return;
        }

        switch ( event ) {
            case INIT:
                getLoaderManager().initLoader( EXERCISES, null, this);
                getLoaderManager().initLoader( SETS, null, this);
                break;

            case CLICK:
                //no need to do anything with exercises
                if ( subj == Subject.SETS ) {
                    getLoaderManager().getLoader( SETS ).forceLoad();
                }
                break;

            case NOTEADD:
                getLoaderManager().getLoader( subject ).forceLoad();
                break;

            case ADD: // ex added - should renew both since focus changed. set added - only set lv to update
                if ( subj == Subject.EXERCISES ) {
                    getLoaderManager().getLoader( EXERCISES ).forceLoad();

                    setsUpTrigger = TriggerEvent.ADD;
                    getLoaderManager().getLoader(SETS).forceLoad();
                }
                if ( subj == Subject.SETS ) {
                    getLoaderManager().getLoader(SETS).forceLoad();
                }
                break;

            case DELETE: // set update may be not required if deleted ex is not current
                if ( subj == Subject.EXERCISES ) {
                    getLoaderManager().getLoader( EXERCISES ).forceLoad();
                }
                if ( subj == Subject.SETS ) {
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

        } else if (exerciseLogAdapter.getCurrent() != -1) {
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
                initiateListUpdate( Subject.EXERCISES, TriggerEvent.ADD );
            }

            //we are trying to add reps and weight
        } else {

            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed() Reps: " + repsEdit.getText() + " Weight: " + weightEdit.getText() + " Curr ex idx: " + exerciseLogAdapter.getCurrent());
            String repString = repsEdit.getText().toString();
            String weiString = weightEdit.getText().toString();

            if (repString.trim().length() == 0 || weiString.trim().length() == 0) {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
                return;
            }

            int newRep = Integer.parseInt(repString);
            Float newWei = Float.parseFloat(weiString);

            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). exercisesLv.getCheckedItemPosition() " + exercisesLv.getCheckedItemPosition() +
                    " allExCursor.getColumnIndex(DBClass.KEY_ID) " + exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_ID));

            //get entry for the current exercise
            Cursor tmpcs = (Cursor) exerciseLogAdapter.getItem(exerciseLogAdapter.getCurrent());
            String exerciseName = tmpcs.getString(exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_EX_NAME));
            Long exerciseLogId = tmpcs.getLong(exerciseLogAdapter.getCursor().getColumnIndex(DBClass.KEY_ID));

            dbmediator.insertSet( exerciseName, exerciseLogId, newRep, newWei);

            //refresh cursor
            initiateListUpdate(Subject.SETS, TriggerEvent.ADD);
            /*
            setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
            setsLv.setAdapter(setsAdapter);
            setsAdapter.changeCursor(allSetsCursor);
            setsAdapter.notifyDataSetChanged();*/


        }

        //set current to last
        //scroll to last
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

                String noteString = currAdapter.getCursor().getString( currAdapter.getCursor().getColumnIndex(DBClass.KEY_NOTE) );
                if ( noteString == null || noteString.isEmpty()) {
                    exerciseNoteTv.setText( getString(R.string.workout_exercise_newnote_hint) );
                } else {
                    exerciseNoteTv.setText( noteString );
                }

                // obtain sets for this exercise
                // fetch new sets only if exercise entry changed
                if (currAdapter.getCurrent() != position) {

                    currAdapter.setCurrent(position);
                    currCursor.moveToPosition(position);
                    currAdapter.notifyDataSetChanged();

                    //empty hint box for set since we have chosen other exercise
                    setNoteTv.setHint(getString(R.string.workout_set_no_note_hint));
                    setNoteTv.setText("");

                    //need to update sets according to new item
                    initiateListUpdate( Subject.SETS, TriggerEvent.CLICK );

                    /*
                    allSetsCursor = dbmediator.fetchSetsForExercise( allExCursor );
                    setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
                    setsLv.setAdapter(setsAdapter);
                    setsAdapter.notifyDataSetChanged();*/

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
                currAdapter = setsAdapter;
                noNoteHint = getString(R.string.workout_set_newnote_hint);

                // show required exercise for selected date
                //if (setsLv.getCount() != 0 )  { no need to check count in exercises since it always must be  filled
                //PROBLEM possibly set if is not set at that moment

                currAdapter.setCurrent(position);
                currAdapter.notifyDataSetChanged();

                int pos = syncPositionsBasedOnDate(setsLv, exercisesLv);
                //exercisesLv.setSelection( pos );// jump to last item
                View v = exercisesLv.getChildAt(pos);
                if (v != null) {
                    v.requestFocus();
                }

                break;
        }

        //remove line below
        Log.v(APP_NAME, "WorkoutJournal :: onItemClick , click position: " + position + " selected: " + currLv.getSelectedItemPosition() + " checked: " + exercisesLv.getCheckedItemPosition() + " current: " + currAdapter.getCurrent());

        // show appropriate note or hint
        /*String noteSet =  currAdapter.getCursor().getString( currAdapter.getCursor().getColumnIndex(DBClass.KEY_NOTE));
        DatabaseUtils.dumpCursor( currAdapter.getCursor() );
        Log.v(APP_NAME, "WorkoutJournal :: onItemClick :: note : " + noteSet);
        if (noteSet == null) {
            currNoteTv.setHint(noNoteHint);
            currNoteTv.setText("");
        } else {
            currNoteTv.setText(noteSet);
        }*/

        Log.v(APP_NAME, "WorkoutJournal :: onItemClick new current: " + currAdapter.getCurrent() + " have note: " + 1);
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
                    if (exerciseLogAdapter.getCurrent() != -1) {
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
        Log.v(APP_NAME, "syncPositions: current src-dst pos (" + primaryAdapter.getCurrent() + "-" + secondaryAdapter.getCurrent() + ")");
        srcCursor.moveToPosition(primaryAdapter.getCurrent());

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

            switch (setsAdapter.compareDates(curDate, baseDate)) {
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

        switch (v.getId()) {
            case R.id.exerciseNoteTv:
                haveRecepient = true;
                currLv = exercisesLv;

                currAdapter = exerciseLogAdapter;
                currCursor = (Cursor) currAdapter.getItem(currAdapter.getCurrent()); //check.

                Log.v(APP_NAME, "tapped notes: exercise section. current item: " + exerciseLogAdapter.getCurrent());
                if (exerciseLogAdapter.getCurrent() == -1) {

                    Log.v(APP_NAME, "tapped notes: doing nothing since no item selected");
                    return;
                }
                Log.v(APP_NAME, "tapped notes: exercise section. current item: " + exerciseLogAdapter.getCurrent());

                headText = currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_EX_NAME));
                isExercise = 1;
                break;
            case R.id.setNoteTv:
                haveRecepient = true;
                currLv = setsLv;
                currAdapter = setsAdapter;

                //if no sets
                if (setsAdapter == null) {
                    Log.v(APP_NAME, "tapped notes: doing nothing since adapter dont exist");
                    return;
                }

                currCursor = (Cursor) currAdapter.getItem(currAdapter.getCurrent());

                if (setsAdapter.getCurrent() == -1) {
                    Log.v(APP_NAME, "tapped notes: doing nothing since no item selected");
                    return;
                }
                Log.v(APP_NAME, "tapped notes: sets section. current item: " + setsAdapter.getCurrent());

                if (currCursor.getCount() == 0)
                    return;// here need other check for case when no set selected (when navogating after exercise lv tapped)

                headText = allExCursor.getString(allExCursor.getColumnIndex(DBClass.KEY_EX_NAME)) + "  " +
                        currCursor.getInt(currCursor.getColumnIndex(DBClass.KEY_REPS)) + " : " +
                        currCursor.getFloat(currCursor.getColumnIndex(DBClass.KEY_WEIGHT));
                break;
            default:
                return;
        }

        if (haveRecepient && !(currCursor == null) && currCursor.getCount() != 0) {
            currCursor.moveToPosition(currAdapter.getCurrent());
            String note =  currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_NOTE));
            Log.v(APP_NAME, " note: " + note);

            Intent dialogIntent = new Intent(this, NotesDialog.class);
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
            currCursor.moveToPosition(currAdapter.getCurrent());
            String note = data.getStringExtra("note");

            Log.v(APP_NAME, "onActivityResult once got item");

            switch (requestCode) {

                case 1: //exercise
                    Log.v(APP_NAME, "gonna insert note '" + note + "' for ex  " + currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_EX_NAME)));
                    dbmediator.insertExerciseNote( exerciseLogAdapter.getCursor(), note);
                    exerciseNoteTv.setText(note);

                    initiateListUpdate( Subject.EXERCISES, TriggerEvent.NOTEADD );

                    /*
                    allExCursor = dbmediator.fetchExerciseHistory();
                    currAdapter.changeCursor(allExCursor);*/
                    currCursor = allExCursor;
                    Log.v(APP_NAME, "onActivityResult once fetched exercise history after inserting note");

                    break;

                case 0: //set

                    dbmediator.insertSetNote( setsAdapter.getCursor(), note);
                    setNoteTv.setText(note);

                    initiateListUpdate(Subject.SETS, TriggerEvent.NOTEADD);
                    /*
                    allSetsCursor = dbmediator.fetchSetsForExercise( allExCursor );
                    currAdapter.changeCursor(allSetsCursor);*/
                    currCursor = allSetsCursor;
                    break;
            }
            //currAdapter.changeCursor( currCursor );
            currAdapter.notifyDataSetChanged();
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
            exerciseLogAdapter.setCurrent(-1);
            retCode = 3;
        }
        //if deleted item was last in a row - need to decrement current
        else if ( idxOfDeleted == sumOfElements-1 ) {
            Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of last in a row item.");

            exerciseLogAdapter.setCurrent(idxOfDeleted - 1);
            retCode = 2;
        }
        else if ( idxOfDeleted == 0 ) {
            Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of first in a row item.");

            //selected is still the same. However, it's position may be changed now if one of prev entries was deleted or selected was last item in the list
            if ( idxOfDeleted < exerciseLogAdapter.getCurrent() || exerciseLogAdapter.getCurrent() == sumOfElements-1 ) {
                exerciseLogAdapter.setCurrent( exerciseLogAdapter.getCurrent() -1 );
            }

            retCode = 1;
        }

        //show renewed data for exercises
        initiateListUpdate( Subject.EXERCISES, TriggerEvent.DELETE );

        /*
        allExCursor = dbmediator.fetchExerciseHistory();
        exerciseLogAdapter.changeCursor(allExCursor);*/

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
        /*
        allSetsCursor = dbmediator.fetchSetsForExercise( allExCursor );
        setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
        setsLv.setAdapter(setsAdapter);
        setsAdapter.notifyDataSetChanged();*/

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(APP_NAME, "onCreateLoader :: Id " + id );
        if ( id == EXERCISES ) {
            return exListDataLoader = new WorkoutDataCursorLoader(this, dbmediator, exerciseLogAdapter.getCursor() );
        } else {
            return setsListDataLoader = new WorkoutDataCursorLoader(this, dbmediator, exerciseLogAdapter.getCursor() );
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
                setsListDataLoader.renewExCursor( data );

                //always empty notes box for sets since we lost focus from sets. Added: only when selected was changed/prev changed deleted
                setNoteTv.setText("");
                setNoteTv.setHint( R.string.workout_set_no_note_hint );

                // if add button clicked
                if ( exUpTrigger == TriggerEvent.ADD ) {

                    exerciseLogAdapter.setCurrent(exerciseLogAdapter.getCursor().getCount() - 1);
                    exerciseLogAdapter.getCursor().moveToPosition(exerciseLogAdapter.getCurrent());
                    exercisesLv.smoothScrollToPosition(exercisesLv.getCount() - 1);

                    setsUpTrigger = TriggerEvent.ADD;
                    getLoaderManager().getLoader( SETS ).forceLoad();
                }


                exUpTrigger = TriggerEvent.NONE;
                break;

            case SETS:
                /*
            setsLv.setAdapter(setsAdapter);
            setsAdapter.changeCursor(allSetsCursor);
            setsAdapter.notifyDataSetChanged();*/
                if ( data != null ) {
                    setsLv.setAdapter( setsAdapter = new WorkoutDataAdapter(this, data, WorkoutDataAdapter.Subject.SETS) );
                }

                if ( setsUpTrigger == TriggerEvent.ADD ) {
                    setsLv.smoothScrollToPosition(setsLv.getCount() - 1);
                }

                setsUpTrigger = TriggerEvent.NONE;
                break;
        }



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class WorkoutDataCursorLoader extends CursorLoader {

        DBClass db;
        Cursor exCursor;

        public WorkoutDataCursorLoader(Context context, DBClass db, Cursor currCursor) {
            super( context );

            Log.v(APP_NAME, "WorkoutDataCursorLoader :: WorkoutDataCursorLoader");
            this.exCursor = currCursor;
            this.db = db;
        }

        public void renewExCursor( Cursor exCursor ) { this.exCursor = exCursor; }

        @Override
        public Cursor loadInBackground() {
            //get sets/exercises
            Log.v(APP_NAME, "WorkoutDataCursorLoader :: loadInBackground :: id "+this.getId() );
            Cursor cursor = null;
            switch ( this.getId() ) {
                case EXERCISES:

                    cursor = db.fetchExerciseHistory();
                    Log.v(APP_NAME, "WorkoutDataCursorLoader :: XXX :: updating cursor ");
                    DatabaseUtils.dumpCursor(exCursor);
                    break;

                case SETS:

                    cursor = db.fetchSetsForExercise(exCursor);

                    Log.v(APP_NAME, "WorkoutDataCursorLoader :: XXX :: using cursor ");
                    DatabaseUtils.dumpCursor(exCursor);
                    DatabaseUtils.dumpCursor(cursor);
                    break;
            }

            return cursor;
        }

    }
}
