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

        double maxYW = 0;
        double maxYR = 0;
        if ( weightType != PointType.NONE )
        {
            dbKey = DBClass.KEY_WEIGHT;
            maxYW = addLineToGraph(graph, dbKey, allsets, minMillis, weightType, swjDb);
        }

        if ( repsType != PointType.NONE)
        {
            dbKey = DBClass.KEY_REPS;
            maxYR = addLineToGraph(graph, dbKey, allsets, minMillis, repsType, swjDb);
        }

        graph.getViewport().setMaxY( maxYW > maxYR ? maxYW : maxYR );

        return rootView;
    }

    private double actualizeValue( double cur, double prev, double act, PointType pt)
    {
        //   take min / take max / take sum (both for avg)
        switch ( pt ) {
            case MIN:
                if ( cur < prev )
                {
                    act = cur;
                }
                break;

            case MAX:
                if ( cur > prev )
                {
                    act = cur;
                }
                break;

            case AVG:
            case SUM:
                act += cur;
                break;

            case NONE:
            default:
                if ( DEBUG_FLAG ) Log.e(APP_NAME, "dwdw");
                act = -1;
                break;
        }

        return act;
    }

    double addLineToGraph( GraphView graph, String dataKey, Cursor dataCursor, final long minMillis, PointType pointType, DBClass db )
    {
        double prevValue = 0;
        double curValue;
        double perDateVal = -1;
        double maxVal = 0;

        int setsAmount = 0;
        long curTime ;
        long prevTime = -1;

        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

        for ( dataCursor.moveToFirst(); !dataCursor.isAfterLast() ; dataCursor.moveToNext()  ) {

            curTime = dataCursor.getLong( dataCursor.getColumnIndex( DBClass.KEY_TIME ) );

            if ( curTime > minMillis )
            {

                if ( prevTime == -1)  { prevTime  = curTime ; }

                setsAmount++;
                curValue = dataCursor.getInt(dataCursor.getColumnIndex(dataKey));

                if (curValue > maxVal) { maxVal = curValue; }

                perDateVal = actualizeValue( curValue, prevValue, perDateVal, pointType);

                if ( perDateVal == -1 )
                {
                    Log.e(APP_NAME, "ss");
                    return -1;
                }

                for ( int i = 0; i < 2; i++)
                {
                    if ( ( !db.isSameDay(prevTime, curTime) && i == 0) ||
                         ( dataCursor.isLast()              && i == 1) )
                    {
                        if ( pointType == PointType.AVG ) { perDateVal /= setsAmount; }

                        dataPoints.add( new DataPoint( i == 0 ? new Date( prevTime ) : new Date( curTime ), perDateVal) );

                        perDateVal = 0;
                        setsAmount = 0;
                    }
                }

                prevTime = curTime;
                prevValue = curValue;
            }

        }

        graph.getViewport().setMinY(0);
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
        return maxVal;
    }


}
