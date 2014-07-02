package com.gk.datacontrol;

import java.util.Random;

import android.util.Log;

public class DebugHelper {
	private  DBClass mydb;
	public static final String APP_NAME = "SWJournal";
    String[] exercisesArray = { "Pull ups", "Push ups", "Exercise X","Drinking Vodka", "Barbell Incline Bench Press", "Butterfly", "Leverage Shrug",
  	      "Barbell Full Squat", "Barbell Curl", "Masturbation", "Alternate Hammer Curl", "Barbell Bench Press"};
    String chars = "qwertyuiopasdfghjklzxcvbnm. QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
    
    
    public DebugHelper( DBClass mydb ) {
    	this.mydb = mydb;
    }
    
	public static String generateString(Random rng, String characters, int length)
	{
	    char[] text = new char[length];
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
	}
	
	public void fillData() {
		Log.v(APP_NAME, "DebugHelper :: fillData(TrainingData td)");
        Random rng = new Random();
        int noteLen;
        long time;
        int repsVal;
        float weightVal;
        final int maxNoteLen = 256;
        final int minNoteLen = 10;
        final int maxRepsCount = 400;
        final int minRepsCount = 1;
        final int maxSetsVal = 20; // TODO: how many can be supported?
        final int minSetsVal = 0;
        final float maxWeightVal=  (float)365.0;
        final float minWeightVal = (float)0.5;
        String note;
        boolean needNote = true;
        mydb.open();
        
        Log.v(APP_NAME, "DebugHelper :: fillData :: filling exercises");
        
        for ( String exercise : exercisesArray) {
        	noteLen = rng.nextInt((maxNoteLen - minNoteLen) + 1) + minNoteLen;
        	if (needNote) {
        		note = generateString(rng, chars, noteLen);
        		needNote = false;
        	} else {
        		note = "";
        		needNote = true;
        	}
        	mydb.insertExerciseDebug(exercise,  note);
        }
        
        Log.v(APP_NAME, "DebugHelper :: fillData :: filling sets");
        for ( String exercise : exercisesArray) {
        	for ( int i = 0 ; i <= rng.nextInt((maxSetsVal - minSetsVal) + 1) + minSetsVal ; i++ ) {
	        	noteLen = rng.nextInt((maxNoteLen - minNoteLen) + 1) + minNoteLen;
	        	repsVal = rng.nextInt((maxRepsCount - minRepsCount) + 1) + minRepsCount;
	        	weightVal = rng.nextFloat() * (maxWeightVal - minWeightVal) + minWeightVal;
	        	time = System.currentTimeMillis();
	        	if (i == 5)  time += 24*60*60000;
	        	mydb.insertSet(exercise, generateString(rng, chars, noteLen), repsVal, ((float)Math.round(weightVal * 100) / 100 ), time);
        	}
        }
        
        mydb.close();
    }
}
