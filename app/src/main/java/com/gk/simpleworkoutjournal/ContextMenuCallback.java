package com.gk.simpleworkoutjournal;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;

import com.gk.datacontrol.DBClass;

enum Subject { EXERCISE, SET };

public class ContextMenuCallback implements MultiChoiceModeListener {
    public enum Subject { EXERCISES, SETS };
    private Subject subject;
	private Cursor activeCursor;
	private int id;
	private boolean isActive;
	private DBClass dbmediator;
	
	public void setData( Subject subj, int id ) {
		this.isActive = true;
		this.subject = subj;
		this.id = id;
	}
	
	@Override
	public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
		Log.e("CMcallback", "ContextMenuCallback :: onActionItemClicked mode: "+arg0+" item: "+arg1);
		// TODO Auto-generated method stub
		arg0.getTag();
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		Log.e("CMcallback", "ContextMenuCallback :: onCreateActionMode mode: "+mode+" menu: "+menu);
	    MenuInflater inflater = mode.getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
		// TODO Auto-generated method stub
		return true;
	}
	
	public ContextMenuCallback( Cursor associatedCursor, DBClass dbmediator, Subject subj ) {
		// TODO Auto-generated method stub
		super();
        this.subject = subj;
		this.dbmediator = dbmediator;
		this.activeCursor = associatedCursor;
		if ( activeCursor != null)
		{
			Log.v("CMcallback", "Created context menu callback "+subj);
		}
	}
	
	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		Log.e("LOL", "ContextMenuCallback :: onDestroyActionMode mode: "+arg0);
		isActive = false;
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		Log.e("LOL", "ContextMenuCallback :: onPrepareActionMode mode: "+arg0+ " menu: "+arg1);
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode arg0, int index, long arg2,
			boolean arg3) {
		// TODO Auto-generated method stub
		if ( activeCursor == null )
		{
			Log.e("LOL", "ContextMenuCallback::onItemCheckedStateChanged trying to operate on context while no adapter set");
			return;
		}
		Log.v("LOL"," dumping after onItemCheckedStateChanged");
		DatabaseUtils.dumpCursor(activeCursor);
		activeCursor.moveToPosition( index );
		//Log.v("LOL", "Got exercise: "+ activeCursor.getString(activeCursor.getColumnIndex(DBClass.KEY_EX_NAME)) );
		Log.e("LOL", "ContextMenuCallback :: onItemCheckedStateChanged mode: "+arg0+" int: "+index+ " long "+arg2+" bool: "+arg3);
		
		
	}

}
