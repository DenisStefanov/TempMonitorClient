package org.duckdns.denis_st.tempmonitorclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;

public class GcmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        Log.d("TempMonitorService GCM", "Intent received " + intent.toString() );
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        if (isOrderedBroadcast()) {
            setResultCode(Activity.RESULT_OK);
        }
    }
}
