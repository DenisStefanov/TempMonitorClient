package org.duckdns.denis_st.tempmonitorclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ServerLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String receivedStr;
        Bundle extras = getIntent().getExtras();
        receivedStr = extras.getString("SERVER_LOG_DATA");
        if (receivedStr != null){
            TextView logView = (TextView) findViewById(R.id.textView2);
            if (logView !=null)
                logView.setText(receivedStr);
        }
        setContentView(R.layout.activity_server_log);
    }


}
