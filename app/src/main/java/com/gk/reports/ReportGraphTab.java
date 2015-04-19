package com.gk.reports;


import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gk.datacontrol.DBClass;
import com.gk.simpleworkoutjournal.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class ReportGraphTab extends Fragment {
    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = true;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: onCreateView()");

        if ( getActivity() == null ) {
            Log.e(APP_NAME, "ReportGraphTab :: onCreateView() failed since no atcivity is attached");
            throw new IllegalStateException("fragment is not attached to any activity");
        }

        View rootView = inflater.inflate(R.layout.fragment_ex_graph, container, false);

        //get passed data
        Bundle exBundle = getArguments();
        String exName = exBundle.getString("exName");
        boolean isWeight = exBundle.getBoolean("isWeight");
        int months = exBundle.getInt("month");

        ((TextView)rootView.findViewById( R.id.exercise_name_in_report )).setText( exName );

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: first ex : "+exName);

        //draw graph
        DBClass swjDb = new DBClass( getActivity() );

        Cursor allsets = swjDb.fetchSetsForExercise(exName);

        DataPoint[] dataPoints = new DataPoint[ allsets.getCount() ];

        int pos, value;
        long time ;

        String dbKey = isWeight ? DBClass.KEY_WEIGHT : DBClass.KEY_REPS;

        for ( allsets.moveToFirst(); !allsets.isAfterLast() ; allsets.moveToNext()  ) {

            pos =  allsets.getPosition();

            value = allsets.getInt( allsets.getColumnIndex( dbKey ) );
            time = allsets.getLong( allsets.getColumnIndex( DBClass.KEY_TIME ) );

            dataPoints[ pos ] = new DataPoint( time, value);
        }

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        LineGraphSeries<DataPoint> seriesW = new LineGraphSeries<DataPoint>(dataPoints);
        seriesW.setDataPointsRadius( 4 );
        seriesW.setDrawDataPoints( true );
      //  LineGraphSeries<DataPoint> seriesR = new LineGraphSeries<DataPoint>(repsPoints);


        graph.addSeries(seriesW);
      //  graph.addSeries(seriesR);

        return rootView;
    }
/*



 */


    // for each exercises : exercises
    // get date  & weights & reps info
    // format
    // draw

}
