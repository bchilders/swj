package com.gk.simpleworkoutjournal;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class ReportGraphTab extends Fragment {
    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: onCreate()");

        View rootView = inflater.inflate(R.layout.fragment_ex_graph, container, false);

        Bundle exBundle = getActivity().getIntent().getExtras();
        String[] exNames = exBundle.getStringArray("exs");
        Log.v(APP_NAME, "ReportGraphTab :: first ex : "+exNames[0]);


        GraphView graph = (GraphView) getView().findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        return rootView;
    }



    // for each exercises : exercises
    // get date  & weights & reps info
    // format
    // draw

}
