package org.duckdns.denis_st.tempmonitorclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Arrays;

import static org.duckdns.denis_st.tempmonitorclient.ServerConnection.sendToServer;

public class Main2Activity extends AppCompatActivity  {
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SeekBar dimmerControlBar;
    private TextView dimmerTextVal;
    private Button buttonDimUP;
    private Button buttonDimDN;
    private CheckBox gpio17;
    private CheckBox gpio18;
    private CheckBox gpio22;
    private CheckBox gpio27;
    private GoogleCloudMessaging gcm;
    private CompoundButton.OnCheckedChangeListener gpio17Listener, gpio18Listener, gpio22Listener, gpio27Listener;

    private void updateDIMMER() {
        try {
            dimmerControlBar.setProgress(Integer.parseInt(prefs.getString("DIMMER", "0")));
            dimmerTextVal.setText(Double.toString(Math.round(Double.parseDouble(prefs.getString("DIMMER", "0")) / 1.2)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerPreferenceListener()
    {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                //System.out.println("onSharedPreferenceChanged " + key);
                if (Arrays.asList("GPIO17", "GPIO18", "GPIO22", "GPIO27").contains(key)) {
                    updateGPIO();
                }
                if (Arrays.asList("DIMMER").contains(key)) {
                    updateDIMMER();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }

    private void updateGPIO()
    {
        gpio17.setOnCheckedChangeListener (null);
        gpio18.setOnCheckedChangeListener (null);
        gpio22.setOnCheckedChangeListener (null);
        gpio27.setOnCheckedChangeListener (null);

        gpio17.setChecked((prefs.getString("GPIO17", "").equals("On"))?true:false);
        gpio18.setChecked((prefs.getString("GPIO18", "").equals("On"))?true:false);
        gpio22.setChecked((prefs.getString("GPIO22", "").equals("On"))?true:false);
        gpio27.setChecked((prefs.getString("GPIO27", "").equals("On"))?true:false);

        gpio17.setEnabled(true);
        gpio18.setEnabled(true);
        gpio22.setEnabled(true);
        gpio27.setEnabled(true);

        gpio17.setOnCheckedChangeListener (gpio17Listener);
        gpio18.setOnCheckedChangeListener (gpio18Listener);
        gpio22.setOnCheckedChangeListener (gpio22Listener);
        gpio27.setOnCheckedChangeListener (gpio27Listener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        registerPreferenceListener();

        gcm = GoogleCloudMessaging.getInstance(Main2Activity.this);

        View v = getWindow().getDecorView().findViewById(android.R.id.content);

        dimmerControlBar = (SeekBar) findViewById(R.id.seekBar);
        dimmerTextVal = (TextView) findViewById(R.id.dimmerTextVal);
        buttonDimUP = (Button) findViewById(R.id.buttonDimUP);
        buttonDimDN = (Button) findViewById(R.id.buttonDimDN);
        gpio17 = (CheckBox) findViewById(R.id.gpio17);
        gpio18 = (CheckBox) findViewById(R.id.gpio18);
        gpio22 = (CheckBox) findViewById(R.id.gpio22);
        gpio27 = (CheckBox) findViewById(R.id.gpio27);

        gpio17Listener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Bundle data = new Bundle();
                data.putString("message_type", "PowerControl");
                data.putString("GPIO", "17");
                data.putString("State", isChecked?"On": "Off");
                buttonView.setEnabled(false);
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(gcm, data);
                    }
                }.start();
            }
        };

        gpio18Listener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Bundle data = new Bundle();
                data.putString("message_type", "PowerControl");
                data.putString("GPIO", "18");
                data.putString("State", isChecked?"On": "Off");
                buttonView.setEnabled(false);
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(gcm, data);
                    }
                }.start();
            }
        };

        gpio22Listener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Bundle data = new Bundle();
                data.putString("message_type", "PowerControl");
                data.putString("GPIO", "22");
                data.putString("State", isChecked?"On": "Off");
                buttonView.setEnabled(false);
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(gcm, data);
                    }
                }.start();
            }
        };

        gpio27Listener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Bundle data = new Bundle();
                data.putString("message_type", "PowerControl");
                data.putString("GPIO", "27");
                data.putString("State", isChecked?"On": "Off");
                buttonView.setEnabled(false);
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(gcm, data);
                    }
                }.start();
            }
        };

        updateGPIO(); //also set listeners

        buttonDimUP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "DimmerControl");
                data.putString("DIMMER", "UP");
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(gcm, data);
                    }
                }.start();
            }
        });

        buttonDimDN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "DimmerControl");
                data.putString("DIMMER", "DN");
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(gcm, data);
                    }
                }.start();
            }
        });

        dimmerControlBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("onStop Tracking touch ");
                if (seekBar.getProgress() < 5)
                    seekBar.setProgress(5);
                //Toast.makeText(getApplicationContext(), String.valueOf(progress), Toast.LENGTH_LONG).show();
                final Bundle data = new Bundle();
                data.putString("message_type", "DimmerControl");
                data.putString("DIMMER", Integer.toString(seekBar.getProgress()));
                new Thread() {
                    @Override
                    public void run() {
                        sendToServer(gcm, data);
                    }
                }.start();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });

        v.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
                Intent i = new Intent(Main2Activity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDIMMER();
    }
}
