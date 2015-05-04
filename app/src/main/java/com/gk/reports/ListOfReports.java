package com.gk.reports;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.gk.datacontrol.DBClass;
import com.gk.simpleworkoutjournal.R;

/**
 * Created by gkurockins on 15/04/2015.
 */
public class ListOfReports extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = false;

    ListView exList;
    DBClass dbmediator;

    SimpleCursorAdapter exNameAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "Reports::onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_reports);

        String[] from = new String[] { DBClass.KEY_ID };
        int[] to = new int[] { R.id.exNameToReport };

        dbmediator  = new DBClass(this);
        exNameAdapter = new SimpleCursorAdapter(this, R.layout.ex_to_report_list_entry, null, from, to, 0);

        exList = (ListView) findViewById(R.id.exNamesReportList);
        exList.setAdapter(exNameAdapter);
        exList.setOnItemClickListener(this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "Reports::onItemClick");
        String ex = ((TextView)view.findViewById(R.id.exNameToReport)).getText().toString();

        Log.v(APP_NAME, "Reports::onItemClick: "+ex);

        Intent reportConfigurator = new Intent( this, ReportConfigurator.class );
        reportConfigurator.putExtra("exName", ex);
        startActivityForResult(reportConfigurator, 0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new MyCursorLoader(this, dbmediator);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        exNameAdapter.swapCursor(cursor);
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
