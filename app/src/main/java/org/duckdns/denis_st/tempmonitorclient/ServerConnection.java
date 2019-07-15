package org.duckdns.denis_st.tempmonitorclient;

import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Random;

public class ServerConnection {

    private static String rndId() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public static void sendToServer(GoogleCloudMessaging gcm, Bundle data){
        try {
            gcm.send("620914624750" + "@gcm.googleapis.com", rndId(), data);
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void serverReconfigure(final GoogleCloudMessaging gcm, final Bundle Data){
        new Thread() {
            @Override
            public void run() {sendToServer(gcm, Data);}
        }.start();
    }
}
