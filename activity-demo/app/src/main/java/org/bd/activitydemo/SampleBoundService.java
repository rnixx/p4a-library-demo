package org.bd.activitydemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class SampleBoundService extends Service {
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    String tag="SampleBoundService";

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        SampleBoundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SampleBoundService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag,"Bound Service created");
    }

    @Override
    public void onDestroy() {
        Log.d(tag, "Bound service destroy");
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(tag, "Bound Service onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(tag, "Bound Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(tag, "Bound Service onRebind");

        super.onRebind(intent);
    }

    /** method for clients */
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }
}
