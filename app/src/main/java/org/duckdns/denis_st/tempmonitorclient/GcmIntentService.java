package org.duckdns.denis_st.tempmonitorclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "GcmIntentService";
	private NotificationManager mNotificationManager;
	private static String data = null;
		
	public String getData(){
		System.out.println("returning datalist");	
		return data;
	}
	
	public GcmIntentService() {
		super("GcmIntentService");		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				processData(extras.getString("type"), extras.getString("data"));
				//Log.i(TAG, "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void processData(String type, String newdata){
		try {
			System.out.println("Received = " + newdata + " Type = " + type);
			data = newdata;
			if (type.equals("alarma")) {
				sendNotification("ALARMA: " + data);
				try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if (prefs.getBoolean("notifications_new_message", true)) {
                        if (prefs.getBoolean("notifications_new_message_vibrate", true)) {
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(3000);
                        }
                        Context ctx = getBaseContext();

                        Intent startIntent = new Intent(ctx, RingtonePlayingService.class);
                        String s = prefs.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
                        startIntent.putExtra("ringtone-uri", prefs.getString("notifications_new_message_ringtone", null));
                        ctx.startService(startIntent);
                    }
				} catch (Exception e) {e.printStackTrace();}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.iconsmall)
		.setContentTitle("TempMon GCM Notification")
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(msg))
		.setAutoCancel(true)
		.setContentText("Content");

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
}