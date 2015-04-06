package com.gk.simpleworkoutjournal;

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

    WorkoutTimer( MenuItem tv) {
        clockView = tv;
    }

    void drawClock(int minutes, int seconds) {
        Log.v(APP_NAME, "iteration " + minutes + ":" + seconds);
        clockView.setTitle(minutes + ":" + seconds);
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
            clockThread = new Thread(new TimeRunner());
            clockThread.start();
        }
    }

    void stop() {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutTimer :: stopping  timer");
         clockThread.interrupt();
    }

    void reset() {

    }

    void pause() {

    }

    class TimeRunner implements Runnable {

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

                WorkoutTimer.this.drawClock(mins, secs);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (DEBUG_FLAG) Log.v(APP_NAME, "WorkoutTimer :: timer interrupted");
                }
            }
        }
    }

}
