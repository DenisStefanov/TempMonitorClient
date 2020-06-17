package org.duckdns.denis_st.tempmonitorclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import java.util.Arrays;
import static org.duckdns.denis_st.tempmonitorclient.ServerConnection.serverReconfigure;


public class Main2Activity extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SeekBar dimmerControlBar;
    private TextView dimmerTextVal, waterAngle;
    private Button buttonDimUP, buttonDimDN;
    private Button btnOpen, btnClose, btnOAdd10, btnCAdd10, btnOAdd1, btnCAdd1;
    private Button btnOpenDist, btnOpenRec;
    private GoogleCloudMessaging gcm;

    private void updateDIMMER() {
        try {
            String dimVal = prefs.getString("DIMMER", "0");
            double dimValPercent = Math.round(Double.parseDouble(dimVal) / 1.2);
            dimmerControlBar.setProgress(Integer.parseInt(dimVal));
            dimmerTextVal.setText(Double.toString(dimValPercent));
            dimmerTextVal.setEnabled((dimValPercent>4)?true:false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableButtons(boolean enable, boolean force) {
        if (Integer.decode(prefs.getString("WaterControl", "0")) < 180 || force) {
            btnOpen.setEnabled(enable);
            btnOAdd10.setEnabled(enable);
            btnOAdd1.setEnabled(enable);
        }
        if (Integer.decode(prefs.getString("WaterControl", "0")) >= 180 || force) {
                btnClose.setEnabled(enable);
                btnCAdd10.setEnabled(enable);
                btnCAdd1.setEnabled(enable);
            }
    }

    private void registerPreferenceListener() {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                //System.out.println("onSharedPreferenceChanged " + key);
                if (Arrays.asList("DIMMER").contains(key)) {
                    updateDIMMER();
                }
                if (Arrays.asList("WaterControl").contains(key)) {
                    enableButtons(true, false);
                    waterAngle.setText(prefs.getString("WaterControl", "0"));
                    System.out.println("notify water =  " + waterAngle.getText());
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
        waterAngle = (TextView) findViewById(R.id.waterAngle);
        buttonDimUP = (Button) findViewById(R.id.buttonDimUP);
        buttonDimDN = (Button) findViewById(R.id.buttonDimDN);
        btnOpen = (Button) findViewById(R.id.btnOpen);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnOAdd10 = (Button) findViewById(R.id.btnOAdd10);
        btnCAdd10 = (Button) findViewById(R.id.btnCAdd10);
        btnOAdd1 = (Button) findViewById(R.id.btnOAdd1);
        btnCAdd1 = (Button) findViewById(R.id.btnCAdd1);
        btnOpenDist = (Button) findViewById(R.id.waterAngleDist);
        btnOpenRec = (Button) findViewById(R.id.waterAngleRec);

        enableButtons(true, false);
        waterAngle.setText(prefs.getString("WaterControl", "0"));

        buttonDimUP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "DimmerControl");
                data.putString("DIMMER", "UP");
                serverReconfigure(gcm, data);
            }
        });

        buttonDimDN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "DimmerControl");
                data.putString("DIMMER", "DN");
                serverReconfigure(gcm, data);
            }
        });

        btnOpen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("OPEN", "100");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("CLOSE", "100");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
            }
        });

        btnOAdd10.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("OPEN", "10");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
            }
        });

        btnCAdd10.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("CLOSE", "10");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
            }
        });

        btnOAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("OPEN", "1");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
            }
        });

        btnCAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("CLOSE", "1");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
            }
        });

        btnOpenDist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("OPEN", "DIST");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
            }
        });

        btnOpenRec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle data = new Bundle();
                data.putString("message_type", "WaterControl");
                data.putString("OPEN", "REC");
                enableButtons(false, true);
                serverReconfigure(gcm, data);
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
                serverReconfigure(gcm, data);
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
