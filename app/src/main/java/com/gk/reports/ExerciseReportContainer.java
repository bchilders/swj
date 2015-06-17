package com.gk.reports;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.TextView;

import com.gk.datacontrol.DBClass;
import com.gk.datacontrol.DataPointParcel;
import com.gk.simpleworkoutjournal.R;
import com.gk.simpleworkoutjournal.WorkoutDataAdapter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gkurockins on 14/04/2015.
 */
public class ExerciseReportContainer extends Activity {
    public enum PointType {
        NONE(-1), MIN(0), MAX(1), AVG(2), SUM(3);

        private final int value;

        PointType(int value) {
            this.value = value;
        }

        public static PointType fromInteger(int x) {
            switch (x) {
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


    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = false;

    private final int MISC_TAB = 0;
    private final int GRAPH_TAB = 1;

    List fragList = new ArrayList();

    void setMonthsToTitle(ActionBar actionBar, int months) {

        int titleId;
        switch (months) {
            case 1:
                titleId = R.string.one_month_period_report;
                break;

            case 3:
                titleId = R.string.three_months_period_report;
                break;

            case 6:
                titleId = R.string.six_months_period_report;
                break;

            case 12:
                titleId = R.string.one_year_period_report;
                break;

            case 24:
                titleId = R.string.two_year_period_report;
                break;

            default:
                Log.e(APP_NAME, "ExerciseReportContainer :: onCreate :: unexpected months amount passed");
                assert false;
                return;
        }


        actionBar.setTitle(titleId);
    }

    private double actualizeValue(double cur, double prev, double act, PointType pt) {
        //   take min / take max / take sum (both for avg)
        switch (pt) {
            case MIN:
                if (prev == -1.0) {
                    prev = Double.MAX_VALUE;
                }

                if (cur < prev) {
                    act = cur;
                }
                break;

            case MAX:
                if (prev == -1.0) {
                    prev = Double.MIN_VALUE;
                }

                if (cur > prev) {
                    act = cur;
                }
                break;

            case AVG:
            case SUM:
                act += cur;
                break;

            case NONE:
            default:
                if (DEBUG_FLAG) Log.e(APP_NAME, "dwdw");
                act = -1;
                break;
        }

        return act;
    }

    double calculateStats(String exName, int months, PointType wType, PointType rType, Bundle bdl) {
        //double addLineToGraph(GraphView graph, String dataKey,  final long minMillis, PointType pointType )
        //{

        long minMillis = new Date().getTime();
        minMillis = minMillis - ((DBClass.MS_IN_A_DAY * 30) * months);

        DBClass swjDb = new DBClass(this);
        Cursor allsets = swjDb.fetchSetsForExercise(exName);

        double prevValue;
        double curValue;
        double perDateVal;
        double prevPerDateVal;
        double extremum = 0.0;

        int setsAmount;
        long curTime;
        long prevTime;

        Date actDate;
        double actPerDate;

        PointType pType;
        String dataKey;
        DataPointParcel dpc = new DataPointParcel();
        DataPointParcel dpc2 = new DataPointParcel();

        //iterate through reps and weight
        for (int j = 0; j < 2; j++) {
            //dpc.clear();
            prevValue = -1.0;
            perDateVal = -1.0;
            prevPerDateVal = -1.0;
            actPerDate = -1.0;
            extremum = 0.0;
            prevTime = -1;
            setsAmount = 0;

            pType = (j == 0) ? wType : rType;
            dataKey = (j == 0) ? DBClass.KEY_WEIGHT : DBClass.KEY_REPS;

            for (allsets.moveToFirst(); !allsets.isAfterLast(); allsets.moveToNext()) {

                curTime = allsets.getLong(allsets.getColumnIndex(DBClass.KEY_TIME));

                if (curTime > minMillis) {

                    if (prevTime == -1) {
                        prevTime = curTime;
                    }

                    setsAmount++;
                    curValue = allsets.getInt(allsets.getColumnIndex(dataKey));

                    perDateVal = actualizeValue(curValue, prevValue, perDateVal, pType);

                    if (perDateVal == -1) {
                        Log.e(APP_NAME, "ss");
                        return -1;
                    }

                    //second check for last entry case
                    for (int i = 0; i < 2; i++) {
                        if ((!swjDb.isSameDay(prevTime, curTime) && i == 0) ||
                                (allsets.isLast() && i == 1)) {
                            actDate = i == 0 ? new Date(prevTime) : new Date(curTime);
                            actPerDate = i == 0 ? prevPerDateVal : perDateVal;

                            if (pType == PointType.AVG) {
                                actPerDate /= setsAmount;
                            }

                            extremum = (actPerDate > extremum) ? actPerDate : extremum;

                            if ( j == 0) {
                                dpc.addPoint(new DataPoint(actDate, actPerDate));
                            } else {
                                dpc2.addPoint(new DataPoint(actDate, actPerDate));
                            }
                            perDateVal = curValue;
                            setsAmount = 0;
                        }
                    }

                    prevPerDateVal = perDateVal;
                    prevTime = curTime;
                    prevValue = curValue;
                }

            }
           // getIntent().putExtra((j == 0) ? "wPoints" : "rPoints", dpc);
            //bdl.putParcelable((j == 0) ? "wPoints" : "rPoints", dpc);
            if ( j == 0 )
            {
                bdl.putParcelable( "wPoints", dpc );
            } else {
                bdl.putParcelable( "rPoints", dpc2 );
            }

        }

        return extremum;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ExerciseReportContainer::onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_report_container);

        final String exName  = getIntent().getExtras().getString("exName");
        final int weightType = getIntent().getExtras().getInt("weightType");
        final int repsType   = getIntent().getExtras().getInt("repsType");
        final int months     = getIntent().getExtras().getInt("months");

        ActionBar actionBar = getActionBar();
        setMonthsToTitle( actionBar, months );
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        final Bundle data = new Bundle();

        double extremum = calculateStats(exName, months, PointType.fromInteger(weightType), PointType.fromInteger(repsType), data );

        data.putString("exName", exName);
        data.putInt("months", months);
        data.putInt("weightType", weightType);
        data.putInt("repsType", repsType);
        data.putDouble("extremum", extremum);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
                Fragment frag = null;

                if (fragList.size() > tab.getPosition())
                    fragList.get(tab.getPosition());


                if ( tab.getPosition() == MISC_TAB ) {
                    if (frag == null) {
                        frag = new ReportStatsTab();
                    }
                }
                else if ( tab.getPosition() == GRAPH_TAB )
                {
                    if (frag == null) {
                        frag = new ReportGraphTab();
                    }
                }

                frag.setArguments(data);
                fragList.add(frag);
                ft.replace(android.R.id.content, frag);
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

         actionBar.addTab(
                    actionBar.newTab()
                            .setText(R.string.stats_tab)
                            .setTabListener(tabListener));

        actionBar.addTab(
                actionBar.newTab()
                        .setText( R.string.graph_tab )
                        .setTabListener(tabListener));



    }

}
