package com.gk.swjsettings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.gk.datacontrol.DBClass;

public class SwjSettings extends Activity {
    public static final String APP_NAME = "SWJournal";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SwjSettingsFragment()).commit();
    }

    public void onDestroy() {
        super.onDestroy();

        Log.v(APP_NAME, "SwjSettingsFragment :: onDestroy(): erase switch state: " + PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_erase_all", false));

        if ( PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_erase_all", false) ) {
            //delete databases
            DBClass swjDataOp = new DBClass(this);
            swjDataOp.cleanAllTables();
            swjDataOp.close();

            //switch the switch back
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("pref_erase_all", false).commit();
        }
    }


}