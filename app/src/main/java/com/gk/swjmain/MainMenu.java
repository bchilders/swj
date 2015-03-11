package com.gk.swjmain;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.gk.simpleworkoutjournal.R;
import com.gk.simpleworkoutjournal.WorkoutJournal;
import com.gk.swjsettings.SwjSettings;

public class MainMenu extends Activity implements OnClickListener {
	private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = false;
	Button startWorkoutBtn;
    Button settingsBtn;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	if ( DEBUG_FLAG ) Log.v(APP_NAME, "MainMenu :: onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        startWorkoutBtn = (Button) findViewById(R.id.buttonMmStart);
        startWorkoutBtn.setOnClickListener(this);

        settingsBtn = (Button) findViewById(R.id.buttonMmSettings);
        settingsBtn.setOnClickListener(this);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onClick(View v) {
    	switch (v.getId()) {
    		case R.id.buttonMmStart:
    		    startActivity(new Intent(this, WorkoutJournal.class));
    			break;

            case R.id.buttonMmSettings:
                startActivity(new Intent(this, SwjSettings.class) );
                break;
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if ( DEBUG_FLAG ) Log.v(APP_NAME, "MainMenu :: onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
}
