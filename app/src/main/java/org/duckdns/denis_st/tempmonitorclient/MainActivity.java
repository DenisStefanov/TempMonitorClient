package org.duckdns.denis_st.tempmonitorclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GcmRegistrar tmgcm;
    private GcmIntentService tmgis;
    private PowerManager.WakeLock wakeLock;
    private List<String> dataList = null;
    private String ServerIP;
    private String user;
    private String passwd;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private String regid = null;
    private String startSrvCmd, stopSrvCmd, RegIDSrvFile, getLogCmd;

    private void showToastinMain(final String text) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();}
        });

    }
    private String PerformServerCommand(String command, String host, String user, String pwd){
        String result = null;
        try{
            JSch jsch=new JSch();

            Session session=jsch.getSession(user, host, 22);
            session.setPassword(pwd);

            session.setConfig("StrictHostKeyChecking", "no");

            session.connect(5000); // making a connection with timeout.

            Channel channel=session.openChannel("exec");

            InputStream in=channel.getInputStream();
            //((ChannelExec)channel).setErrStream(System.err);

            ((ChannelExec)channel).setCommand(command);

            channel.connect(5000);

            byte[] tmp=new byte[1024];
            while(true) {
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;
                result = result + new String(tmp, 0, i);
            }

            showToastinMain(result + " Ok");

            System.out.println("host response: " + result);
            System.out.println("exit-status: " + channel.getExitStatus());
            Thread.sleep(500);
            channel.disconnect();
            session.disconnect();
        }
        catch(Exception e){
            System.out.println(e);
            showToastinMain(e.toString());
        }
        return result;
    }
    private void GCMRegister() {
            System.out.println("Registering with GCM...");
            regid = tmgcm.register();
            if (!regid.isEmpty()) {
                System.out.println("Got GCM reg id. [" + regid + "]");
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    ServerIP = prefs.getString("ServerIP", null);
                    user = prefs.getString("user", null);
                    passwd = prefs.getString("passwd", null);
                    System.out.println("Got Server IP from shared prefs. [" + ServerIP + "]");
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        ServerIP = prefs.getString("ServerIP", null);
        user = prefs.getString("user", null);
        passwd = prefs.getString("passwd", null);
        startSrvCmd = prefs.getString("startSrvCmd", null);
        stopSrvCmd = prefs.getString("stopSrvCmd", null);
        RegIDSrvFile = prefs.getString("RegIDSrvFile", null);
        getLogCmd = prefs.getString("getLogCmd", null);
        System.out.println("Got Server IP from shared prefs. [" + ServerIP + "]");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tmgis = new GcmIntentService();

        PowerManager pm;
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(pm.SCREEN_BRIGHT_WAKE_LOCK, "My wakelock");

        tmgcm = new GcmRegistrar(getApplicationContext());
        // Check device for Play Services APK.
        if (tmgcm.checkPlayServices(this)) {
            new Thread() {
                @Override
                public void run() {GCMRegister();}}.start();
            } else {
            System.out.println("No valid Google Play Services APK found.");
        }

        View v = getWindow().getDecorView().findViewById(android.R.id.content);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){requestForData();}
                return true;
            }
        });

    }

    private void requestForData(){
        try {

            Context ctx = getBaseContext();
            Intent stopIntent = new Intent(ctx, RingtonePlayingService.class);
            ctx.stopService(stopIntent);

            dataList = tmgis.getData();
            if (dataList == null){
                dataList = new ArrayList<String>(Arrays.asList("Mon Jan 01 00:00:00 2000,00.00,00.00"));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        wakeLock.acquire();
        super.onResume();
    }

    @Override
    protected void onPause() {
        wakeLock.release();
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id. action_updRegID) {
            new Thread() {
                @Override
                public void run() {PerformServerCommand("echo " + regid + " > " + RegIDSrvFile, ServerIP, user, passwd);}}.start();
            return true;
        }
        if (id == R.id. action_startSrv) {
            new Thread() {
                @Override
                public void run() {PerformServerCommand(startSrvCmd, ServerIP, user, passwd);}}.start();
            return true;
        }
        if (id == R.id. action_stopSrv) {
            new Thread() {
                @Override
                public void run() {PerformServerCommand(stopSrvCmd, ServerIP, user, passwd);}}.start();
            return true;
        }
        if (id == R.id. action_showSrvLog) {

            String SrvLog = PerformServerCommand(getLogCmd, ServerIP, user, passwd);

            Intent intent = new Intent(this, ServerLogActivity.class);
            intent.putExtra("SERVER_LOG_DATA", SrvLog);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
