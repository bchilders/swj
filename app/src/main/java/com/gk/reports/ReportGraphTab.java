package com.gk.reports;


import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gk.datacontrol.DBClass;
import com.gk.datacontrol.DataPointParcel;
import com.gk.simpleworkoutjournal.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
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

        ((TextView)rootView.findViewById( R.id.exercise_name_in_report )).setText(exName);

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: first ex : "+exName);

        DataPointParcel parceledPoints;
        ArrayList<DataPoint> points;
        LineGraphSeries<DataPoint> series;

        //draw graph
        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        String[] parPoints = {"wPoints","rPoints"};
        String pointType;
        for ( String parPoint : parPoints )
        {
            pointType = (parPoint == "wPoints") ? "weightType" : "repsType";
            if ( PointType.fromInteger( exBundle.getInt( pointType  ) ) == PointType.NONE )
            {
                continue;
            }

            parceledPoints = exBundle.getParcelable( parPoint );
            points = parceledPoints.restoreData();
            series = new LineGraphSeries<DataPoint>( points.toArray( new DataPoint[ points.size() ] ) );
            series.setDataPointsRadius(4);
            series.setDrawDataPoints(true);

            if ( parPoint == "wPoints" ) {
                series.setColor(getResources().getColor(R.color.baseColor_complementary));
            } else {
                series.setColor(getResources().getColor(R.color.baseColor));
            }

            String legendTitle = (  parPoint == "wPoints" ) ? getString(R.string.weights) : getString(R.string.reps);
            graph.addSeries( series );
            series.setTitle( legendTitle );
        }

        graph.getViewport().setYAxisBoundsManual(true);
        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), DateFormat.getDateInstance(DateFormat.SHORT)));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        graph.getGridLabelRenderer().setNumVerticalLabels(7);

        graph.getViewport().setMaxY( exBundle.getDouble("extremum") + 5);
        graph.getViewport().setMinY(0);

        // legend
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setMargin( 10 );
        graph.getLegendRenderer().setBackgroundColor( Color.argb( 150 , 187 , 231, 247 ) ); // base color - lightest

        return rootView;
    }

    private double actualizeValue( double cur, double prev, double act, PointType pt)
    {
        //   take min / take max / take sum (both for avg)
        switch ( pt ) {
            case MIN:
                if ( prev == -1.0)
                {
                    prev = Double.MAX_VALUE;
                }

                if ( cur < prev )
                {
                    act = cur;
                }
                break;

            case MAX:
                if ( prev == -1.0)
                {
                    prev = Double.MIN_VALUE;
                }

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
        double prevValue = -1.0;
        double curValue;
        double perDateVal = -1.0;
        double prevPerDateVal = -1.0;
        double extremum = 0.0;

        int setsAmount = 0;
        long curTime ;
        long prevTime = -1;

        Date actDate;
        double actPerDate;

        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

        for ( dataCursor.moveToFirst(); !dataCursor.isAfterLast() ; dataCursor.moveToNext()  ) {

            curTime = dataCursor.getLong( dataCursor.getColumnIndex( DBClass.KEY_TIME ) );

            if ( curTime > minMillis )
            {

                if ( prevTime == -1)  { prevTime  = curTime ; }

                setsAmount++;
                curValue = dataCursor.getInt(dataCursor.getColumnIndex(dataKey));

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
                        actDate =  i == 0 ? new Date( prevTime ) : new Date( curTime );
                        actPerDate = i == 0 ? prevPerDateVal : perDateVal;

                        if ( pointType == PointType.AVG ) { actPerDate /= setsAmount; }

                        extremum = ( actPerDate > extremum ) ? actPerDate : extremum;
                        dataPoints.add( new DataPoint( actDate, actPerDate) );

                        perDateVal = curValue;
                        setsAmount = 0;
                    }
                }

                prevPerDateVal = perDateVal;
                prevTime = curTime;
                prevValue = curValue;
            }

        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>( dataPoints.toArray( new DataPoint[ dataPoints.size() ] ));
        series.setDataPointsRadius(4);
        series.setDrawDataPoints(true);

        if ( dataKey ==  DBClass.KEY_WEIGHT) {
            series.setColor(getResources().getColor(R.color.baseColor_complementary));
        } else {
            series.setColor(getResources().getColor(R.color.baseColor));
        }

        String legendTitle = ( dataKey == DBClass.KEY_WEIGHT) ? getString( R.string.weights ): getString( R.string.reps );
        graph.addSeries(series);

        series.setTitle( legendTitle);

        return extremum;
    }


}
