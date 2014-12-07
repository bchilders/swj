package com.gk.simpleworkoutjournal;

import android.app.Activity;
import android.content.Intent;
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

public class WorkoutJournal extends Activity implements  OnItemClickListener, OnTouchListener {
    public static final String APP_NAME = "SWJournal";

    LinearLayout notesLayout;

    AutoCompleteTextView exerciseTextView;
    EditText repsEdit, weightEdit;

    ListView currLv, exercisesLv, setsLv;
    TextView currNoteTv, exerciseNoteTv, setNoteTv;
    Cursor currCursor, allExCursor, allSetsCursor;
    WorkoutDataAdapter currAdapter, exercisesAdapter, setsAdapter;

    ImageButton switchBtn;
    boolean inContextMode;

    WJContext exercisesContextualMode;
    WJContext setsContextualMode;

    boolean notesShowed = false;
    DBClass dbmediator;

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

//        //TODO: DEV-ONLY 
//        Log.v(APP_NAME, "WorkoutJournal :: onCreate :: starting filling debug info, it will take some time");
//        DebugHelper debugHelper = new DebugHelper( dbmediator );
//        debugHelper.fillData();
//        Log.v(APP_NAME, "WorkoutJournal :: onCreate :: filling debug info completed");
//        //TODO: DEV-ONLY

        Log.d("WorkoutJournal", "onCreate :: creating adapter for exercises from db");
        dbmediator = new DBClass(this);
        dbmediator.open();
        allExCursor = dbmediator.fetchExerciseHistory();
        exercisesAdapter = new WorkoutDataAdapter(this, allExCursor, WorkoutDataAdapter.Subject.EXERCISES);
        //fill the text view now
        exercisesLv.setAdapter(exercisesAdapter);

        //exercisesLv.setSelection(exercisesAdapter.getCount()-1); 
        exercisesLv.smoothScrollToPosition(exercisesLv.getCount() - 1);
        //exercisesLv.setItemChecked(exercisesLv.getCount() - 1, true); TODO: remove
        //TODO: show appropriate sets
        exercisesContextualMode = new WJContext(this, Subject.EXERCISES);
        setsContextualMode = new WJContext(this, Subject.SETS);

