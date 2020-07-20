package org.duckdns.denis_st.tempmonitorclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.System.in;
import static org.duckdns.denis_st.tempmonitorclient.ServerConnection.serverReconfigure;

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
        sendNotification(msg, "my_channel_01");
        try {
            if (notifyMsg) {
                if (vibrate) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
					Integer vibLen = Integer.parseInt(prefs.getString("VibrateLengthPreference", "1000"));
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(vibLen);
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
			Log.d("TempMonitorService", "Timer task run" );
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
		Log.d("TempMonitorService", "Scheduled a timer" );

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
			else {
				processCustomData(extras);
			}
		}
		else
			Log.d("TempMonitorService", "Extras empty ");
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		//GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void processCustomData(Bundle extras){
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		try {
			String type = extras.getString("type", null);
			String data = extras.getString("data", null);
			String dimval;
			Log.d("TempMonitorService","Incoming message data: " + data);
			Bundle commands = new Bundle();
			for (String token : (data.split(","))){
				Log.d("TempMonitorService","token " + token + " split " + token.split(":"));
				String[] tok = token.split(":");
				commands.putString(tok[0], tok[1] +':' + tok[2]);
			}
			Log.d("TempMonitorService","Bundle " + commands.toString() );

			Log.d("TempMonitorService","Incoming message" + extras.toString());
			if  (type != null && type.equals("NFCCtrl")) {

				for (String key : commands.keySet()) {
					final Bundle data_send = new Bundle();
					data_send.putString("message_type", key);
					data_send.putString(commands.getString(key).split(":")[0],
							commands.getString(key).split(":")[1]);
					serverReconfigure(gcm, data_send);
				}
			}
		} catch (Exception e) {e.printStackTrace();}
	}



	private void processData(Bundle extras){
		try {
            String type = extras.getString("type", null);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            Integer delay = Integer.parseInt(prefs.getString("ConnectionTimeoutPreference", "10"));
            String ringtone = prefs.getString("notifications_new_message_ringtone", null);
            restartTimer(ringtone, delay);

			Log.d("TempMonitorService", "Incoming message type " + type);
			Log.d("TempMonitorService","Incoming message" + extras.toString());

			if (type.equals("upd") || type.equals("alarma")) {
				Date nowDate = Calendar.getInstance().getTime();
                SharedPreferences.Editor editor = prefs.edit();
				editor.putString("LastUpdatedSrv", extras.getString("LastUpdated", null));
				editor.putString("stillTemp", extras.getString("tempStill", "0.0"));
				editor.putString("towerTemp", extras.getString("tempTower", "0.0"));
				editor.putString("coolerTemp", extras.getString("tempCooler", "0.0"));
				editor.putBoolean("liqLevel", Boolean.parseBoolean(extras.getString("liqLevelSensor", "0")));
				editor.putString("LastUpdatedLcl", new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").format(nowDate));
				editor.putString("pressureVal", extras.getString("pressureVal", ""));

				editor.commit();
            }
            if (type.equals("alarma")) {
                Notify("ALARMA." +
						" TempStill=" + extras.getString("tempStill", "0.0") +
						" TempTower=" + extras.getString("tempTower", "0.0"),
						ringtone, prefs.getBoolean("notifications_new_message", true),
						prefs.getBoolean("notifications_new_message_vibrate", true));
			}

			if (type.equals("Notify")) {
				Notify(extras.getString("note", null), null, true, false);
			}

			if (type.equals("NotifyWater")){
                //Notify(extras.getString("note", null), null, true, false);
				//System.out.println("Incoming message note " + extras.getString("note", null));
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("WaterControl", extras.getString("note", null));
                    editor.commit();
			}

			if (type.equals("NotifyDIMMER")){
				//Notify(extras.getString("note", null), null, true, false);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("DIMMER", extras.getString("DIMMER", "0"));
				editor.commit();
			}

            if (type.equals("ServerConfig")){
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("stillTempThreshold", extras.getString("stillTempThreshold", "0.0"));
				editor.putString("towerTempThreshold", extras.getString("towerTempThreshold", "0.0"));
				editor.putBoolean("stillToggleChecked", Boolean.valueOf(extras.getString("stillToggle", null)));
				editor.putBoolean("towerToggleChecked", Boolean.valueOf(extras.getString("towerToggle", null)));
				editor.putBoolean("stillAutoChecked", Boolean.valueOf(extras.getString("stillAutoToggle", null)));
                editor.putBoolean("towerAutoChecked", Boolean.valueOf(extras.getString("towerAutoToggle", null)));
                editor.commit();
			}
		} catch (Exception e) {e.printStackTrace();}
	}

	private void sendNotification(String msg, String CHANNEL_ID) {
		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification.Builder(this)
				//.setContentTitle("TempMonitorClient")
				.setContentText(msg)
				.setSmallIcon(R.drawable.iconsmall)
				.setChannelId(CHANNEL_ID)
				.build();

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
}