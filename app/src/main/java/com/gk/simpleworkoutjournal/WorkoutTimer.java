package com.gk.simpleworkoutjournal;

import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by George on 06.04.2015.
 */
public class WorkoutTimer {

    public static final  String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = true;

    private static boolean enabled = false;
    Thread clockThread;

    MenuItem clockView;
    Activity parentActivity;

    WorkoutTimer( Activity parentAct, MenuItem tv) {
        clockView = tv;
        parentActivity = parentAct;

        if (enabled)
        {
            drawClock(0,0);
        }
    }

    void drawClock(int minutes, int seconds) {
        Log.v(APP_NAME, "iteration " + minutes + ":" + seconds);
        clockView.setTitle( String.format("%02d:%02d", minutes, seconds) );
    }


    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    void start() {

        if (enabled)
        {
            stop();
            clockThread = new Thread(new TimeRunner( parentActivity ));
            clockThread.start();
        }
    }

    void stop() {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutTimer :: stopping  timer");
        if ( clockThread != null ) clockThread.interrupt();
        drawClock(0,0);
    }

    void reset() {

    }

    void pause() {

    }


    class TimeRunner implements Runnable {


        class uiTimerUpdater implements Runnable {
            int m;
            int s;

            uiTimerUpdater( int m, int s) {
                this.m = m;
                this.s = s;
            }

            @Override
            public void run() {
                WorkoutTimer.this.drawClock(m, s);
            }
        }

        Activity uiActivity;

        TimeRunner( Activity parAct ) {
            this.uiActivity = parAct;
        }

        public void run() {
            if (DEBUG_FLAG) Log.v(APP_NAME, "WorkoutTimer :: starting timer");

            int mins = 0;
            int secs = 0;

            while (true) {

                if (!enabled) {
                    break;
                }

                secs++;

                if (secs == 60) {
                    mins++;
                    secs = 0;
                }

                uiActivity.runOnUiThread(new uiTimerUpdater( mins, secs));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (DEBUG_FLAG) Log.v(APP_NAME, "WorkoutTimer :: timer interrupted");
                    return;
                }
            }
        }
    }

}
