package org.duckdns.denis_st.tempmonitorclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private String regid = null;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private Menu MainMenu = null;

    private void registerPreferenceListener()
    {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (Arrays.asList("ReadingsChanged").contains(key)) {
                    updateReadings();
                }
                if (Arrays.asList("LimitsChanged").contains(key)) {
                    updateLimits();
                }
                if (Arrays.asList("GPIOCheckboxChanged").contains(key)) {
                    updateGPIO();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    private void sendToServer(Map<String,String> data){
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder("620914624750" + "@gcm.googleapis.com")
                .setMessageId(rndId())
                .setData(data)
                .build());
            System.out.println(data);
    }

    private void showToastinMain(final String text) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();}
        });
    }

    private void updateGPIO()
    {
        MenuItem gpio17 = MainMenu.findItem(R.id.action_gpio17);
        MenuItem gpio18 = MainMenu.findItem(R.id.action_gpio18);
        MenuItem gpio22 = MainMenu.findItem(R.id.action_gpio22);
        MenuItem gpio27 = MainMenu.findItem(R.id.action_gpio27);
        gpio17.setChecked((prefs.getString("GPIO17", "").equals("On"))?true:false);
        gpio18.setChecked((prefs.getString("GPIO18", "").equals("On"))?true:false);
        gpio22.setChecked((prefs.getString("GPIO22", "").equals("On"))?true:false);
        gpio27.setChecked((prefs.getString("GPIO27", "").equals("On"))?true:false);
        gpio17.setEnabled(true);
        gpio18.setEnabled(true);
        gpio22.setEnabled(true);
        gpio27.setEnabled(true);
    }

    private void updateReadings() {
        try {
            Date nowDate = Calendar.getInstance().getTime();
            Date lclDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(prefs.getString("LastUpdatedLcl", "Fri Jan 01 00:00:00 2016"));
            String srvDate = prefs.getString("LastUpdatedSrv", "Unknown");
            String stillTemp = prefs.getString("stillTemp", "0.0");
            String towerTemp = prefs.getString("towerTemp", "0.0");

            ViewGroup myLayout = (ViewGroup) findViewById(R.id.include);
            DrawView drawView = new DrawView(this, stillTemp, prefs.getString("stillTempThreshold", "0.0"),
                    towerTemp, prefs.getString("towerTempThreshold", "0.0"));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLimits() {
        try {
            TextView StillTempFix = (TextView) findViewById(R.id.editStillTempFix);
            TextView TowerTempFix = (TextView) findViewById(R.id.editTowerTempFix);
            ToggleButton toggleStill = (ToggleButton) findViewById(R.id.ToggleStillBtn);
            ToggleButton toggleTower = (ToggleButton) findViewById(R.id.ToggleTowerBtn);

            toggleStill.setChecked(prefs.getBoolean("stillToggleChecked", false));
            toggleTower.setChecked(prefs.getBoolean("towerToggleChecked", false));
            StillTempFix.setText(prefs.getString("stillTempThreshold", "0.0"));
            TowerTempFix.setText(prefs.getString("towerTempThreshold", "0.0"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String rndId() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
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
        Button cameraShot = (Button) findViewById(R.id.CamerashotClick);
        TextView StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
        TextView TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);

        toggleStill.setChecked(prefs.getBoolean("stillToggleChecked", false));
        toggleTower.setChecked(prefs.getBoolean("towerToggleChecked", false));
        StillTempFix.setText(prefs.getString("stillTempThreshold", "0.0"));
        TowerTempFix.setText(prefs.getString("towerTempThreshold", "0.0"));
        StillTempFix.setEnabled(!toggleStill.isChecked());
        TowerTempFix.setEnabled(!toggleTower.isChecked());

        View v = getWindow().getDecorView().findViewById(android.R.id.content);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    updateReadings();
                    updateLimits();
                    Log.d("TempMon", FirebaseInstanceId.getInstance().getToken());
                }
                return true;
            }
        });

        toggleStill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView StillTempFix = (TextView)findViewById(R.id.editStillTempFix);
                StillTempFix.setEnabled(!isChecked);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("stillTempThreshold", (isChecked)?StillTempFix.getText().toString():"0.0");
                editor.putBoolean("stillToggleChecked", isChecked);
                editor.commit();
            }
        });

        toggleTower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView TowerTempFix = (TextView)findViewById(R.id.editTowerTempFix);
                TowerTempFix.setEnabled(!isChecked);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("towerTempThreshold", (isChecked)?TowerTempFix.getText().toString():"0.0");
                editor.putBoolean("towerToggleChecked", isChecked);
                editor.commit();
            }
        });

        cameraShot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                final Map<String, String> data = new HashMap<String, String>();
                data.put("message_type", "GetPicture");
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(data);
                    }
                }.start();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReadings();
        updateLimits();
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
        MainMenu = menu;

        final Map<String, String> data = new HashMap<String, String>();

        data.put("message_type", "ReadActuals");
        new Thread() {
            @Override
            public void run() {
                sendToServer(data);
            }
        }.start();

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
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "StoreRegid");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_startSrv) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "StartServer");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_stopSrv) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "StopServer");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_configSrv) {
            final Map<String, String> data = new HashMap<String, String>();

            data.put("message_type", "ServerConfig");
            data.put("fixtempstill", String.valueOf(prefs.getBoolean("stillToggleChecked", false)));
            data.put("absolutestill", prefs.getString("stillTempThreshold", "0.0"));
            data.put("fixtemptower", String.valueOf(prefs.getBoolean("towerToggleChecked", false)));
            data.put("absolutetower", prefs.getString("towerTempThreshold", "0.0"));

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("stillTempThreshold", data.get("absolutestill"));
            editor.putString("towerTempThreshold", data.get("absolutetower"));
            editor.putBoolean("stillToggleChecked", Boolean.valueOf(data.get("fixtempstill")));
            editor.putBoolean("towerToggleChecked", Boolean.valueOf(data.get("fixtemptower")));
            editor.commit();

            new Thread() {
                @Override
                public void run() {sendToServer(data);}
            }.start();

            return true;
        }
        if (id == R.id.action_configSrvGet) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "ConfigServerGet");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }

        if (id == R.id.action_gpio17) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "PowerControl");
            data.put("GPIO", "17");
            data.put("State", item.isChecked()?"Off": "On");
            //item.setChecked(item.isChecked()?false: true);
            item.setEnabled(false);
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_gpio18) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "PowerControl");
            data.put("GPIO", "18");
            data.put("State", item.isChecked()?"Off": "On");
            //item.setChecked(item.isChecked()?false: true);
            item.setEnabled(false);
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_gpio27) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "PowerControl");
            data.put("GPIO", "27");
            data.put("State", item.isChecked()?"Off": "On");
            //item.setChecked(item.isChecked()?false: true);
            item.setEnabled(false);
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }
        if (id == R.id.action_gpio22) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "PowerControl");
            data.put("GPIO", "22");
            data.put("State", item.isChecked()?"Off": "On");
            //item.setChecked(item.isChecked()?false: true);
            item.setEnabled(false);
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }

        if (id == R.id.action_readActuals) {
            final Map<String, String> data = new HashMap<String, String>();
            data.put("message_type", "ReadActuals");
            new Thread() {
                @Override
                public void run() {
                    sendToServer(data);
                }
            }.start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
