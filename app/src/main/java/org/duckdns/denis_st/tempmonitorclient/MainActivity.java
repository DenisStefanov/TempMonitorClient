package org.duckdns.denis_st.tempmonitorclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static org.duckdns.denis_st.tempmonitorclient.ServerConnection.sendToServer;
import static org.duckdns.denis_st.tempmonitorclient.ServerConnection.serverReconfigure;

public class MainActivity extends AppCompatActivity {
    private GcmRegistrar tmgcm;
    private GoogleCloudMessaging gcm;
    private String regid = null;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private ToggleButton toggleStill;
    private ToggleButton toggleTower;
    private TextView StillTempFix;
    private TextView TowerTempFix;
    private CheckBox toggleStillAuto;
    private CheckBox toggleTowerAuto;

    private void registerPreferenceListener()
    {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (Arrays.asList("stillTemp", "towerTemp", "coolerTemp", "liqLevel",
                        "LastUpdatedLcl", "pressureVal").contains(key)) {
                    updateReadings();
                }
                if (Arrays.asList("stillTempThreshold", "towerTempThreshold", "stillToggleChecked",
                        "towerToggleChecked", "stillAutoChecked", "towerAutoChecked").contains(key)) {
                    updateLimits();
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

    private void updateReadings() {
        try {
            Date nowDate = Calendar.getInstance().getTime();
            Date lclDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(prefs.getString("LastUpdatedLcl", "Fri Jan 01 00:00:00 2016"));
            String srvDate = prefs.getString("LastUpdatedSrv", "Unknown");
            String stillTemp = prefs.getString("stillTemp", "0.0");
            String towerTemp = prefs.getString("towerTemp", "0.0");
            Boolean liqLevel = prefs.getBoolean("liqLevel", false);
            String coolerTemp = prefs.getString("coolerTemp", "0.0");
            String pressure   = prefs.getString("pressureVal", "0.0");

            ViewGroup myLayout = (ViewGroup) findViewById(R.id.include);
            DrawView drawView = new DrawView(this, stillTemp, prefs.getString("stillTempThreshold", "0.0"),
                    towerTemp, prefs.getString("towerTempThreshold", "0.0"), liqLevel);
            myLayout.addView(drawView);

            long diffSec = Math.abs(lclDate.getTime() - nowDate.getTime()) / 1000;
            long diffMin = diffSec / 60;
            diffSec = diffSec - diffMin * 60;

            TextView LastUpd = (TextView) findViewById(R.id.lastupdate);
            LastUpd.setText(srvDate.toString() + " (" + String.valueOf(diffMin) + ":" + String.valueOf(diffSec) + " ago)");
            TextView TowerTemp = (TextView) findViewById(R.id.tempTowerVal);
            TowerTemp.setText(towerTemp);
            TextView StillTemp = (TextView) findViewById(R.id.tempStillVal);
            StillTemp.setText(stillTemp);
            TextView CoolerTemp = (TextView) findViewById(R.id.tempCoolerVal);
            CoolerTemp.setText("Cooler:" + coolerTemp);
            TextView PressureTemp = (TextView) findViewById(R.id.pressureTextVal);
            PressureTemp.setText("Press:" + pressure);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void composeMessage(Bundle msgData){

        msgData.putString("message_type", "ServerConfig");
        msgData.putString("fixtempstill", String.valueOf(toggleStill.isChecked()));
        msgData.putString("fixtemptower", String.valueOf(toggleTower.isChecked()));
        msgData.putString("absolutestill", StillTempFix.getText().toString());
        msgData.putString("absolutetower", TowerTempFix.getText().toString());
        msgData.putString("fixtemptowerbypower", String.valueOf(toggleTowerAuto.isChecked()));
        msgData.putString("fixtempstillbypower", String.valueOf(toggleStillAuto.isChecked()));

        toggleTower.setEnabled(false);
        toggleStill.setEnabled(false);
        toggleTowerAuto.setEnabled(false);
        toggleStillAuto.setEnabled(false);
    }

    private void updateLimits() {
        try {
            toggleStillAuto.setChecked(prefs.getBoolean("stillAutoChecked", false));
            toggleTowerAuto.setChecked(prefs.getBoolean("towerAutoChecked", false));
            toggleStill.setChecked(prefs.getBoolean("stillToggleChecked", false));
            toggleTower.setChecked(prefs.getBoolean("towerToggleChecked", false));
            StillTempFix.setText(prefs.getString("stillTempThreshold", "0.0"));
            TowerTempFix.setText(prefs.getString("towerTempThreshold", "0.0"));

            toggleStillAuto.setEnabled(true);
            toggleTowerAuto.setEnabled(true);
            toggleStill.setEnabled(true);
            toggleTower.setEnabled(true);
            StillTempFix.setEnabled(!toggleStill.isChecked());
            TowerTempFix.setEnabled(!toggleTower.isChecked());

        } catch (Exception e) {
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
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
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

//        Timer updateTimer = new Timer();
//        TimerTask updateReadings = new UpdateReadings();
//        updateTimer.scheduleAtFixedRate(updateReadings, 1000, 1000);

        toggleStill = (ToggleButton) findViewById(R.id.ToggleStillBtn);
        toggleTower = (ToggleButton) findViewById(R.id.ToggleTowerBtn);
        StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
        TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);
        toggleStillAuto = (CheckBox) findViewById(R.id.checkBoxAutoStill);
        toggleTowerAuto = (CheckBox) findViewById(R.id.checkBoxAutoTower);

        toggleStillAuto.setChecked(prefs.getBoolean("stillAutoChecked", false));
        toggleTowerAuto.setChecked(prefs.getBoolean("towerAutoChecked", false));
        toggleStill.setChecked(prefs.getBoolean("stillToggleChecked", false));
        toggleTower.setChecked(prefs.getBoolean("towerToggleChecked", false));
        StillTempFix.setText(prefs.getString("stillTempThreshold", "0.0"));
        TowerTempFix.setText(prefs.getString("towerTempThreshold", "0.0"));
        StillTempFix.setEnabled(!toggleStill.isChecked());
        TowerTempFix.setEnabled(!toggleTower.isChecked());

        tmgcm = new GcmRegistrar(getApplicationContext());
        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);

        // Check device for Play Services APK.
        if (tmgcm.checkPlayServices(this)) {
            new Thread() {
                @Override
                public void run() {GCMRegister();}}.start();
            } else {
            System.out.println("No valid Google Play Services APK found.");
        }

        View v = getWindow().getDecorView().findViewById(android.R.id.content);

        v.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeLeft() {
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(i);
            }
            /*
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    updateReadings();
                }
                return true;
            }*/
        });

        toggleStill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //SharedPreferences.Editor editor = prefs.edit();
                //editor.putString("stillTempThreshold", (isChecked)?StillTempFix.getText().toString():"0.0");
                //editor.putBoolean("stillToggleChecked", isChecked);
                //editor.commit();
                final Bundle Data = new Bundle();
                composeMessage(Data);
                serverReconfigure(gcm, Data);
           }
        });

        toggleTower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //SharedPreferences.Editor editor = prefs.edit();
                //editor.putString("towerTempThreshold", (isChecked)?TowerTempFix.getText().toString():"0.0");
                //editor.putBoolean("towerToggleChecked", isChecked);
                //editor.commit();
                final Bundle Data = new Bundle();
                composeMessage(Data);
                serverReconfigure(gcm, Data);
            }
        });

        toggleStillAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("stillAutoChecked", isChecked);
                editor.commit();
                final Bundle Data = new Bundle();
                composeMessage(Data);
                serverReconfigure(gcm, Data);
            }
        });

        toggleTowerAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("towerAutoChecked", isChecked);
                editor.commit();
                final Bundle Data = new Bundle();
                composeMessage(Data);
                serverReconfigure(gcm, Data);
            }
        });

        final Bundle dataAct = new Bundle();
        dataAct.putString("message_type", "ReadActuals");
        new Thread() {
            @Override
            public void run() {
                sendToServer(gcm, dataAct);
            }
        }.start();

        final Bundle dataDim = new Bundle();
        dataDim.putString("message_type", "ReadDimmer");
        new Thread() {
            @Override
            public void run() {
                sendToServer(gcm, dataDim);
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReadings();
        updateLimits();
        //updateDIMMER();
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
                    sendToServer(gcm, data);
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
                    sendToServer(gcm, data);
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
                    sendToServer(gcm, data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_configSrv) {
            final Bundle Data = new Bundle();
            composeMessage(Data);
            serverReconfigure(gcm, Data);
            return true;
        }
        if (id == R.id.action_configSrvGet) {
            final Bundle data = new Bundle();
            data.putString("message_type", "ConfigServerGet");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(gcm, data);
                }
            }.start();
            return true;
        }

        if (id == R.id.action_readActuals) {
            final Bundle data = new Bundle();
            data.putString("message_type", "ReadActuals");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(gcm, data);
                }
            }.start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
