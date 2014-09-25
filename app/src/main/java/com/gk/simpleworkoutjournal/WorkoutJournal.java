package com.gk.simpleworkoutjournal;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gk.datacontrol.DBClass;

import static com.gk.simpleworkoutjournal.WorkoutDataAdapter.*;

public class WorkoutJournal extends Activity implements  OnItemClickListener, OnTouchListener, OnItemLongClickListener  {
	public static final String APP_NAME = "SWJournal";
	
	LinearLayout notesLayout;
	
	AutoCompleteTextView exerciseTextView;
	EditText repsEdit, weightEdit;
	
	ListView 		   currLv,      exercisesLv,      setsLv;
	TextView 		   currNoteTv,  exerciseNoteTv,   setNoteTv ;
	Cursor   		   currCursor,  allExCursor,      allSetsCursor;
	WorkoutDataAdapter currAdapter, exercisesAdapter, setsAdapter;
	
	ActionMode  contextMode;
	boolean inContextMode;
	
	boolean notesShowed = false;
	DBClass dbmediator;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_journal);
        
        // fetch all required UI items
        notesLayout = (LinearLayout)findViewById(R.id.notesLayout);
        
        setsLv = (ListView)findViewById(R.id.setsLv);
        setNoteTv = (TextView) findViewById(R.id.setNoteTv);
        
        exercisesLv = (ListView)findViewById(R.id.exercisesLv);
        exerciseNoteTv = (TextView) findViewById(R.id.exerciseNoteTv);
        
        exerciseTextView = (AutoCompleteTextView) findViewById(R.id.addExerciseACTV);
        
        repsEdit = (EditText) findViewById(R.id.editReps);
        weightEdit = (EditText) findViewById(R.id.editWeight);
      
        // set notes touch listeners for exercise and set
        exerciseNoteTv.setOnTouchListener(this);
        
        // set click / touch listeners
        setsLv.setOnItemClickListener(this);
        exercisesLv.setOnItemClickListener(this);
        
        setsLv.setOnTouchListener(this);
        exercisesLv.setOnTouchListener(this);
        
        // set long click listeners
        setsLv.setOnItemLongClickListener( this );
        exercisesLv.setOnItemLongClickListener( this );
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
    	exercisesLv.setItemChecked(exercisesLv.getCount() - 1, true);
    	//TODO: show appropriate sets
    	
    	// this stuff is neede for context menu
    	exercisesLv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    	//setsLv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    	
    	exercisesLv.setMultiChoiceModeListener( new ContextMenuCallback( allExCursor, dbmediator  ) );
    	setsLv.setMultiChoiceModeListener( new ContextMenuCallback( allSetsCursor, dbmediator  ) );
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
    	Log.d(APP_NAME, "onOptionsItemSelected "+item.getItemId() );
        switch (item.getItemId()) {
	        case R.id.action_moreinfo_icon:
	            if ( notesShowed ) {
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
    	Log.v(APP_NAME, "WorkoutJournal :: onBackButtonPressed()" );
    	if (exerciseTextView.getVisibility() == View.GONE) {
			exerciseTextView.setVisibility(View.VISIBLE);
			repsEdit.setVisibility(View.GONE);
			weightEdit.setVisibility(View.GONE);
    	} else if ( exercisesAdapter.getCurrent() != -1 ) { 
			exerciseTextView.setVisibility(View.GONE);
			repsEdit.setVisibility(View.VISIBLE);
			weightEdit.setVisibility(View.VISIBLE);
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

	    	Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). Exercise in edit text: "+exerciseTextView.getText());
	    	String incomingName = exerciseTextView.getText().toString();
            incomingName = incomingName.trim();
	    	if ( incomingName.length() == 0 ) {
	    		Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
	    		return;
	    	}
	    	
	    	dbmediator.addExercise( incomingName ); // may fail since exercise is in db - it's ok
	    	if ( dbmediator.logExercise( incomingName ) ) {
		    	//populate listview with updated data
		    	allExCursor = dbmediator.fetchExerciseHistory();
		    	exercisesAdapter.changeCursor(allExCursor);

                currAdapter = exercisesAdapter;
                currCursor = allExCursor;
		    	exercisesAdapter.setCurrent(allExCursor.getCount() - 1 );

		    	exerciseTextView.setText("");

                //show note at once if it exist
                Cursor thisExercise = (Cursor) exercisesAdapter.getItem( exercisesAdapter.getCurrent() );
                String exerciseNote = thisExercise.getString( allExCursor.getColumnIndex(DBClass.KEY_NOTE) );

                //if not hint - empty box. If hint exist for this exercise - add it to box
                if ( exerciseNote != null ) {
                    exerciseNoteTv.setText( exerciseNote );
                } else  {
                    exerciseNoteTv.setText("");
                    exerciseNoteTv.setHint(R.string.workout_exercise_newnote_hint);
                }

                //always empty notes box for sets sice we lost focuse
                setNoteTv.setText("");
                setNoteTv.setHint(R.string.workout_set_no_note_hint);

		    	//jump to last item but do not choose it
		    	exercisesLv.smoothScrollToPosition(exercisesLv.getCount() - 1);

                //get set list for this exercise
                allSetsCursor = dbmediator.fetchSetsForExercise( incomingName );
                setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
                setsLv.setAdapter( setsAdapter  );
                setsAdapter.notifyDataSetChanged();

	    	}
	    	
    	//we are trying to add reps and weight
    	} else {



            Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed() Reps: "+repsEdit.getText()+ " Weight: "+weightEdit.getText()+ "Ex curr idx: "+exercisesAdapter.getCurrent() );
    		String repString = repsEdit.getText().toString();
    		String weiString = weightEdit.getText().toString();
    		
    		if ( repString.trim().length() == 0 || weiString.trim().length() == 0 ) {
	    		Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show(); // TODO: make a string resources for this toast
	    		return;	
    		}

    		int newRep = Integer.parseInt(repString);
	    	Float newWei = Float.parseFloat(weiString);
	    	
			
	    	Log.v(APP_NAME, "WorkoutJournal :: onAddButtonPressed(). exercisesLv.getCheckedItemPosition() "+exercisesLv.getCheckedItemPosition()+
	    			" allExCursor.getColumnIndex(DBClass.KEY_ID) "+allExCursor.getColumnIndex(DBClass.KEY_ID));
	    	//get name of the current exercise
			Cursor tmpcs = (Cursor) exercisesAdapter.getItem(exercisesAdapter.getCurrent() );
			Log.v(APP_NAME, "tmpcs rows: "+tmpcs.getCount());
			String exerciseName = tmpcs.getString(allExCursor.getColumnIndex(DBClass.KEY_ID));
	    	
	    	dbmediator.insertSet(exerciseName, newRep, newWei );
	    	
	    	allSetsCursor = dbmediator.fetchSetsForExercise(exerciseName);
	    	Log.v(APP_NAME, "before change cursor");

            //refresh cursor
            setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
            setsLv.setAdapter( setsAdapter  );
            setsAdapter.changeCursor(allSetsCursor);
            setsAdapter.notifyDataSetChanged();
            setsLv.smoothScrollToPosition( setsLv.getCount() - 1);

    	}
    	dbmediator.close();
    }

    public void updateContextBar()
    {
        String contextMainText = "unknown";

        switch ( currAdapter.getSubject() ) {
            case EXERCISES:
                contextMainText = currCursor.getString(currCursor.getColumnIndex("_id"));
                break;
            case SETS:
                contextMainText =  "Set chosen";
                break;
        }

         contextMode.setTitle( contextMainText  );
    }
    /*
     * 
     * 
     */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub

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
				if ( currAdapter.getCurrent() != position )	{
				    //empty hint box for set since we have chosen other exercise
					setNoteTv.setHint( getString(R.string.workout_set_no_note_hint) );
					setNoteTv.setText("");
					
					Cursor tmpcs = (Cursor) exercisesAdapter.getItem(position);
					String exercise = tmpcs.getString(allExCursor.getColumnIndex(DBClass.KEY_ID));
					dbmediator.open(); 
					allSetsCursor = dbmediator.fetchSetsForExercise( exercise );
					dbmediator.close();
					setsAdapter = new WorkoutDataAdapter(this, allSetsCursor, WorkoutDataAdapter.Subject.SETS);
				    setsLv.setAdapter( setsAdapter  );
				    setsAdapter.notifyDataSetChanged();
				    if (setsLv.getCount() != 0 )  {
				    	int pos = syncPositionsBasedOnDate( exercisesLv, setsLv);
				    	//setsLv.setSelection( pos ); // jump to last item
				        View v = setsLv.getChildAt( pos );
				        if (v != null) 
				        {
				           v.requestFocus();
				        }
				    }
				}

				
		    	exerciseTextView.setVisibility(View.GONE);
				repsEdit.setVisibility(View.VISIBLE);
				weightEdit.setVisibility(View.VISIBLE);
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
				int pos = syncPositionsBasedOnDate( setsLv, exercisesLv);
			    	//exercisesLv.setSelection( pos );// jump to last item
			        View v = exercisesLv.getChildAt( pos );
			        if (v != null) 
			        {
			           v.requestFocus();
			        }
			    //}
				//dcroll
				
				break;
		}
		
		//remove line below
		Log.v(APP_NAME, "WorkoutJournal :: onItemClick , click position: "+position+" selected: "+currLv.getSelectedItemPosition()+" checked: "+exercisesLv.getCheckedItemPosition()+" current: "+currAdapter.getCurrent());	
		
		// reset bg color for for previous view, set new view as current and set bg color to it.
		if ( currAdapter.getCurrent() != -1 ) 
			currAdapter.getView( currAdapter.getCurrent(), view, currLv ).setBackgroundColor(Color.WHITE);
		currAdapter.setCurrent( position );
		currAdapter.getView( currAdapter.getCurrent(), view, currLv ).setBackgroundColor( getResources().getColor(R.color.baseColor_ligher) );
		currAdapter.notifyDataSetChanged();
		
		// show appropriate note or hint
		Cursor tmpcs2 = (Cursor) currAdapter.getItem(position);
		String noteSet = tmpcs2.getString(currCursor.getColumnIndex(DBClass.KEY_NOTE));
		Log.v(APP_NAME, "onItemClick noteSet : "+noteSet);
		if (noteSet == null) {
			currNoteTv.setHint( noNoteHint );
			currNoteTv.setText("");
		} else {
			currNoteTv.setText(noteSet);
		}
		
		if ( inContextMode )
		{
            updateContextBar();
		}
		
		Log.v(APP_NAME, "WorkoutJournal :: onItemClick new current: "+currAdapter.getCurrent()+" have note: "+noteSet );
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

