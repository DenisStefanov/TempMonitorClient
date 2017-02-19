package org.duckdns.denis_st.tempmonitorclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private GcmRegistrar tmgcm;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private Menu MainMenu = null;
    private String RegID = null;
    private JavaScriptInterface jsInterface = null;

    private void GCMRegister() {
        System.out.println("Registering with GCM...");
        RegID = tmgcm.register();
        if (!RegID.isEmpty()) {
            System.out.println("Got GCM reg id. [" + RegID + "]");
            if (jsInterface != null) {
                jsInterface.setRegID(RegID);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);

        WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String nameSSID = wifiInfo.getSSID();
        if (nameSSID.equals("\"DIR-320NRU - 56\"")) {
            url = prefs.getString("ServerURLInt", null);
        }
        else {
            url = prefs.getString("ServerURLExt", null);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        System.out.println("Registering JS interface...");
        jsInterface = new JavaScriptInterface(this);

        tmgcm = new GcmRegistrar(getApplicationContext());
        // Check device for Play Services APK.
        if (tmgcm.checkPlayServices(this)) {
            new Thread() {
                @Override
                public void run() {GCMRegister();}}.start();
            } else {
            System.out.println("No valid Google Play Services APK found.");
        }

        WebView webTempmon;
        webTempmon = (WebView) findViewById(R.id.webTempmon);

        webTempmon.addJavascriptInterface(jsInterface, "Android");
        webTempmon.getSettings().setJavaScriptEnabled(true);
        webTempmon.getSettings().setAppCacheEnabled(false);
        webTempmon.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webTempmon.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        //Map<String, String> extraHeaders = new HashMap<String, String>();
        //extraHeaders.put("orientation",Integer.toString(this.getResources().getConfiguration().orientation));
        webTempmon.loadUrl(url + "/getTempReadingsChart/?orientation=" + Integer.toString(this.getResources().getConfiguration().orientation));
        //webTempmon.clearCache(true);
    }

    @Override
    protected void onResume() { super.onResume(); }

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
        return super.onOptionsItemSelected(item);
    }
}
