package com.gk.datacontrol;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 02.06.2015.
 */
public class DataPointParcel  implements Parcelable {
    private static final String APP_NAME = "SWJournal";
    private static boolean DEBUG_FLAG = true;
    private ArrayList<DataPoint> dataPoints;

    public DataPointParcel() {
        if ( DEBUG_FLAG ) Log.d( APP_NAME, "DataPointParcel :: DataPointParcel");
        dataPoints = new ArrayList<DataPoint>();
    }

    public ArrayList<DataPoint> restoreData()
    {
        Log.d( APP_NAME, "DataPointParcel :: restoreData");
        return dataPoints;
    }

    public void clear()
    {
        Log.d( APP_NAME, "DataPointParcel :: clear");
        if ( !dataPoints.equals(null))
        {
            dataPoints.clear();
        }
    }

    public void addPoint( DataPoint dp )
    {
        Log.d( APP_NAME, "DataPointParcel :: addPoint");
        dataPoints.add( dp );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d( APP_NAME, "DataPointParcel :: writeToParcel");
        for ( DataPoint dp : dataPoints)
        {
            dest.writeDouble( dp.getX() );
            dest.writeDouble( dp.getY() );
        }

    }


}
