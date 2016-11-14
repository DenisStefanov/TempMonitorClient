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

import com.google.android.gms.gcm.GoogleCloudMessaging;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private GcmRegistrar tmgcm;
    private GcmIntentService tmgis;
    private String regid = null;
    private String stillTempThresholdText = "0.0";
    private String towerTempThresholdText = "0.0";
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private void registerPreferenceListener()
    {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("ServerData")) {
                    updateScreen(prefs.getString(key, null));
                }
                else if (key.equals("ServerConfig")) {
                    updateUIControlsInMain(prefs.getString(key, null));
                }

            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

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
                //String Srvdata = tmgis.getData();
                //updateScreen(Srvdata);

                //SharedPreferences sp = getSharedPreferences("TempMonServerPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("stillTempThresholdText", StillTempFix.getText().toString());
                editor.putString("towerTempThresholdText", TowerTempFix.getText().toString());
                editor.putBoolean("stillToggleChecked", toggleStill.isChecked());
                editor.putBoolean("towerToggleChecked", toggleTower.isChecked());
                editor.commit();
            }
        });
    }

    private void sendToServer(Bundle data){
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
        try {
            gcm.send("620914624750" + "@gcm.googleapis.com", Calendar.getInstance().getTime().toString(), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        registerPreferenceListener();

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ToggleButton toggleStill = (ToggleButton) findViewById(R.id.ToggleStillBtn);
        ToggleButton toggleTower = (ToggleButton) findViewById(R.id.ToggleTowerBtn);
        TextView StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
        TextView TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);

        toggleStill.setChecked(prefs.getBoolean("stillToggleChecked", false));
        toggleTower.setChecked(prefs.getBoolean("towerToggleChecked", false));
        StillTempFix.setText(prefs.getString("stillTempThresholdText", "0.0"));
        TowerTempFix.setText(prefs.getString("towerTempThresholdText", "0.0"));
        StillTempFix.setEnabled(!toggleStill.isChecked());
        TowerTempFix.setEnabled(!toggleTower.isChecked());

        tmgis = new GcmIntentService();
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
                    //String Srvdata = tmgis.getData();
                    //Srvdata = "Sun Oct 30 21:05:39 2016,23.375,22.937";
                    //if (Srvdata != null)
                    //    updateScreen(Srvdata);
                }
                return true;
            }
        });

        toggleStill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
                StillTempFix.setEnabled(!isChecked);
                if (isChecked) stillTempThresholdText = StillTempFix.getText().toString();
                //String Srvdata = tmgis.getData();
                //if (Srvdata != null)
                //    updateScreen(Srvdata);
            }
        });

        toggleTower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);
                TowerTempFix.setEnabled(!isChecked);
                if (isChecked) towerTempThresholdText = TowerTempFix.getText().toString();
                //String Srvdata = tmgis.getData();
                //if (Srvdata != null)
                //    updateScreen(Srvdata);
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

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_updRegID) {
            final Bundle data = new Bundle();
            data.putString("message_type", "StoreRegid");
            if (regid.isEmpty()) {
                showToastinMain("RegID is empty. Will not update Server");
                return false;
            }
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_startSrv) {
            final Bundle data = new Bundle();
            data.putString("message_type", "StartServer");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_stopSrv) {
            final Bundle data = new Bundle();
            data.putString("message_type", "StopServer");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_configSrv) {
            TextView StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
            ToggleButton toggleStill = (ToggleButton) findViewById(R.id.ToggleStillBtn);
            TextView TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);
            ToggleButton toggleTower = (ToggleButton) findViewById(R.id.ToggleTowerBtn);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("stillTempThresholdText", StillTempFix.getText().toString());
            editor.putString("towerTempThresholdText", TowerTempFix.getText().toString());
            editor.putBoolean("stillToggleChecked", toggleStill.isChecked());
            editor.putBoolean("towerToggleChecked", toggleTower.isChecked());
            editor.commit();

            final Bundle data = new Bundle();
            data.putString("message_type", "ReconfigServer");
            data.putString("Conf1", toggleStill.isChecked()?"fixtemp,yes;":"fixtemp,no;");
            data.putString("Conf1", "absolute," + StillTempFix.getText().toString());
            data.putString("Conf2", toggleTower.isChecked()?"fixtemp,yes;":"fixtemp,no;");
            data.putString("Conf2", "absolute," + TowerTempFix.getText().toString());
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();

            return true;
        }
        if (id == R.id.action_configSrvGet) {
            final Bundle data = new Bundle();
            data.putString("message_type", "ConfigServerGet");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_portScan) {
            Intent intent = new Intent(this, PortScanActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
