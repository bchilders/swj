package com.gk.simpleworkoutjournal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.HashSet;

/**
 * Created by Georgeek on 30.11.2014.
 */

class WJContext implements AbsListView.MultiChoiceModeListener, DialogInterface.OnClickListener  {
    public static final String APP_NAME = "SWJournal";
    WorkoutJournal activity;
    WorkoutDataAdapter.Subject contextSubj;

    LinearLayout actionModeZone;
    Button ctxDeleteLogBtn;

    ImageButton ctxCancelBtn;
    ImageButton ctxAddBtn;

    EditText ctxEditRepsField;
    EditText ctxEditWeightField;

    AutoCompleteTextView ctxEditExField;

    ActionMode thisActionMode;

    WJContext(WorkoutJournal activity, WorkoutDataAdapter.Subject subj)
    {
        super();
        this.activity = activity;
        this.contextSubj = subj;

        actionModeZone = (LinearLayout) activity.findViewById( R.id.actionModeZone);
        ctxDeleteLogBtn = (Button)activity.findViewById(R.id.ctx_deleteLogEntriesBtn);

        ctxCancelBtn = (ImageButton)activity.findViewById(R.id.ctx_cancelBtn);
        ctxAddBtn = (ImageButton) activity.findViewById(R.id.ctx_addEditedBtn);

        ctxEditRepsField = (EditText)activity.findViewById(R.id.ctx_editReps);
        ctxEditWeightField = (EditText)activity.findViewById(R.id.ctx_editWeight);

        ctxEditExField = (AutoCompleteTextView) activity.findViewById(R.id.ctx_editExerciseACTV);
    }


    @Override
    public boolean onActionItemClicked(ActionMode actMode, MenuItem menuItem) {
        Log.v(APP_NAME, "WJContext :: onActionItemClicked mode: " + actMode + " item: " + menuItem);

        //get the only possible entry to work with
        if ( activity.currAdapter.getcheckedAmount() != 1 ) {
            Log.e(APP_NAME, "WJContext :: onActionItemClicked: one checked expected, other amount is actually checked: "+activity.currAdapter.getcheckedAmount());
            return false;
        }

        Integer sequenceNumber = (Integer)activity.currAdapter.getIdsOfCtxChecked().toArray()[0];
        Cursor entry = (Cursor)activity.currLv.getItemAtPosition( sequenceNumber );

        //launch appropriate action for this entry
        switch( menuItem.getItemId() )
        {
            case R.id.context_action_rename_edit_single:
                Log.v(APP_NAME, "WJContext :: onActionItemClicked case: edit/rename");

                ctxDeleteLogBtn.setVisibility(View.GONE);
                ctxCancelBtn.setVisibility(View.VISIBLE);
                ctxAddBtn.setVisibility(View.VISIBLE);

                if ( contextSubj == WorkoutDataAdapter.Subject.EXERCISES )   {
                    ctxEditExField.setVisibility(View.VISIBLE);
                    ctxEditRepsField.setVisibility(View.GONE);
                    ctxEditWeightField.setVisibility(View.GONE);
                } else {
                    ctxEditExField.setVisibility(View.GONE);
                    ctxEditRepsField.setVisibility(View.VISIBLE);
                    ctxEditWeightField.setVisibility(View.VISIBLE);
                }

                break;

            case R.id.context_action_delete_ex:

                ctxDeleteLogBtn.setVisibility(View.VISIBLE);
                ctxCancelBtn.setVisibility(View.GONE);
                ctxAddBtn.setVisibility(View.GONE);

                Log.v(APP_NAME, "WJContext :: onActionItemClicked case: delete ex");

                String exToDelete = entry.getString( entry.getColumnIndex("exercise_name") );

                //some dialog over here
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder( this.activity );
                alertBuilder.setPositiveButton( "Delete", this );
                alertBuilder.setNegativeButton( "Cancel", this );
                alertBuilder.setTitle(exToDelete);
                alertBuilder.setMessage("Delete everything related to this exercise?");

                AlertDialog alert = alertBuilder.create();

                alert.show();
                //rest of work will be done by alert handler


                break;

            default:
                Log.e(APP_NAME, "WJContext :: onActionItemClicked unexpected case");
                break;
        }

        return true;
    }


