package com.gk.simpleworkoutjournal;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gk.datacontrol.DBClass;

public class WorkoutDataAdapter extends CursorAdapter {
	public static final String APP_NAME = "SWJournal";
	
	public enum Subject { EXERCISES, SETS };
	private Subject currSubj;
	private String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	private TextView exerciseTv, repsTv, weightTv;
	private ImageView img;
	private TextView dateHeader;
	private long prevTimestamp, timestamp;
	private Calendar formattedDate = Calendar.getInstance();
	private int currentIndex = -1;
	private String dateString;
	private boolean showDate;
	
	public WorkoutDataAdapter(Context context, Cursor c, Subject subject) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );
		currSubj = subject;
		
	}
	
	public Subject getSubject()
	{
		return this.currSubj;
	}
	
	@Override
	public void bindView(View view, Context ctx, Cursor cursor) {
		
		img = (ImageView) view.findViewById(R.id.workout_entry_note_image);
		dateHeader = (TextView)view.findViewById(R.id.workout_entry_date_header);
        
	    String noteString = cursor.isNull(cursor.getColumnIndex(DBClass.KEY_NOTE)) ? 
	 			   "" : cursor.getString(cursor.getColumnIndex(DBClass.KEY_NOTE));	
	    String entryMainText = "";
	    
	    timestamp = cursor.getLong( cursor.getColumnIndex(DBClass.KEY_TIME) );
	    formattedDate.setTimeInMillis( timestamp );
	    
	    // check if we need to highlight current position
	    if (cursor.getPosition() == currentIndex) {
	    	view.setBackgroundColor( ctx.getResources().getColor(R.color.baseColor_lightest) );
	    } else {
	    	view.setBackgroundColor(Color.WHITE);
	    }
	    
	    // check if we need to show note image
	    if (noteString.isEmpty()) {
	    	img.setVisibility(View.INVISIBLE);
	    } else {
	    	img.setVisibility(View.VISIBLE);
	    }
	    
	    // take data required 
	    if ( cursor.isFirst()) {
	    	dateHeader.setVisibility(View.VISIBLE);
	    	showDate = true;
	    } else {
	        cursor.moveToPrevious();
	        prevTimestamp = cursor.getLong( cursor.getColumnIndex(DBClass.KEY_TIME) );
	        cursor.moveToNext();
	        showDate = (compareDates(timestamp, prevTimestamp) == 1);
	    }
	       
	    switch (currSubj) {
       		case EXERCISES:
       			exerciseTv = (TextView) view.findViewById(R.id.workout_entry_textview);
       			entryMainText = cursor.getString(cursor.getColumnIndex("_id"));
       			exerciseTv.setText(entryMainText);
       			
       			// we need to show date header if previous entry have different date
    	        if ( showDate ) {
    	        	dateHeader.setVisibility(View.VISIBLE);
           		    dateString = formattedDate.get(Calendar.DAY_OF_MONTH)  +" "+
      		    			 monthNames[formattedDate.get(Calendar.MONTH)] +" "+
      		    			 formattedDate.get(Calendar.YEAR)+"  ";
    	        } else { //TODO: date format
    	        	dateHeader.setVisibility(View.INVISIBLE);
    	        }

	            Log.v(APP_NAME, "bindView :: bindView exercise. name: "+entryMainText);
       			break;
       		case SETS:
       			Log.v(APP_NAME, "entry of sets type binding started");
	       		int setReps = cursor.getInt(cursor.getColumnIndex(DBClass.KEY_REPS));
	       		float setWeight = cursor.getFloat(cursor.getColumnIndex(DBClass.KEY_WEIGHT));
	       		
	       		repsTv = (TextView) view.findViewById(R.id.workout_reps_textview);
	       		repsTv.setText(String.valueOf(setReps));
	       		weightTv = (TextView) view.findViewById(R.id.workout_weight_textview);
	       		weightTv.setText(String.valueOf(setWeight));
	       		
      			// we need to always show date for sets. Add date if it is not as in previous entry
	       		dateString = "";
	       		dateHeader.setVisibility(View.VISIBLE);
	       		if ( showDate ) {
           		    dateString = formattedDate.get(Calendar.DAY_OF_MONTH)  +" "+
      		    			 monthNames[formattedDate.get(Calendar.MONTH)] +" "+
      		    			 formattedDate.get(Calendar.YEAR) + "   ";
    	        } 
    	        dateString += String.format("%02d:%02d", formattedDate.get(Calendar.HOUR_OF_DAY), formattedDate.get(Calendar.MINUTE));//formattedDate.get(Calendar.HOUR_OF_DAY) + ":" + formattedDate.get(Calendar.);
       			break;
       		default:
       			Log.e(APP_NAME, "Unrecognized table type");
	    }
       
	   dateHeader.setText(dateString);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater infl = LayoutInflater.from(context);
        View root;
        switch (currSubj) {
        	case EXERCISES:
        		root = infl.inflate(R.layout.exercise_entry, parent, false);
        		break;
        	default: //use for SETS default case to avoid error related to non-initialized item
        		root = infl.inflate(R.layout.set_entry, parent, false);
        }
        return root;
	}
	
	public void setCurrent(int position) {
		Log.v(APP_NAME, "setCurrent:  new current for subj "+currSubj.toString()+": "+position);
		currentIndex = position;
	}
	
	public int getCurrent() { return currentIndex; }
	
	/*
	 *  1 if 1st  > 2nd
	 * -1 if 1st  < 2nd
	 *  o if 1st == 2nd
	 */
	public int compareDates(long unixTime1, long unixTime2) {
		//trim hours/minutes/seconds
		unixTime1 -= (unixTime1 % DBClass.MS_IN_A_DAY);
		unixTime2 -= (unixTime2 % DBClass.MS_IN_A_DAY);
		if (unixTime1 > unixTime2) {
			return 1;
		} else if (unixTime1 < unixTime2) {
			return -1;
		} else {
			return 0;
		}
	}
}
