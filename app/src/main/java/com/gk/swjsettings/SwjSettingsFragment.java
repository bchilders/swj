package com.gk.swjsettings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gk.simpleworkoutjournal.R;

public class SwjSettingsFragment extends PreferenceFragment {
    public static final String APP_NAME = "SWJournal";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.swjprefs);

    }


}
