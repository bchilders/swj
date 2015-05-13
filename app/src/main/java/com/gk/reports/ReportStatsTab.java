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


public class ReportStatsTab extends Fragment {
    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = true;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportStatsTab :: onCreateView()");

        if ( getActivity() == null ) {
            Log.e(APP_NAME, "ReportStatsTab :: onCreateView() failed since no atcivity is attached");
            throw new IllegalStateException("fragment is not attached to any activity");
        }

        View rootView = inflater.inflate(R.layout.fragment_ex_stats, container, false);
        return rootView;
    }

}
