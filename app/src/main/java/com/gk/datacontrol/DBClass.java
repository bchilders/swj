package com.gk.datacontrol;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBClass  {
	private int exercisesInDay = 0, exerciseDays = 0, setsInDay = 0, setDays = 0; // used for simulating date change situation\
	public static final  String APP_NAME = "SWJournal";
	
	private static final String DB_NAME = "SWJournal";
	private static final int    DB_VERSION = 1;
	public static final long MS_IN_A_DAY = 86400000;
	
	private static final String TABLE_EXERCISES = "exercises";
	private static final String TABLE_SETS_LOG = "sets_log";
	public  static final String TABLE_EXERCISE_LOG 	= "exercise_log";

	public  static final String KEY_ID 			= "_id";
    public  static final String KEY_EX_LOG_ID   = "ex_log_id";
	public  static final String KEY_NAME		= "name";
	public  static final String KEY_NOTE 		= "note";
	public  static final String KEY_EX_NAME  	= "exercise_name";
	public  static final String KEY_TIME 		= "time";
	public  static final String KEY_REPS    	= "reps";
	public  static final String KEY_WEIGHT  	= "weight";
	
	private static final String CREATE_EXERCISES_TABLE = "CREATE TABLE "+ TABLE_EXERCISES +" ("+
														  KEY_NAME + " TEXT PRIMARY KEY," +
														  KEY_NOTE + " TEXT"+
														  ");";
	
	private static final String CREATE_EXERCISE_LOG_TABLE = "CREATE TABLE "+TABLE_EXERCISE_LOG+" ("+
			KEY_ID      	   + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			KEY_EX_NAME  	   + " TEXT,"  +
		    KEY_TIME           + " INTEGER,"+
            KEY_NOTE           + " TEXT," +
			"FOREIGN KEY ("+KEY_EX_NAME+") REFERENCES "+ TABLE_EXERCISES +"("+KEY_NAME+")"+
			");";

    private static final String CREATE_SETS_LOG_TABLE = "CREATE TABLE "+ TABLE_SETS_LOG +" ("+
            KEY_ID	   + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_TIME   + " INTEGER,"+
            KEY_REPS   + " INTEGER," +
            KEY_WEIGHT + " REAL," +
            KEY_NOTE   + " TEXT,"+
            KEY_EX_LOG_ID + " INTEGER,"+
            KEY_EX_NAME + " INTEGER,"+
            "FOREIGN KEY ("+ KEY_EX_LOG_ID +") REFERENCES "+ TABLE_EXERCISE_LOG +"("+KEY_ID+"),"+
            "FOREIGN KEY ("+ KEY_EX_NAME +") REFERENCES "+ TABLE_EXERCISES +"("+KEY_NAME+")"+
            ");";

    private SQLiteDatabase realdb;
	private DBHelper dbHelper;
	private ContentValues values;
	
	public DBClass(Context context) {
		dbHelper = new DBHelper(context);
		values =  new ContentValues();
        open();
		Log.v(APP_NAME, "DBClass :: DBHelper(Context context");
		
	}


	public void close() {
		if (dbHelper!=null) dbHelper.close();
	}
	
	public void open() {
		realdb = dbHelper.getWritableDatabase();
		realdb.execSQL("PRAGMA foreign_keys=ON;");
	}
	
	public String millisToDate( long milliTime ) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
        calendar.setTimeInMillis(milliTime);
		return sdf.format(calendar.getTime());
	}
	
	public void dumpDBtoLog() {
		Log.v(APP_NAME, "DBClass :: dumpDBtoLog() ");

		Cursor exerciseCursor = realdb.query(TABLE_EXERCISES, null, null, null, null, null, null);

		Cursor setsCursor;
		String selection = KEY_EX_NAME + " = ?" ;
		String selectionArgs[];
		if ( exerciseCursor.moveToFirst() ) {
			int exerciseNameIndex = exerciseCursor.getColumnIndex(KEY_NAME);
			int exerciseNoteIndex = exerciseCursor.getColumnIndex(KEY_NOTE);
			do {
				Log.v(APP_NAME, "Exercise name: "+ exerciseCursor.getString(exerciseNameIndex));
				Log.v(APP_NAME, "Exercise note: "+ exerciseCursor.getString(exerciseNoteIndex));
				selectionArgs = new String[] { exerciseCursor.getString(exerciseNameIndex) };
				setsCursor = realdb.query(TABLE_SETS_LOG, null, selection, selectionArgs, null , null, null);
				
				if ( setsCursor.moveToFirst() ) {
					int setNameIndex = setsCursor.getColumnIndex(KEY_EX_NAME);
					int setTimeIndex = setsCursor.getColumnIndex(KEY_TIME);
					int setNoteIndex = setsCursor.getColumnIndex(KEY_NOTE);
					int setRepsIndex = setsCursor.getColumnIndex(KEY_REPS);
					int setWeightIndex = setsCursor.getColumnIndex(KEY_WEIGHT);
					
					do {
						Log.v(APP_NAME, "    Set exercise name: "+ exerciseCursor.getString(setNameIndex));
						Log.v(APP_NAME, "    Set note: "+ exerciseCursor.getString(setTimeIndex));
						Log.v(APP_NAME, "    Set name: "+ exerciseCursor.getString(setNoteIndex));
						Log.v(APP_NAME, "    Set reps: "+ exerciseCursor.getString(setRepsIndex));
						Log.v(APP_NAME, "    Set weight: "+ exerciseCursor.getString(setWeightIndex));
						Log.v(APP_NAME, "");
					} while ( setsCursor.moveToNext() );
				} else {
					Log.v(APP_NAME, "    No sets for this exercise");
				}
				Log.v(APP_NAME, "");
			} while ( exerciseCursor.moveToNext() );
		} else {
			Log.v(APP_NAME, "No exercises in DB");
		}
        close();
	}


    /*
     * Will delete all logs related to exercise and exercise itself
     */
    public int deleteEx(String exName) {
        Log.e(APP_NAME, "DBClass :: OBSOLETE :: deleteEx :: exName: "+ exName);


        int affectedSum = realdb.delete(TABLE_SETS_LOG,     KEY_EX_NAME + " = '" + exName + "'", null);
        affectedSum    += realdb.delete(TABLE_EXERCISE_LOG, KEY_EX_NAME + " = '" + exName + "'", null);
        affectedSum    += realdb.delete(TABLE_EXERCISES,    KEY_NAME    + " = '" + exName + "'", null);
        close();

        Log.v(APP_NAME, "DBClass :: deleteEx :: sum of deleted: "+ affectedSum );
        return affectedSum;
    }

    public int deleteEx( Cursor exCursor ) {
        Log.v(APP_NAME, "DBClass :: deleteEx started");

        String exToDelete = exCursor.getString( exCursor.getColumnIndex("exercise_name") );

        int affectedSum = realdb.delete(TABLE_SETS_LOG,     KEY_EX_NAME + " = '" + exToDelete + "'", null);
        affectedSum    += realdb.delete(TABLE_EXERCISE_LOG, KEY_EX_NAME + " = '" + exToDelete + "'", null);
        affectedSum    += realdb.delete(TABLE_EXERCISES,    KEY_NAME    + " = '" + exToDelete + "'", null);
        close();

        Log.v(APP_NAME, "DBClass :: deleteEx :: sum of deleted: "+ affectedSum );
        return affectedSum;

    }

    public int rmExLogEntry( Cursor exLogEntry ) {
        Log.v(APP_NAME, "DBClass :: rmExLogEntry started" );


        Long exLogId = exLogEntry.getLong( exLogEntry.getColumnIndex( KEY_ID ) );

        int affectedRows = realdb.delete(TABLE_SETS_LOG,  KEY_EX_LOG_ID +" = "+exLogId, null);
        affectedRows += realdb.delete(TABLE_EXERCISE_LOG, KEY_ID        +" = "+exLogId, null);

        Log.v(APP_NAME, "DBClass :: rmExLogEntry :: affected rows: "+ affectedRows );
        return affectedRows;
    }

    //OBSOLETE
	public long insertExerciseNote( String exercise, String newNote )  {
		values.put( KEY_NOTE, newNote  );

		long res = realdb.update(TABLE_EXERCISES, values, KEY_NAME + "=\"" + exercise +"\"" , null);

		if (res != 1) {
			Log.e(APP_NAME, "DBClass :: insertNote for exercise :: failed. (name: "+exercise+")" );
		} else {
			Log.v(APP_NAME, "DBClass :: insertNote for exercise :: success for exercise "+exercise);
		}
		values.clear();
		return res;
	}

    public long insertExerciseNote( Cursor exCursor, String newNote ) {
        Log.v(APP_NAME, "DBClass :: insertExerciseNote started" );

        values.put( KEY_NOTE, newNote  );

        String exName = exCursor.getString(exCursor.getColumnIndex(DBClass.KEY_EX_NAME));

        long res = realdb.update(TABLE_EXERCISES, values, KEY_NAME + "=\"" + exName +"\"" , null);

        if (res != 1) {
            Log.e(APP_NAME, "DBClass :: insertExerciseNote:: failed. (name: "+exName+")" );
        } else {
            Log.v(APP_NAME, "DBClass :: insertExerciseNote:: success for exercise "+exName);
        }
        values.clear();
        return res;
    }
    //OBSOLETE
	public long insertSetNote( long setId, String newNote )  {
		values.put( KEY_NOTE, newNote );

		long res = realdb.update(TABLE_SETS_LOG, values, KEY_ID + "=" + setId , null);

		if (res != 1) {
			Log.e(APP_NAME, "DBClass :: OBSOLETE :: insertNote for set :: failed. (id: "+setId+")" );
		} else {
			Log.e(APP_NAME, "DBClass :: OBSOLETE :: insertNote for set :: success for set with id "+setId);
		}
		values.clear();
		return res;
	}

    public long insertSetNote( Cursor setCursor, String newNote ) {
        Log.v(APP_NAME, "DBClass :: insertSetNote started");
        values.put(KEY_NOTE, newNote);


        long setId = setCursor.getLong(setCursor.getColumnIndex(DBClass.KEY_ID));
        Log.v(APP_NAME, "DBClass :: insertSetNote :: gonna insert note '" + newNote + "' for set id " + setId);
        long res = realdb.update(TABLE_SETS_LOG, values, KEY_ID + "=" + setId , null);



        if (res != 1) {
            Log.e(APP_NAME, "DBClass :: insertSetNote for set :: failed. (id: "+setId+")" );
        } else {
            Log.v(APP_NAME, "DBClass :: insertSetNote for set :: success for set with id "+setId);
        }

        values.clear();
        return res;

    }
	public long insertSet( String exName, long exLogId, int reps, float weight) {
		 long time = System.currentTimeMillis();
		 if (setsInDay % 3 == 0) {
			 setDays++;
			 setsInDay = 0;
		 }		  
		 time += (MS_IN_A_DAY * setDays); // number - ms in day
		 setsInDay++;
		 
		return insertSet( exName, exLogId, null, reps, weight, time );
	}
	
	public long insertSet( String exName, long exLogId, String setNote, int reps, float weight, long time) {
        Log.v(APP_NAME, "DBClass :: insertSet :: exName:  "+ exName);
        Log.v(APP_NAME, "DBClass :: insertSet :: exLogId: "+ exLogId);
		Log.v(APP_NAME, "DBClass :: insertSet :: setNote: "+ setNote);
		Log.v(APP_NAME, "DBClass :: insertSet :: reps: "+ reps);
		Log.v(APP_NAME, "DBClass :: insertSet :: weight: "+ weight);
		Log.v(APP_NAME, "DBClass :: insertSet :: time: "+ millisToDate(time) );

        values.put(KEY_EX_NAME, exName);
    	values.put(KEY_EX_LOG_ID, exLogId);
    	values.put(KEY_NOTE, setNote);
    	values.put(KEY_REPS, reps );
    	values.put(KEY_WEIGHT,  weight);
    	values.put(KEY_TIME, time);


		long res = realdb.insert(TABLE_SETS_LOG, null, values);

		values.clear();
		if (res == -1) {
			Log.e(APP_NAME, "DBClass :: insertSet :: failed. (exName: "+exName+
                                                                "exLogId: "+exLogId+
																"; time: "+millisToDate(time)+
																"; reps: "+reps+
																"; weight: "+weight+
																")" );
		} else {
			Log.v(APP_NAME, "DBClass :: insertSet :: success");
		}
		return res;
	}


	 public Cursor fetchAllExercies() {
		 Log.v(APP_NAME, "DBClass :: fetchAllExercies begin");

		 Cursor mCursor = realdb.rawQuery("SELECT "+KEY_NAME+" AS _id, "+KEY_NOTE+" FROM "+ TABLE_EXERCISES, null);

		 if (mCursor != null) {
		  mCursor.moveToFirst();
		 }
		 Log.v(APP_NAME, "DBClass :: fetchAllExercies complete");
		 return mCursor;
	 }

    //TODO: OBSOLETE, should change to one with Cursor
	 public Cursor fetchSetsForExercise( String exerciseName ) {

		 Log.e(APP_NAME, "DBClass :: OBSOLETE FUNCTION CALLED :: fetchSetsForExercise for "+exerciseName);

         Cursor setsCursor = realdb.rawQuery("SELECT * FROM "+ TABLE_SETS_LOG +
				 							 " WHERE "+KEY_EX_NAME+" = '"+exerciseName+"' ORDER BY "+KEY_TIME, null );



		 Log.v(APP_NAME, "DBClass :: fetchSetsForExercise for '"+exerciseName+"' complete.");
		 return setsCursor;
	 }

    public Cursor fetchSetsForExercise( Cursor currExercise ) {
//  if troubles:      Cursor tmpcs = (Cursor) exerciseLogAdapter.getItem( exerciseLogAdapter.getCurrent() );

        Log.v(APP_NAME, "DBClass :: fetchSetsForExercise");

        Cursor setsCursor = null;

        if  ( currExercise != null && currExercise.getCount() != 0) {
            String exerciseName = currExercise.getString(currExercise.getColumnIndex(KEY_EX_NAME));

            Log.v(APP_NAME, "DBClass :: fetchSetsForExercise :: exercise: " + exerciseName);

            setsCursor = realdb.rawQuery("SELECT * FROM " + TABLE_SETS_LOG +
                    " WHERE " + KEY_EX_NAME + " = '" + exerciseName + "' ORDER BY " + KEY_TIME, null);

            setsCursor.moveToFirst();


            Log.v(APP_NAME, "DBClass :: fetchSetsForExercise for '" + exerciseName + "' complete.");
        }

        return setsCursor;
    }

    //OBSLETE
     public String getNoteForEx( String exName ) {

         Cursor cursor = realdb.rawQuery("SELECT " + KEY_NOTE +" FROM "+ TABLE_EXERCISES + " WHERE " + KEY_NAME + " = '" + exName +"'", null );


         if (cursor != null) {
             cursor.moveToFirst();
         }

         return cursor.getString( cursor.getColumnIndex( KEY_NOTE ) );
     }

    public String getNoteForEx( Cursor exCursor ) {
        Log.v(APP_NAME, "DBClass :: getKeyNoteFoEx begin");
        String note = "";

        if ( exCursor != null && exCursor.getCount() > 0 ) {

            String exName = exCursor.getString( exCursor.getColumnIndex(DBClass.KEY_EX_NAME) );
            Log.v(APP_NAME, "DBClass :: getKeyNoteFoEx :: exercise : " + exName);
            Cursor noteCursor = realdb.rawQuery("SELECT " + KEY_NOTE + " FROM " + TABLE_EXERCISES + " WHERE " + KEY_NAME + " = '" + exName + "'", null);

            if ( noteCursor != null ) {
                noteCursor.moveToFirst();
                note = noteCursor.getString(noteCursor.getColumnIndex(KEY_NOTE));
            }

        }

        return note;
    }

	 public Cursor fetchExerciseHistory() {
		 Log.v(APP_NAME, "DBClass :: fetchExerciseHistory begin");


         Cursor mCursor = realdb.rawQuery( "SELECT " +KEY_ID +"," + KEY_EX_NAME + "," + KEY_TIME + "," + TABLE_EXERCISES+"."+KEY_NOTE + " FROM " + TABLE_EXERCISE_LOG +
                 " LEFT OUTER JOIN "+TABLE_EXERCISES+" ON "
                 +TABLE_EXERCISE_LOG+ "." +KEY_EX_NAME+ " = "+TABLE_EXERCISES+"."+KEY_NAME +
                 "  ORDER BY " + KEY_TIME + " ASC", null);

		 if (mCursor != null) {
			 mCursor.moveToFirst();
		 }
		 Log.v(APP_NAME, "DBClass :: fetchExerciseHistory complete");

		 return mCursor;
	 }
	 
	 public boolean addExercise(String exercise) {
		 Log.v(APP_NAME, "DBClass :: addExercise for '"+exercise+"'");
		 long result = -1;
		 values.put(KEY_NAME, exercise);
		 
		 //check if there already exist an exercise like this

		 Cursor tmpcs = realdb.rawQuery("SELECT "+KEY_NAME+" FROM "+ TABLE_EXERCISES + " WHERE "+KEY_NAME+ " = \"" + exercise + "\"", null );


		 if (tmpcs.getCount() == 0)
			 result = realdb.insert(TABLE_EXERCISES, null, values);

		 values.clear();
		 Log.v(APP_NAME, "DBClass :: addExercise done");
		 return (result != -1);
	 }
	 
	 public boolean logExercise(String exercise) {
		 
		 long time = System.currentTimeMillis();
		 if (exercisesInDay % 3 == 0) {
			 exerciseDays++;
			 exercisesInDay = 0;
		 }		  
		 time += (MS_IN_A_DAY * exerciseDays); // number - ms in day
		 exercisesInDay++;
		 
		 return logExercise(exercise, time);
	 }

	 public boolean logExercise(String exercise, long time ) {
		 Log.v(APP_NAME, "DBClass :: logExercise begin for '"+exercise+"', time "+millisToDate( time ) );
		 
		 //we use full timestamp
		 values.put(KEY_EX_NAME, exercise);
		 values.put(KEY_TIME, time);
		 //DEV-ONLY
		 
		 //values.put(KEY_TIME, getUnixDay() );

		 long result = realdb.insert(TABLE_EXERCISE_LOG, null, values);

		 values.clear();
		 Log.v(APP_NAME, "DBClass :: logExercise done");
		 return (result != -1) ? true : false;
		 
	 }
	 
	  private class DBHelper extends SQLiteOpenHelper {
	
		    public DBHelper(Context context) {
		      super(context, DB_NAME, null, DB_VERSION);
		      //DEV-ONLY
		      context.deleteDatabase(DB_NAME);
		    }
	
		    @Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL(CREATE_EXERCISES_TABLE);
				db.execSQL(CREATE_SETS_LOG_TABLE);
				Log.v(APP_NAME, "DBClass :: onCreate :: sets table created");
				db.execSQL(CREATE_EXERCISE_LOG_TABLE);
				Log.v(APP_NAME, "DBClass :: onCreate :: exersises log table created");
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				// TODO Auto-generated method stub
				
			}
	  }
	  
	  public long getUnixDay() {
		  long now = System.currentTimeMillis();
		  return now  - (now % MS_IN_A_DAY) ;
	  }
		    
}
