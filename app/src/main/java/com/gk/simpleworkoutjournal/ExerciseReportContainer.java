package com.gk.simpleworkoutjournal;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

/**
 * Created by gkurockins on 14/04/2015.
 */
public class ExerciseReportContainer extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_report_container);

        ActionBar actionBar = getActionBar();

        // Creating ActionBar tabs.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
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
                            .setText("Stats")
                            .setTabListener(tabListener));

        actionBar.addTab(
                actionBar.newTab()
                        .setText("Graph")
                        .setTabListener(tabListener));





    }
}
