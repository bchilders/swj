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
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


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
        int months = exBundle.getInt("months");

        long minMillis = new Date().getTime();
        minMillis = minMillis - ( (DBClass.MS_IN_A_DAY * 30)* months);

        ((TextView)rootView.findViewById( R.id.exercise_name_in_report )).setText( exName );

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: first ex : "+exName);

        //draw graph
        DBClass swjDb = new DBClass( getActivity() );

        Cursor allsets = swjDb.fetchSetsForExercise(exName);

        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

        String dbKey = isWeight ? DBClass.KEY_WEIGHT : DBClass.KEY_REPS;

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        int  value;
        long time ;
        int maxVal = 0;

        for ( allsets.moveToFirst(); !allsets.isAfterLast() ; allsets.moveToNext()  ) {

            value = allsets.getInt( allsets.getColumnIndex( dbKey ) );
            time = allsets.getLong( allsets.getColumnIndex( DBClass.KEY_TIME ) );

            if ( time > minMillis )
            {
                dataPoints.add( new DataPoint(new Date(time), value) );
            }

            if (value > maxVal)
            {
                maxVal = value;
            }
        }

        graph.getViewport().setMinY( 0 );
        graph.getViewport().setMaxY( maxVal );
        graph.getViewport().setYAxisBoundsManual( true );

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity() ,  DateFormat.getDateInstance(DateFormat.SHORT) ));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        graph.getGridLabelRenderer().setNumVerticalLabels(7);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>( dataPoints.toArray( new DataPoint[ dataPoints.size() ] ));
        series.setDataPointsRadius( 4 );
        series.setDrawDataPoints( true );

        series.setColor( getResources().getColor( R.color.baseColor ) );
        graph.addSeries(series);

        return rootView;
    }
/*



 */


    // for each exercises : exercises
    // get date  & weights & reps info
    // format
    // draw

}
