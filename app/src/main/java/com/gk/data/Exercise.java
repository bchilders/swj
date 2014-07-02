package com.gk.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Exercise  {
	private String name;
	private String note;
	private List< ExerciseSet > sets = new ArrayList< ExerciseSet >();
	
	public Exercise( String name, String note) {
		this.name = name;
		this.note = note;
	}
	
	public String getName() { return this.name; }
	public String getNote() { return this.note; }
	public List< ExerciseSet > getSets() { 
		return this.sets;
	}
	
	public boolean setName( String name) { 
		//check length?
		this.name = name; 
		return true; 
	}

	public boolean setNote(String note) { 
		//check length?
		this.note = note; 
		return true; 
	}
	
	public boolean addSet( byte reps, float weight, String note,  Calendar time ) {
		sets.add(new ExerciseSet( reps, weight, note, time) );
		return true; 
	}
	
	public boolean removeSet( ExerciseSet set ) { 
		sets.remove( set );
		return true;
	}

	public boolean removeSet( Calendar time ) { 
		for ( ExerciseSet set : sets) {
			if ( time == set.getTime() ) {
				sets.remove( set );
				return true;
			}
		}
		return false;
	}
	
	public String[] getSetStringArray() {
		String[] resultArr = new String[ sets.size() ];
		for ( int i = 0; i < sets.size(); i++) {
			resultArr[i] = sets.get( i ).getReps() + " : " + sets.get( i ).getWeight();
		}
		return resultArr;
	}
	
	public int getSetsAmount() {
		return sets.size();
	}
	
	public List< ExerciseSet > getSetsList() {
		return sets;
	}
	
	public String toString() { return name; }
}
