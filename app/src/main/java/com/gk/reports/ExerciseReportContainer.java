package com.gk.reports;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.gk.simpleworkoutjournal.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gkurockins on 14/04/2015.
 */
public class ExerciseReportContainer extends Activity
{

    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = false;

    private final int MISC_TAB = 0;
    private final int GRAPH_TAB = 1;

    List fragList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ExerciseReportContainer::onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_report_container);

        final String exName  = getIntent().getExtras().getString("exName");
        final int months     = getIntent().getExtras().getInt("months");
        final int weightType = getIntent().getExtras().getInt("weightType");
        final int repsType   = getIntent().getExtras().getInt("repsType");

        int titleId;
        switch ( months ) {
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
                Log.e( APP_NAME ,"ExerciseReportContainer :: onCreate :: unexpected months amount passed");
                assert false;
                return;
        }

        ActionBar actionBar = getActionBar();
        actionBar.setTitle( titleId );

        // Creating ActionBar tabs.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
                Fragment frag = null;

                if (fragList.size() > tab.getPosition())
                    fragList.get(tab.getPosition());

                Bundle data = new Bundle();

                data.putString("exName", exName);
                data.putInt("months", months);
                data.putInt("weightType", weightType);
                data.putInt("repsType", repsType);

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