/*
            Log.v(APP_NAME," dumping after ontouch");
            Log.v(APP_NAME,"current item of currCursor"+currAdapter.getCurrent() );
            DatabaseUtils.dumpCursor(currCursor);*/
			//Log.v(APP_NAME, "WorkoutJournal :: onTouch , click selected: "+currLv.getSelectedItemPosition()+" checked: "+currLv.getCheckedItemPosition());


			switch (view.getId()) {
				case R.id.setsLv:
					if ( exercisesAdapter.getCurrent() != -1 ) { 
						exerciseTextView.setVisibility(View.GONE);
						repsEdit.setVisibility(View.VISIBLE);
						weightEdit.setVisibility(View.VISIBLE);
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
	public int syncPositionsBasedOnDate( ListView primaryLv, ListView secondaryLv) {
		// move cursor to this position  in order to get values from there
		int srcPosition = primaryLv.getCheckedItemPosition();
		WorkoutDataAdapter primaryAdapter = (WorkoutDataAdapter) primaryLv.getAdapter();
		Cursor srcCursor = primaryAdapter.getCursor();
		
		WorkoutDataAdapter secondaryAdapter = (WorkoutDataAdapter) secondaryLv.getAdapter();
		Cursor dstCursor = secondaryAdapter.getCursor();
		
		srcCursor.moveToPosition( srcPosition );
		Log.v(APP_NAME, "syncPositions: current src-dst pos ("+ primaryAdapter.getCurrent() +"-"+secondaryAdapter.getCurrent()+")" );
		srcCursor.moveToPosition( primaryAdapter.getCurrent() );
		Log.v(APP_NAME, "syncPositions: dumping src cursor with active pos: "+srcCursor.getPosition() );
		DatabaseUtils.dumpCursor(srcCursor);
		long baseDate = srcCursor.getLong(srcCursor.getColumnIndex(DBClass.KEY_TIME));   // and get  time
		long curDate; // =  dstCursor.getColumnIndex(DBClass.KEY_TIME);
		boolean found = false;
		int max = secondaryAdapter.getCount();
		int min = 0;
		int timeColumnIdx = dstCursor.getColumnIndex(DBClass.KEY_TIME);
		while (!found) {
			dstCursor.moveToPosition( (min + max) / 2);
			curDate = dstCursor.getLong(timeColumnIdx);
			Log.v(APP_NAME, "syncPositions: search source date "+baseDate+" in dst cursor (range ["+min+"-"+max+"]). Current dst: "+dstCursor.getPosition()+" with date "+curDate);
			
			switch ( setsAdapter.compareDates(curDate, baseDate)) {
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
			if ( (max - min) < 3  ) break;
		}
		//if (min == setCur.getPosition()) return setCur.getCount()-1; // take last position
		Log.v(APP_NAME, "WorkoutJournal :: syncPositions : set to "+dstCursor.getPosition());
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
			currCursor = (Cursor) currAdapter.getItem( currAdapter.getCurrent() ); //check.

			Log.v(APP_NAME, "tapped notes: exercise section. current item: "+exercisesAdapter.getCurrent());
            if ( exercisesAdapter.getCurrent() == -1 ) {

                Log.v(APP_NAME, "tapped notes: doing nothing since no item selected");
                return;
            }
            Log.v(APP_NAME, "tapped notes: exercise section. current item: "+exercisesAdapter.getCurrent());

			headText = currCursor.getString( currCursor.getColumnIndex(DBClass.KEY_ID));
			isExercise = 1;
			break;
		case R.id.setNoteTv:
			haveRecepient = true;
			currLv = setsLv;
			currAdapter = setsAdapter;

            //if no sets
            if ( setsAdapter == null )
            {
                Log.v(APP_NAME, "tapped notes: doing nothing since adapter dont exist");
                return;
            }

			currCursor = (Cursor) currAdapter.getItem( currAdapter.getCurrent() );
			
		    if ( setsAdapter.getCurrent() == -1 ) {
                Log.v(APP_NAME, "tapped notes: doing nothing since no item selected");
                return;
            }
            Log.v(APP_NAME, "tapped notes: sets section. current item: "+setsAdapter.getCurrent());

			if ( currCursor.getCount() == 0 ) return;// here need other check for case when no set selected (when navogating after exercise lv tapped)
			headText = currCursor.getString( currCursor.getColumnIndex(DBClass.KEY_EX_NAME)) + "  "  +
					   currCursor.getInt( currCursor.getColumnIndex(DBClass.KEY_REPS))    + " : " +
					   currCursor.getFloat( currCursor.getColumnIndex(DBClass.KEY_WEIGHT));
			break;
		default:
			return;
		}



		if (haveRecepient && !(currCursor == null) && currCursor.getCount() != 0 ) {
			currCursor.moveToPosition( currAdapter.getCurrent() );
			String note = currCursor.getString( currCursor.getColumnIndex(DBClass.KEY_NOTE));
			
			Log.v(APP_NAME," note: "+note );
			
			Intent dialogIntent = new Intent(this, NotesDialog.class);
			dialogIntent.putExtra("headText", headText);
			dialogIntent.putExtra("note", note);
			// id is exercise name or sets id
			
			startActivityForResult( dialogIntent, isExercise );
			Log.v(APP_NAME, "after  startActivity" );
		} 
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(APP_NAME, "onActivityResult" );

		if ( resultCode == RESULT_OK ) {
			String note = data.getStringExtra("note");

            Log.v(APP_NAME, "bbb");
            DatabaseUtils.dumpCursor(currCursor); //gk

			currCursor = (Cursor) currAdapter.getItem( currAdapter.getCurrent() );
            Log.v(APP_NAME, "onActivityResult once got item with nr."+currAdapter.getCurrent());

            Log.v(APP_NAME, "ccc");
            DatabaseUtils.dumpCursor(currCursor); //gk

            Log.v(APP_NAME, "onActivityResult once got item");
			dbmediator.open();


			switch ( requestCode ){

				case 1: //exercise
                    Log.v(APP_NAME, "gonna insert note '"+note+"' for ex id "+currCursor.getString( currCursor.getColumnIndex(DBClass.KEY_ID)));
					dbmediator.insertExerciseNote(  currCursor.getString( currCursor.getColumnIndex(DBClass.KEY_ID) ), note );
					exerciseNoteTv.setText( note );

					allExCursor = dbmediator.fetchExerciseHistory();
                    currAdapter.changeCursor( allExCursor );
                    currCursor = allExCursor;
					Log.v(APP_NAME, "onActivityResult once fetched exercise history after inserting note");
					DatabaseUtils.dumpCursor(allExCursor);
                    Log.v(APP_NAME, "onActivityResult once fetched exercise history after inserting note");

					break;

				case 0: //set

					Log.v(APP_NAME, "gonna insert note '"+note+"' for set id "+currCursor.getLong( currCursor.getColumnIndex(DBClass.KEY_ID)));
					dbmediator.insertSetNote( currCursor.getLong( currCursor.getColumnIndex(DBClass.KEY_ID) ) , note );
					setNoteTv.setText( note );
					Cursor tmpcs = (Cursor) exercisesAdapter.getItem(exercisesAdapter.getCurrent());
					String test = tmpcs.getString(allExCursor.getColumnIndex(DBClass.KEY_ID));
					allSetsCursor = dbmediator.fetchSetsForExercise(test );
                    currAdapter.changeCursor( allSetsCursor );
                    currCursor = allSetsCursor;
					break;
			}
			//currAdapter.changeCursor( currCursor );
			currAdapter.notifyDataSetChanged();

			dbmediator.close();
			
		}
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
        //if (mActionMode != null) {
        //    return false;
        //}
    	//exercisesLv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    	//setsLv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // Start the CAB using the ActionMode.Callback defined above
        //mActionMode = getActivity().
		if ( inContextMode ) return false;
		
		Log.v(APP_NAME,"onLongClick :: going to start context");
		//ContextMenuCallback actionModeCb = new ContextMenuCallback( currCursor, dbmediator, ); //move to common
		//contextMode =  startActionMode( null );

	    assert contextMode != null;

        inContextMode = true;
        updateContextBar();
		//contextMode.setTag();

		//arg1.setSelected(true);
        return true;
	}

    class ContextMenuCallback implements AbsListView.MultiChoiceModeListener {
        public static final String APP_NAME = "SWJournal";
        private int id;
        private boolean isActive;
        private DBClass dbmediator;

        public void setData( Subject subj, int id ) {
            this.isActive = true;
            this.id = id;
        }

        @Override
        public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
            Log.v(APP_NAME, "ContextMenuCallback :: onActionItemClicked mode: "+arg0+" item: "+arg1);
            // TODO Auto-generated method stub
            arg0.getTag();
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.v(APP_NAME, "ContextMenuCallback :: onCreateActionMode mode: "+mode+" menu: "+menu);
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
            // TODO Auto-generated method stub
            return true;
        }

        public ContextMenuCallback( Cursor associatedCursor, DBClass dbmediator ) {
            // TODO Auto-generated method stub
            super();
            this.dbmediator = dbmediator;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            Log.v(APP_NAME, "ContextMenuCallback :: onDestroyActionMode mode: "+arg0);
            isActive = false;

            currAdapter.clearChecked();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
            Log.v(APP_NAME, "ContextMenuCallback :: onPrepareActionMode mode: "+arg0+ " menu: "+arg1);
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode arg0, int index, long arg2, boolean isChecked ) {
            contextMode =  startActionMode( this ); //required to set title later //TODO: check if need reduce scope of context mode.
            Log.v(APP_NAME, "ContextMenuCallback :: onItemCheckedStateChanged mode: " + arg0 + " int: " + index + " long " + arg2 + " bool: " + isChecked);
            if ( isChecked ) {
                currAdapter.setChecked( index );
            } else {
                currAdapter.unsetChecked( index );
            }

            String contextMenuTitle;
            switch ( currAdapter.getSubject() )
            {
                case EXERCISES:
                    exercisesAdapter.notifyDataSetChanged();
                    contextMode.setTitle("Exercises selected: "+ exercisesLv.getMaxScrollAmount() );

                    break;
                case SETS:
                    contextMode.setTitle("Sets selected: "+ setsLv.getCheckedItemCount() );
                    break;
                default:
                    throw new IllegalStateException( "trying to create context menu for unknown subject");
            }
           //TODO: show "sets" or "exercises"  in context bar. Find a wway to disable opposite list view while in comtect mode


        }

    }

}



