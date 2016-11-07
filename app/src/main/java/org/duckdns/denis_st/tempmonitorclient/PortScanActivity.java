package org.duckdns.denis_st.tempmonitorclient;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;

public class PortScanActivity extends AppCompatActivity {
    ArrayList<String> scanListvalues;
    private ArrayAdapter<String> adapter;
    private SharedPreferences prefs;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();

    ProgressDialog progressBar;

    private void showToastinMain(final String text) {
        PortScanActivity.this.runOnUiThread(new Runnable() {
            public void run() {Toast.makeText(PortScanActivity.this, text, Toast.LENGTH_LONG).show();}
        });
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final ListView scanList = (ListView) findViewById(R.id.scan_list);

        scanListvalues = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, scanListvalues);
        scanList.setAdapter(adapter);

        scanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String  ServerIP  = (String) scanList.getItemAtPosition(position);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("ServerIP", ServerIP);
                editor.commit();
                Toast.makeText(PortScanActivity.this, "Server IP configured to " + ServerIP, Toast.LENGTH_LONG).show();
            }

        });

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Scanning network ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(255);
        progressBar.show();

        new Thread() {
            @Override
            public void run() {ScanMyNet(22, 200);}}.start();
    }

    private boolean ScanMyNet(int port, int timeout) {
        try {
            String LocalIP = getLocalIpAddress();

            for (int lastOctet = 1; lastOctet < 255; lastOctet++) {
                Socket socket = new Socket();
                String[] tmpStr = LocalIP.split("\\.");
                String RemoteIP = tmpStr[0] + "." + tmpStr[1] + "." + tmpStr[2] + "." +  Integer.toString(lastOctet);
                try {
                    socket.connect(new InetSocketAddress(RemoteIP, port), timeout);
                    System.out.println("Trying " + RemoteIP + " Success!!! ");
                    scanListvalues.add(RemoteIP);
                    updateUIControls();
                    socket.close();
                }
                catch (Exception e) {
                    socket.close();
                    //System.out.println("No luck with " + RemoteIP + " " + e.toString());
                }
                // Update the progress bar
                progressBarStatus = lastOctet;
                progressBarHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(progressBarStatus);
                    }
                });
            }
            progressBar.dismiss();
            showToastinMain("Found " + Integer.toString(scanListvalues.size()) + " SSH services");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());

                    // for getting IPV4 format
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {

                        String ip = inetAddress.getHostAddress().toString();
                        System.out.println("ip---::" + ip);
                        // return inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void updateUIControls() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();}});}
    }