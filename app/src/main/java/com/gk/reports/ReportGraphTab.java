package com.gk.reports;


import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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

import junit.framework.Assert;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ReportGraphTab extends Fragment {
    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = true;

    public enum PointType {
        NONE(-1), MIN(0), MAX(1), AVG(2), SUM(3);

        private final int value;

        PointType(int value) {
            this.value = value;
        }

        public static PointType fromInteger(int x) {
            switch(x) {
                case -1:
                    return NONE;
                case 0:
                    return MIN;
                case 1:
                    return MAX;
                case 2:
                    return AVG;
                case 3:
                    return SUM;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

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
        int months = exBundle.getInt("months");
        PointType weightType = PointType.fromInteger(exBundle.getInt("weightType"));
        PointType repsType = PointType.fromInteger(exBundle.getInt("repsType"));

        ((TextView)rootView.findViewById( R.id.exercise_name_in_report )).setText( exName );

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: first ex : "+exName);

        //draw graph
        DBClass swjDb = new DBClass( getActivity() );


        String dbKey;
        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
        Cursor allsets = swjDb.fetchSetsForExercise(exName);

        if ( weightType == PointType.NONE && repsType == PointType.NONE )
        {
            Log.e(APP_NAME,"ReportGraphTab :: onCreateView : ");
            throw new RuntimeException();
        }

        long minMillis = new Date().getTime();
        minMillis = minMillis - ( (DBClass.MS_IN_A_DAY * 30)* months);

        if ( weightType != PointType.NONE )
        {
            dbKey = DBClass.KEY_WEIGHT;
            addLineToGraph(graph, dbKey, allsets, minMillis);
        }

        if ( repsType != PointType.NONE)
        {
            dbKey = DBClass.KEY_REPS;
            addLineToGraph(graph, dbKey, allsets, minMillis);
        }


        return rootView;
    }

    void addLineToGraph( GraphView graph, String dataKey, Cursor dataCursor, final long minMillis )
    {
        int  value;
        long time ;
        int maxVal = 0;

        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

        for ( dataCursor.moveToFirst(); !dataCursor.isAfterLast() ; dataCursor.moveToNext()  ) {

            value = dataCursor.getInt( dataCursor.getColumnIndex( dataKey ) );
            time = dataCursor.getLong( dataCursor.getColumnIndex( DBClass.KEY_TIME ) );

            if ( time > minMillis )
            {
                dataPoints.add( new DataPoint(new Date(time), value) );
            }

            if (value > maxVal)
            {
                maxVal = value;
            }
        }

        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(maxVal);
        graph.getViewport().setYAxisBoundsManual(true);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), DateFormat.getDateInstance(DateFormat.SHORT)));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        graph.getGridLabelRenderer().setNumVerticalLabels(7);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>( dataPoints.toArray( new DataPoint[ dataPoints.size() ] ));
        series.setDataPointsRadius(4);
        series.setDrawDataPoints(true);

        if ( dataKey ==  DBClass.KEY_WEIGHT) {
            series.setColor(getResources().getColor(R.color.baseColor_complementary));
        } else {
            series.setColor(getResources().getColor(R.color.baseColor));
        }

        graph.addSeries(series);
    }


}
