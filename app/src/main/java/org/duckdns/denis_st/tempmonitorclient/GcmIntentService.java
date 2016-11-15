package org.duckdns.denis_st.tempmonitorclient;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "GcmIntentService";
	private NotificationManager mNotificationManager;

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
				processData(extras);
     		}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void processData(Bundle extras){
		try {
            String type = extras.getString("type", null);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if (type.equals("upd") || type.equals("alarma")) {
                String time = extras.getString("time", null);
                String tempStill = extras.getString("tempStill", null);
                String tempTower = extras.getString("tempTower", null);
                String data = time + "," + tempStill + "," + tempTower;
                SharedPreferences.Editor editor = prefs.edit();
				editor.putString("ServerData", data);
				editor.commit();
			}
			if (type.equals("alarma")) {
				sendNotification("ALARMA");
				try {
                    if (prefs.getBoolean("notifications_new_message", true)) {
                        if (prefs.getBoolean("notifications_new_message_vibrate", true)) {
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(3000);
                        }
                        Context ctx = getBaseContext();

                        Intent startIntent = new Intent(ctx, RingtonePlayingService.class);
                        startIntent.putExtra("ringtone-uri", prefs.getString("notifications_new_message_ringtone", null));
                        ctx.startService(startIntent);
                    }
				} catch (Exception e) {e.printStackTrace();}
			}
			if (type.equals("Notify")){
				sendNotification(extras.getString("note", null));
			}
            if (type.equals("ServerConfig")){
                //
            }

		} catch (Exception e) {e.printStackTrace();}
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.iconsmall)
		.setContentTitle("TempMonitorClient")
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(msg))
		.setAutoCancel(true)
		.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}