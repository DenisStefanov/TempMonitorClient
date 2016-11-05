package org.duckdns.denis_st.tempmonitorclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private GcmRegistrar tmgcm;
    private GcmIntentService tmgis;
    private String regid = null;
    private String stillTempThresholdText = "0.0";
    private String towerTempThresholdText = "0.0";

    private void showToastinMain(final String text) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();}
        });

    }

    private void updateUIControlsInMain(final String result) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                TextView StillTempFix = (TextView) findViewById(R.id.editStillTempFix);
                TextView TowerTempFix = (TextView) findViewById(R.id.editTowerTempFix);
                ToggleButton toggleStill = (ToggleButton) findViewById(R.id.ToggleStillBtn);
                ToggleButton toggleTower = (ToggleButton) findViewById(R.id.ToggleTowerBtn);
                for (String configEl : result.split(";")) {
                    if (configEl.split(",")[0].equals("Conf1")) {
                        if (configEl.split(",")[1].equals("fixtemp")) {
                            toggleStill.setChecked(configEl.split(",")[2].equals("yes"));
                        }
                        if (configEl.split(",")[1].equals("absolute")) {
                            StillTempFix.setText(configEl.split(",")[2]);
                            stillTempThresholdText = StillTempFix.getText().toString();
                        }
                    }
                    if (configEl.split(",")[0].equals("Conf2")) {
                        if (configEl.split(",")[1].equals("fixtemp")) {
                            toggleTower.setChecked(configEl.split(",")[2].equals("yes"));
                        }
                        if (configEl.split(",")[1].equals("absolute")) {
                            TowerTempFix.setText(configEl.split(",")[2]);
                            towerTempThresholdText = TowerTempFix.getText().toString();
                        }
                    }
                }
                String Srvdata = tmgis.getData();
                updateScreen(Srvdata);
            }
        });
    }

    private void PerformServerCommand(String command, String host, String user, String pwd, Integer cmdtype){
        String result = "";
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
            showToastinMain(result);
            System.out.println("host response: " + result);
            System.out.println("exit-status: " + channel.getExitStatus());
            Thread.sleep(500);
            channel.disconnect();
            session.disconnect();
            if (cmdtype == R.id.action_configSrvGet) {
                if (!result.isEmpty())
                    updateUIControlsInMain(result);
            }
        }
        catch(Exception e){
            System.out.println(e);
            showToastinMain(e.toString());
        }
        return;
    }

    private void GCMRegister() {
            System.out.println("Registering with GCM...");
            regid = tmgcm.register();
            if (!regid.isEmpty()) {
                System.out.println("Got GCM reg id. [" + regid + "]");
                }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tmgis = new GcmIntentService();

        String Srvdata = tmgis.getData();
        if (Srvdata != null)
            updateScreen(Srvdata);

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
                if (event.getAction() == MotionEvent.ACTION_UP){
                    String Srvdata = tmgis.getData();
                    //Srvdata = "Sun Oct 30 21:05:39 2016,23.375,22.937";
                    if (Srvdata != null)
                        updateScreen(Srvdata);
                }
                return true;
            }
        });

        ToggleButton toggleStill = (ToggleButton) findViewById(R.id.ToggleStillBtn);
        toggleStill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
                if (isChecked) {
                    StillTempFix.setEnabled(false);
                    stillTempThresholdText = StillTempFix.getText().toString();
                } else {
                    StillTempFix.setEnabled(true);
                }
                String Srvdata = tmgis.getData();
                if (Srvdata != null)
                    updateScreen(Srvdata);
            }
        });
        ToggleButton toggleTower = (ToggleButton) findViewById(R.id.ToggleTowerBtn);
        toggleTower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);
                if (isChecked) {
                    TowerTempFix.setEnabled(false);
                    towerTempThresholdText = TowerTempFix.getText().toString();
                } else {
                    TowerTempFix.setEnabled(true);
                }
                String Srvdata = tmgis.getData();
                if (Srvdata != null)
                    updateScreen(Srvdata);

            }
        });
    }

    private void updateScreen(String Srvdata){
        try {
            String srvDateText = Srvdata.split(",")[0];
            Date srvDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(srvDateText);
            String stillTempText = Srvdata.split(",")[1];
            String towerTempText = Srvdata.split(",")[2];

            Context ctx = getBaseContext();
            Intent stopIntent = new Intent(ctx, RingtonePlayingService.class);
            ctx.stopService(stopIntent);

            ViewGroup myLayout = (ViewGroup) findViewById(R.id.include);

            DrawView drawView = new DrawView(this, stillTempText, stillTempThresholdText, towerTempText, towerTempThresholdText);
            myLayout.addView(drawView);

            Date nowDate = Calendar.getInstance().getTime();
            long diffSec = Math.abs(srvDate.getTime() - nowDate.getTime()) / 1000;
            long diffMin = diffSec/60;

            TextView LastUpd = (TextView)findViewById(R.id.lastupdate);
            LastUpd.setText(srvDateText + " (" + String.valueOf(diffMin) + ":" + String.valueOf(diffSec) + " ago)");
            TextView TowerTemp = (TextView)findViewById(R.id.tempTowerVal);
            TowerTemp.setText(towerTempText);
            TextView StillTemp = (TextView)findViewById(R.id.tempStillVal);
            StillTemp.setText(stillTempText);
    }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String ServerIP = prefs.getString("ServerIP", null);
        final String user = prefs.getString("user", null);
        final String passwd = prefs.getString("passwd", null);
        final String startSrvCmd = prefs.getString("startSrvCmd", null);
        final String stopSrvCmd = prefs.getString("stopSrvCmd", null);
        final String configSrvCmd = prefs.getString("configSrvCmd", null);
        final String configSrvGetCmd = prefs.getString("configSrvGetCmd", null);

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_updRegID) {
            if (regid.isEmpty()) {
                showToastinMain("RegID is empty. Will not update Server");
                return false;
            }
            new Thread() {
                @Override
                public void run() {PerformServerCommand(configSrvCmd + " 'Common,regid,"  + regid + "'", ServerIP, user, passwd, R.id.action_updRegID);}}.start();
            return true;
        }
        if (id == R.id.action_startSrv) {
            new Thread() {
                @Override
                public void run() {PerformServerCommand(startSrvCmd, ServerIP, user, passwd, R.id.action_startSrv);}}.start();
            return true;
        }
        if (id == R.id.action_stopSrv) {
            new Thread() {
                @Override
                public void run() {PerformServerCommand(stopSrvCmd, ServerIP, user, passwd,R.id.action_stopSrv);}}.start();
            return true;
        }
        if (id == R.id.action_configSrv) {
            final String params, params1, params2;
            TextView StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
            ToggleButton toggleStill = (ToggleButton) findViewById(R.id.ToggleStillBtn);
            TextView TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);
            ToggleButton toggleTower = (ToggleButton) findViewById(R.id.ToggleTowerBtn);

            params = toggleStill.isChecked()?"Conf1,fixtemp,yes;":"Conf1,fixtemp,no;";
            params1 = toggleTower.isChecked()?"Conf2,fixtemp,yes;":"Conf2,fixtemp,no;";
            params2 = "Conf1,absolute," + StillTempFix.getText().toString() + ";"+ "Conf2,absolute," + TowerTempFix.getText().toString();

            new Thread() {
                @Override
                public void run() {PerformServerCommand(configSrvCmd + " '" + params + params1 + params2 + "'", ServerIP, user, passwd, R.id.action_configSrv);}}.start();
            return true;
        }
        if (id == R.id.action_configSrvGet) {
            final String params;
            params = "Conf1,fixtemp;Conf2,fixtemp;Conf1,absolute;Conf2,absolute";
            new Thread() {
                @Override
                public void run() {PerformServerCommand(configSrvGetCmd + " '" + params + "'", ServerIP, user, passwd, R.id.action_configSrvGet);}}.start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
