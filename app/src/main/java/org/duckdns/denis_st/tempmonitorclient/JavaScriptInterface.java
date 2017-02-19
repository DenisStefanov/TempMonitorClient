package org.duckdns.denis_st.tempmonitorclient;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by Den on 2/17/2017.
 */

public class JavaScriptInterface {
    Context mContext;
    String mRegID;

    /** Instantiate the interface and set the context */
    JavaScriptInterface(Context c) {
        mContext = c;
    }

    public void setRegID(String RegID) {
        mRegID = RegID;
    }

    @JavascriptInterface
    public String getRegID() {
        return mRegID;
    }
}