        exercisesLv.setMultiChoiceModeListener( exercisesContextualMode );
        setsLv.setMultiChoiceModeListener( setsContextualMode );
        inContextMode = false;
        dbmediator.close();
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
        Log.d(APP_NAME, "onOptionsItemSelected " + item.getItemId());
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
        super.onDestroy();
        dbmediator.close();
    }

    public void onBackButtonPressed(View v) {


        Log.v(APP_NAME, "WorkoutJournal :: onBackButtonPressed()");
        if (exerciseTextView.getVisibility() == View.GONE) {
            exerciseTextView.setVisibility(View.VISIBLE);
            repsEdit.setVisibility(View.GONE);
            weightEdit.setVisibility(View.GONE);
            switchBtn.setImageResource(R.drawable.ic_custom_circledforward);

        } else if (exercisesAdapter.getCurrent() != -1) {
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
        dbmediator.open();
        //we are trying  both to log exercise to log DB and to add it into exercise DB and list view
        if (exerciseTextView.getVisibility() == View.VISIBLE) {

            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). Exercise in edit text: " + exerciseTextView.getText());
            String incomingName = exerciseTextView.getText().toString();
            incomingName = incomingName.trim();
            if (incomingName.length() == 0) {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
                return;
            }

            dbmediator.addExercise(incomingName); // may fail since exercise is in db - it's ok
            if (dbmediator.logExercise(incomingName)) {
                //populate listview with updated data
                allExCursor = dbmediator.fetchExerciseHistory();
                exercisesAdapter.changeCursor(allExCursor);

                currAdapter = exercisesAdapter;
                currCursor = allExCursor;
                exercisesAdapter.setCurrent(allExCursor.getCount() - 1);

                exerciseTextView.setText("");

                //show note at once if it exist
                Cursor thisExercise = (Cursor) exercisesAdapter.getItem(exercisesAdapter.getCurrent());
                String exerciseNote = thisExercise.getString(allExCursor.getColumnIndex(DBClass.KEY_NOTE));

                //if not hint - empty box. If hint exist for this exercise - add it to box
                if (exerciseNote != null) {
                    exerciseNoteTv.setText(exerciseNote);
                } else {
                    exerciseNoteTv.setText("");
                    exerciseNoteTv.setHint(R.string.workout_exercise_newnote_hint);
                }

                //always empty notes box for sets sice we lost focuse
                setNoteTv.setText("");
                setNoteTv.setHint(R.string.workout_set_no_note_hint);

                //jump to last item but do not choose it
                exercisesLv.smoothScrollToPosition(exercisesLv.getCount() - 1);

                //get set list for this exercise
                allSetsCursor = dbmediator.fetchSetsForExercise(incomingName);
                setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
                setsLv.setAdapter(setsAdapter);
                setsAdapter.notifyDataSetChanged();

            }

            //we are trying to add reps and weight
        } else {

            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed() Reps: " + repsEdit.getText() + " Weight: " + weightEdit.getText() + "Ex curr idx: " + exercisesAdapter.getCurrent());
            String repString = repsEdit.getText().toString();
            String weiString = weightEdit.getText().toString();

            if (repString.trim().length() == 0 || weiString.trim().length() == 0) {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
                return;
            }

            int newRep = Integer.parseInt(repString);
            Float newWei = Float.parseFloat(weiString);


            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). exercisesLv.getCheckedItemPosition() " + exercisesLv.getCheckedItemPosition() +
                    " allExCursor.getColumnIndex(DBClass.KEY_ID) " + allExCursor.getColumnIndex(DBClass.KEY_ID));
            //get name of the current exercise
            Cursor tmpcs = (Cursor) exercisesAdapter.getItem(exercisesAdapter.getCurrent());
            Log.v(APP_NAME, "tmpcs rows: " + tmpcs.getCount());
            String exerciseName = tmpcs.getString(allExCursor.getColumnIndex(DBClass.KEY_ID));

            dbmediator.insertSet(exerciseName, newRep, newWei);

            allSetsCursor = dbmediator.fetchSetsForExercise(exerciseName);
            Log.v(APP_NAME, "before change cursor");

            //refresh cursor
            setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
            setsLv.setAdapter(setsAdapter);
            setsAdapter.changeCursor(allSetsCursor);
            setsAdapter.notifyDataSetChanged();
            setsLv.smoothScrollToPosition(setsLv.getCount() - 1);

        }
        dbmediator.close();
    }

    public void updateContextBar() {
        /*
        switch ( currAdapter.getSubject() )
        {
            case EXERCISES:
                exercisesAdapter.notifyDataSetChanged();
                //contextMode.setTitle("Exercises selected: "+ currAdapter.getcheckedAmount() );

                break;
            case SETS:
               // contextMode.setTitle("Sets selected: "+ setsLv.getCheckedItemCount() );
                break;
            default:
                throw new IllegalStateException( "trying to create context menu for unknown subject");
        }*/
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
                currAdapter = exercisesAdapter;
                noNoteHint = getString(R.string.workout_exercise_newnote_hint);

                // obtain sets for this exercise
                // fetch new sets only if exercise entry changed
                if (currAdapter.getCurrent() != position) {

                    currAdapter.setCurrent(position);
                    currAdapter.notifyDataSetChanged();

                    //empty hint box for set since we have chosen other exercise
                    setNoteTv.setHint(getString(R.string.workout_set_no_note_hint));
                    setNoteTv.setText("");

                    //update sets list view accordingly
                    Cursor tmpcs = (Cursor) exercisesAdapter.getItem(position);
                    String exercise = tmpcs.getString(allExCursor.getColumnIndex(DBClass.KEY_ID));
                    dbmediator.open();
                    allSetsCursor = dbmediator.fetchSetsForExercise(exercise);
                    dbmediator.close();
                    setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
                    setsLv.setAdapter(setsAdapter);
                    setsAdapter.notifyDataSetChanged();

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
                //}
                //dcroll

                break;
        }

        //remove line below
        Log.v(APP_NAME, "WorkoutJournal :: onItemClick , click position: " + position + " selected: " + currLv.getSelectedItemPosition() + " checked: " + exercisesLv.getCheckedItemPosition() + " current: " + currAdapter.getCurrent());

        // show appropriate note or hint
        Cursor tmpcs2 = (Cursor) currAdapter.getItem(position);
        String noteSet = tmpcs2.getString(currCursor.getColumnIndex(DBClass.KEY_NOTE));
        Log.v(APP_NAME, "onItemClick noteSet : " + noteSet);
        if (noteSet == null) {
            currNoteTv.setHint(noNoteHint);
            currNoteTv.setText("");
        } else {
            currNoteTv.setText(noteSet);
        }

        if (inContextMode) {
            //TODO: if ever called? inContextMode is needed?
            Toast.makeText(this, "SHOULD NEVER REACH", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
            updateContextBar();
        }

        Log.v(APP_NAME, "WorkoutJournal :: onItemClick new current: " + currAdapter.getCurrent() + " have note: " + noteSet);
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
                    if (exercisesAdapter.getCurrent() != -1) {
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

        srcCursor.moveToPosition(srcPosition);
        Log.v(APP_NAME, "syncPositions: current src-dst pos (" + primaryAdapter.getCurrent() + "-" + secondaryAdapter.getCurrent() + ")");
        srcCursor.moveToPosition(primaryAdapter.getCurrent());
        Log.v(APP_NAME, "syncPositions: dumping src cursor with active pos: " + srcCursor.getPosition());
        DatabaseUtils.dumpCursor(srcCursor);
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

                currAdapter = exercisesAdapter;
                currCursor = (Cursor) currAdapter.getItem(currAdapter.getCurrent()); //check.

                Log.v(APP_NAME, "tapped notes: exercise section. current item: " + exercisesAdapter.getCurrent());
                if (exercisesAdapter.getCurrent() == -1) {

                    Log.v(APP_NAME, "tapped notes: doing nothing since no item selected");
                    return;
                }
                Log.v(APP_NAME, "tapped notes: exercise section. current item: " + exercisesAdapter.getCurrent());

                headText = currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_ID));
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
                headText = currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_EX_NAME)) + "  " +
                        currCursor.getInt(currCursor.getColumnIndex(DBClass.KEY_REPS)) + " : " +
                        currCursor.getFloat(currCursor.getColumnIndex(DBClass.KEY_WEIGHT));
                break;
            default:
                return;
        }

        if (haveRecepient && !(currCursor == null) && currCursor.getCount() != 0) {
            currCursor.moveToPosition(currAdapter.getCurrent());
            String note = currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_NOTE));

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
            String note = data.getStringExtra("note");

            //DatabaseUtils.dumpCursor(currCursor); //gk

            //currCursor = (Cursor) currAdapter.getItem(currAdapter.getCurrent());

            //DatabaseUtils.dumpCursor(currCursor); //gk

            Log.v(APP_NAME, "onActivityResult once got item");
            dbmediator.open();


            switch (requestCode) {

                case 1: //exercise
                    Log.v(APP_NAME, "gonna insert note '" + note + "' for ex id " + currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_ID)));
                    dbmediator.insertExerciseNote(currCursor.getString(currCursor.getColumnIndex(DBClass.KEY_ID)), note);
                    exerciseNoteTv.setText(note);

                    allExCursor = dbmediator.fetchExerciseHistory();
                    currAdapter.changeCursor(allExCursor);
                    currCursor = allExCursor;
                    Log.v(APP_NAME, "onActivityResult once fetched exercise history after inserting note");
                    DatabaseUtils.dumpCursor(allExCursor);
                    Log.v(APP_NAME, "onActivityResult once fetched exercise history after inserting note");

                    break;

                case 0: //set

                    Log.v(APP_NAME, "gonna insert note '" + note + "' for set id " + currCursor.getLong(currCursor.getColumnIndex(DBClass.KEY_ID)));
                    dbmediator.insertSetNote(currCursor.getLong(currCursor.getColumnIndex(DBClass.KEY_ID)), note);
                    setNoteTv.setText(note);
                    Cursor tmpcs = (Cursor) exercisesAdapter.getItem(exercisesAdapter.getCurrent());
                    String test = tmpcs.getString(allExCursor.getColumnIndex(DBClass.KEY_ID));
                    allSetsCursor = dbmediator.fetchSetsForExercise(test);
                    currAdapter.changeCursor(allSetsCursor);
                    currCursor = allSetsCursor;
                    break;
            }
            //currAdapter.changeCursor( currCursor );
            currAdapter.notifyDataSetChanged();
            dbmediator.close();

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
    @return 2 if no items left to operate on
            1 if idx of checked must be decremented.
            0 if no extra actions required
     */
    public int adjustAfterExDeleted( ) {
        Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted");
        int retCode = 0;

        // remember - these are values before adjustment - DB is already changed!
        int prevHighlighted  = exercisesAdapter.getCurrent();
        int sumOfElements = exercisesLv.getCount();

        //if it was the only left element - need to report to handle checked items.
        if ( sumOfElements <= 1 ) {
            Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of very last item. Sum of items before deletion: "+sumOfElements);
            retCode = 2;
        }
        //if deleted item was last in a row - need to decrement current
        else if ( prevHighlighted == sumOfElements-1 ) {
            Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted detected deletion of last in a row item.");

            exercisesAdapter.setCurrent( prevHighlighted - 1 );
            retCode = 1;
        }

        Cursor entry = (Cursor)currLv.getItemAtPosition( prevHighlighted );
        String exercise = entry.getString( entry.getColumnIndex("exercise_name") );

        Log.v(APP_NAME, "WorkoutJournal :: adjustAfterExDeleted prevHighlighted: "+prevHighlighted+" sumOfElements: "+sumOfElements+" exercise: "+exercise);
        //show renewed data for exercises and related sets
        dbmediator.open();
        allExCursor = dbmediator.fetchExerciseHistory();
        dbmediator.close();
        exercisesAdapter.changeCursor(allExCursor);

        return retCode;
    }
}
