package com.gk.reports;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.gk.datacontrol.DBClass;
import com.gk.simpleworkoutjournal.R;

/**
 * Created by George on 19.04.2015.
 */
public class ReportConfigurator extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    final String APP_NAME = "SWJournal";
    final boolean DEBUG_FLAG = true;
    DBClass dbmediator;
    SimpleCursorAdapter exChooserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_FLAG) Log.v(APP_NAME, "NotesDialog :: creating NotesDialog");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_configurator);

        Spinner periodChooser = (Spinner) findViewById( R.id.periodChooser );
        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource( this, R.array.periods, R.layout.period_chooser_spinner );
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodChooser.setAdapter(periodAdapter);

        Spinner wPointChooser = (Spinner) findViewById( R.id.weightPointChooser );
        ArrayAdapter<CharSequence> wPointAdapter= ArrayAdapter.createFromResource( this, R.array.points, R.layout.period_chooser_spinner );
        wPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wPointChooser.setAdapter(wPointAdapter);

        Spinner rPointChooser = (Spinner) findViewById( R.id.repsPointChooser );
        ArrayAdapter<CharSequence> rPointAdapter= ArrayAdapter.createFromResource( this, R.array.points, R.layout.period_chooser_spinner );
        rPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rPointChooser.setAdapter(rPointAdapter);

        Spinner exChooser = (Spinner) findViewById( R.id.exerciseChooser );

        String[] from = new String[] { DBClass.KEY_ID };
        int[] to = new int[] { R.id.periodChooserItem };

        dbmediator  = new DBClass(this);
        exChooserAdapter = new SimpleCursorAdapter(this, R.layout.period_chooser_spinner, null, from, to, 0);
        //exChooserAdapter.setDropDownViewResource(android.R.layout.two_line_list_item);
        exChooser.setAdapter(exChooserAdapter);

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new MyCursorLoader(this, dbmediator);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        exChooserAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    static class MyCursorLoader extends CursorLoader {
        DBClass db;

        public MyCursorLoader(Context context, DBClass db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            return db.fetchExerciseNames( "" );
        }
    }

}



/*
Intent reportStatsScreen = new Intent( getContext(), ExerciseReportContainer.class );
reportStatsScreen.putExtra("exName", getIntent().getExtras().getString("exName") );
        reportStatsScreen.putExtra("months", 1);

        //startActivity( reportStatsScreen );

*/