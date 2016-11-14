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
import android.widget.Toast;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "GcmIntentService";
	private NotificationManager mNotificationManager;
	private static String data = null;

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
     		}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void processData(String type, String newdata){
		try {
			System.out.println("Received = " + newdata + " Type = " + type);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            data = newdata;
			if (type.equals("upd")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("ServerData", newdata);
				editor.commit();

			}
			else if (type.equals("alarma")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("ServerData", newdata);
				editor.commit();

				sendNotification("ALARMA: " + data);
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
			else if (type.equals("configUpdated")){
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("ServerConfig", data);
				editor.commit();
				sendNotification("Server config updated");
			}
			else if (type.equals("Notify")){
				sendNotification(data);
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
		.setContentTitle("TempMon GCM Notification")
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(msg))
		.setAutoCancel(true)
		.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}