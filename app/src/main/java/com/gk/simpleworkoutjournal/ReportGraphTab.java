package com.gk.simpleworkoutjournal;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gk.datacontrol.DBClass;
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

        Bundle exBundle = getActivity().getIntent().getExtras();
        String exName = exBundle.getString("exName");
        Log.v(APP_NAME, "ReportGraphTab :: first ex : "+exName);

        //draw graph
        DBClass swjDb = new DBClass( getActivity() );

        Cursor allsets = swjDb.fetchSetsForExercise(exName);

        DataPoint[] weightPoints = new DataPoint[ allsets.getCount() ];
        DataPoint[] repsPoints  = new DataPoint[ allsets.getCount() ];

        int pos;
        int reps;
        int weight;
        long time;

        for ( allsets.moveToFirst(); !allsets.isAfterLast() ; allsets.moveToNext()  ) {

            pos =  allsets.getPosition();

            reps = allsets.getInt( allsets.getColumnIndex( DBClass.KEY_REPS ) );
            weight = allsets.getInt( allsets.getColumnIndex( DBClass.KEY_WEIGHT ) );
            time = allsets.getLong( allsets.getColumnIndex( DBClass.KEY_TIME ) );

            weightPoints[ pos ] = new DataPoint( time, weight);
            //repsPoints[ pos ] = new DataPoint( time, reps);
        }

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        LineGraphSeries<DataPoint> seriesW = new LineGraphSeries<DataPoint>(weightPoints);
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