    @Override
    public boolean onCreateActionMode(ActionMode actMode, Menu menu) {
        Log.v(APP_NAME, "WJContext :: onCreateActionMode mode: "+actMode+" menu: "+menu);

        actionModeZone.setVisibility( View.VISIBLE );
        thisActionMode = actMode;
        MenuInflater inflater = actMode.getMenuInflater();
        inflater.inflate(R.menu.workout_context_menu, menu);

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actMode) {
        Log.v(APP_NAME, "WJContext :: onDestroyActionMode mode: "+actMode);
        actionModeZone.setVisibility( View.GONE );

        if ( !activity.setsLv.isEnabled() ) activity.setsLv.setEnabled( true );
        if ( !activity.exercisesLv.isEnabled() ) activity.exercisesLv.setEnabled( true );
        activity.currAdapter.clearChecked();
        //currAdapter.notifyDataSetChanged(); TODO: probably may remove
    }

    @Override
    public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
        Log.v(APP_NAME, "WJContext :: onPrepareActionMode mode: "+arg0+ " menu: "+arg1);
        // TODO Auto-generated method stub
        return true;
    }


    @Override
    public void onItemCheckedStateChanged(ActionMode actMode, int index, long arg2, boolean isChecked ) {
        //contextMode =  startActionMode( this ); //required to set title later //TODO: check if need reduce scope of context mode.
        Log.e(APP_NAME, "WJContext :: onItemCheckedStateChanged mode: " + actMode + " int: " + index + " long " + arg2 + " bool: " + isChecked);

        //reset buttons and editTexts
        onCancelEditBtnPressed();

        String actionBarText = "";
        //if long click is the first click - we need to get currLv here. Potentially will need to define other current as well!
        switch ( this.contextSubj )
        {
            case EXERCISES:
                //if changed from other listview
                if ( activity.setsLv.isEnabled() ) activity.setsLv.setEnabled( false );

                actionBarText = "Exercises chosen: ";
                activity.currLv = activity.exercisesLv;
                activity.currAdapter = activity.exerciseLogAdapter;

                break;
            case SETS:
                //if changed from other listview
                if ( activity.exercisesLv.isEnabled() ) activity.exercisesLv.setEnabled( false );

                actionBarText =  "Sets chosen: ";
                activity.currLv = activity.setsLv;
                activity.currAdapter = activity.setsLogAdapter;
                break;
            default:
                break;
        }

        activity.currAdapter.invertCtxChecked(index);
        activity.currAdapter.notifyDataSetChanged();

        //if all items deselected
        int checkedAmount = activity.currAdapter.getcheckedAmount();
        if ( checkedAmount == 0 ) {

            actMode.finish();

        } else if ( checkedAmount == 1 ) {

            actMode.getMenu().getItem( 0 ).setVisible( true ); //edit btn

            if ( this.contextSubj == WorkoutDataAdapter.Subject.EXERCISES ) {
                actMode.getMenu().getItem(1).setVisible(true); // delete exercise btn
            } else {
                actMode.getMenu().getItem( 1 ).setVisible( false );
            }

        } else {
            actMode.getMenu().getItem( 0 ).setVisible( false );
            actMode.getMenu().getItem( 1 ).setVisible( false );
        }

        actionBarText += activity.currAdapter.getcheckedAmount();
        actMode.setTitle( actionBarText );
    }

    public void onDeleteExPressed() {
        Log.v(APP_NAME, "WJContext :: onDeleteExPressed");
    }

    public void onEditRenamePressed() {
        Log.v(APP_NAME, "WJContext :: onEditRenamePressed");

        if ( activity.currAdapter.getcheckedAmount() != 1)
        {
            Log.v(APP_NAME, "WJContext :: onEditRenamePressed one ckecked item expected, while there are more");
        }

        HashSet<Integer> onlyItem = activity.currAdapter.getIdsOfCtxChecked();

    }

    public void onDeleteLogEntriesPressed() {
        Log.v(APP_NAME, "WJContext :: onDeleteLogEntriesPressed");

        HashSet<Integer> ids = activity.exerciseLogAdapter.getIdsOfCtxChecked();

        int affectedSetEntries = 0;
        int affectedExEntries  = 0;
        Cursor entry;
        for ( Integer id : ids )
        {
            Log.v(APP_NAME, "WJContext :: following checked ID of item in list view to delete: "+id);
            entry = (Cursor)activity.exercisesLv.getItemAtPosition( id );

            affectedSetEntries += activity.dbmediator.rmExLogEntry( entry );
            affectedExEntries++ ;
        }

        int newMaxIdx = activity.setsLogAdapter.getCount() - affectedSetEntries - 1;
        if ( activity.setsLogAdapter.getIdxOfCurrent() > newMaxIdx ) activity.setsLogAdapter.setIdxOfCurrent( newMaxIdx );

        newMaxIdx = activity.exerciseLogAdapter.getCount() - affectedExEntries - 1;
        if ( activity.exerciseLogAdapter.getIdxOfCurrent() > newMaxIdx ) activity.exerciseLogAdapter.setIdxOfCurrent( newMaxIdx );

        if ( affectedSetEntries > 0 ) activity.initiateListUpdate(WorkoutDataAdapter.Subject.SETS , WorkoutJournal.TriggerEvent.DELETE);
        if ( affectedExEntries > 0 ) activity.initiateListUpdate(WorkoutDataAdapter.Subject.EXERCISES , WorkoutJournal.TriggerEvent.DELETE);

    }

    public void onCancelEditBtnPressed() {
        Log.v(APP_NAME, "WJContext :: onCancelEditBtnPressed");

        ctxCancelBtn.setVisibility(View.GONE);
        ctxAddBtn.setVisibility(View.GONE);
        ctxEditRepsField.setVisibility(View.GONE);
        ctxEditWeightField.setVisibility(View.GONE);
        ctxEditExField.setVisibility(View.GONE);

        ctxDeleteLogBtn.setVisibility(View.VISIBLE);

        ctxEditWeightField.setText("");
        ctxEditRepsField.setText("");
        ctxEditExField.setText("");

    }

    public void onAddEditedBtnPressed() {
        Log.v(APP_NAME, "WJContext :: onAddEditedBtnPressed");
    }

    private void deleteSelectedExercise() {

        Integer sequenceNumber = (Integer)activity.exerciseLogAdapter.getIdsOfCtxChecked().toArray()[0];
        activity.dbmediator.deleteEx( (Cursor)activity.exercisesLv.getItemAtPosition( sequenceNumber ) );

        switch ( activity.adjustAfterExDeleted( sequenceNumber ) ) {
            case 0: // no need to change anything (will focus on the next element got this idx)
                //activity.exerciseLogAdapter.invertCtxChecked( sequenceNumber );
                break;

            case 1: // first was deleted
                //activity.exerciseLogAdapter.invertCtxChecked( sequenceNumber );

                break;

            case 2: // need to move invert CtxCheckedselected lower
                activity.exerciseLogAdapter.invertCtxChecked( sequenceNumber - 1); //select
                activity.exerciseLogAdapter.invertCtxChecked( sequenceNumber ); //deselect
                break;

            case 3: // no items left
            default:
                Log.v(APP_NAME, "WJContext :: deleteSelectedExercise: exiting after no items left or unexpected return from deleteSelectedExercise()" );

                thisActionMode.finish();
                return;
        }

        //activity.exerciseLogAdapter.changeCursor(activity.allExCursor);
        //activity.currAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.v(APP_NAME, "WJContext :: onClick of alert dialog pressed. dialog: "+dialog+ " which: "+which );

        switch ( which )  {
            case -1: // Delete button
                deleteSelectedExercise();
                break;

            case -2: // Cancel button
                break;
        }

    }
}





