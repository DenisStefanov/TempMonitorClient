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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "GcmIntentService";
	private NotificationManager mNotificationManager;
	private static Timer mTimer;
	private mTimerTask alarmTimerTask;

	public GcmIntentService() {
		super("GcmIntentService");		
	}

	private void Notify (String msg, String ringtone, Boolean notifyMsg, Boolean vibrate){
        sendNotification(msg);
        try {
            if (notifyMsg) {
                if (vibrate) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);
                }
                Context ctx = getBaseContext();

                if (ringtone != null) {
                    Intent startIntent = new Intent(ctx, RingtonePlayingService.class);
                    startIntent.putExtra("ringtone-uri", ringtone);
                    ctx.startService(startIntent);
                }
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    class mTimerTask extends TimerTask {
	    private String ringtone;

	    mTimerTask(String rt){
            ringtone = rt;
        }

        @Override
        public void run() {
	        Notify("CONNECTION LOST!!!", this.ringtone, true, true);
            }
    }

    private void restartTimer(String ringtone, Integer delay) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        alarmTimerTask = new mTimerTask(ringtone);
        mTimer.schedule(alarmTimerTask, delay*1000);
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

            Integer delay = Integer.parseInt(prefs.getString("ConnectionTimeoutPreference", "10"));
            String ringtone = prefs.getString("notifications_new_message_ringtone", null);
            restartTimer(ringtone, delay);

			if (type.equals("upd") || type.equals("alarma")) {
				Date nowDate = Calendar.getInstance().getTime();
                SharedPreferences.Editor editor = prefs.edit();
				editor.putString("LastUpdatedSrv", extras.getString("LastUpdated", null));
				editor.putString("stillTemp", extras.getString("tempStill", "0.0"));
				editor.putString("towerTemp", extras.getString("tempTower", "0.0"));
				String s = extras.getString("liqLevelSensor", "0");
				editor.putBoolean("liqLevel", Boolean.parseBoolean(extras.getString("liqLevelSensor", "0")));
				editor.putString("LastUpdatedLcl", new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").format(nowDate));
                editor.putBoolean("ReadingsChanged", !prefs.getBoolean("ReadingsChanged", false));
				editor.commit();
            }
            if (type.equals("alarma")) {
                Notify("ALARMA", ringtone, prefs.getBoolean("notifications_new_message", true), prefs.getBoolean("notifications_new_message_vibrate", true));
			}
			if (type.equals("Notify")){
                Notify(extras.getString("note", null), null, true, false);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("GPIO" + extras.getString("GPIO", ""), extras.getString("State", ""));
                editor.putBoolean("GPIOCheckboxChanged", !prefs.getBoolean("GPIOCheckboxChanged", false));
                editor.commit();
            }
            if (type.equals("ServerConfig")){
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("stillTempThreshold", extras.getString("stillTempThreshold", "0.0"));
				editor.putString("towerTempThreshold", extras.getString("towerTempThreshold", "0.0"));
				editor.putBoolean("stillToggleChecked", Boolean.valueOf(extras.getString("stillToggle", null)));
				editor.putBoolean("towerToggleChecked", Boolean.valueOf(extras.getString("towerToggle", null)));
                editor.putBoolean("LimitsChanged", !prefs.getBoolean("LimitsChanged", false));
				editor.commit();
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