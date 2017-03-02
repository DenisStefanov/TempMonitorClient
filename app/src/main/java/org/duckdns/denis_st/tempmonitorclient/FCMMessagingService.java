package org.duckdns.denis_st.tempmonitorclient;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by Den on 3/2/2017.
 */

public class FCMMessagingService extends FirebaseMessagingService{
    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        processData(remoteMessage);
    }

    private void processData(RemoteMessage msg){
        try {
            String type = msg.getData().get("type");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (type.equals("upd") || type.equals("alarma")) {
                Date nowDate = Calendar.getInstance().getTime();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("LastUpdatedSrv", msg.getData().get("LastUpdated"));
                editor.putString("stillTemp", msg.getData().get("tempStill"));
                editor.putString("towerTemp", msg.getData().get("tempTower"));
                editor.putString("LastUpdatedLcl", new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").format(nowDate));
                editor.putBoolean("ReadingsChanged", !prefs.getBoolean("ReadingsChanged", false));
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
                sendNotification(msg.getData().get("note"));
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("GPIO" + msg.getData().get("GPIO"), msg.getData().get("State"));
                editor.putBoolean("GPIOCheckboxChanged", !prefs.getBoolean("GPIOCheckboxChanged", false));
                editor.commit();
            }
            if (type.equals("ServerConfig")){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("stillTempThreshold", msg.getData().get("stillTempThreshold"));
                editor.putString("towerTempThreshold", msg.getData().get("towerTempThreshold"));
                editor.putBoolean("stillToggleChecked", Boolean.valueOf(msg.getData().get("stillToggle")));
                editor.putBoolean("towerToggleChecked", Boolean.valueOf(msg.getData().get("towerToggle")));
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
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setAutoCancel(true)
                        .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
