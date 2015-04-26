package com.gk.reports;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gk.simpleworkoutjournal.R;

/**
 * Created by George on 19.04.2015.
 */
public class ReportPeriodChooser extends Activity implements View.OnClickListener {
    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = false;

    Button one_M_btn;
    Button three_M_btn;
    Button six_M_btn;
    Button one_Y_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if ( DEBUG_FLAG) Log.v( APP_NAME, "ReportPeriodChooser :: onCreate");

        super.onCreate(savedInstanceState);
        setContentView( R.layout.report_period_chooser );

        one_M_btn = (Button)findViewById( R.id.one_month_btn );
        three_M_btn = (Button)findViewById( R.id.three_months_btn );
        six_M_btn = (Button)findViewById( R.id.six_months_btn );
        one_Y_btn = (Button)findViewById( R.id.one_year_btn );

        one_M_btn.setOnClickListener( this );
        three_M_btn.setOnClickListener( this );
        six_M_btn.setOnClickListener( this );
        one_Y_btn.setOnClickListener( this );

    }

    @Override
    public void onClick(View v) {
        if ( DEBUG_FLAG) Log.v( APP_NAME, "ReportPeriodChooser :: onClick");

        int months;
        switch ( v.getId() ) {
            case R.id.one_month_btn:
                months = 1;
                break;
            case R.id.three_months_btn:
                months = 3;
                break;
            case R.id.six_months_btn:
                months = 6;
                break;
            case R.id.one_year_btn:
                months = 12;
                break;
            default:
                Log.e( APP_NAME, "ReportPeriodChooser :: onClick unidentified button clicked");
                assert false;
                return;
        }

        Intent reportStatsScreen = new Intent( this, ExerciseReportContainer.class );
        reportStatsScreen.putExtra("exName", getIntent().getExtras().getString("exName") );
        reportStatsScreen.putExtra("months", months);

        startActivity( reportStatsScreen );
    }
}
