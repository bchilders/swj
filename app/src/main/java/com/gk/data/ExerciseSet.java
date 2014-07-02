package com.gk.data;

import java.util.Calendar;

public class ExerciseSet {
	private byte repeations;
	private float weight;
	private String note;
	private Calendar time;
	
	public ExerciseSet(byte reps, float weight, String note, Calendar time) {
		this.time = time;
		this.repeations = reps;
		this.weight = weight;
		this.note = note;
	}
	
	public byte getReps() { return this.repeations; }
	public float getWeight() { return this.weight; }
	public Calendar getTime() { return this.time; }
	public String getNote() { return this.note; }
	public Calendar getDate() { return this.time; } //TODO: return date only (no hours and minutes) 
	public boolean setNote( String note) { this.note = note; return true; }
	public boolean setWeight( float weight ) {this.weight = weight;return true;  }
	public boolean setTime( Calendar time) { this.time = time; return true; }
	
}
