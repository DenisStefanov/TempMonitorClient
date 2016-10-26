package org.duckdns.denis_st.tempmonitorclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import com.jcraft.jsch.*;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class tmonService extends Service {
	private Bundle extras;
	private Timer timer = new Timer();
	private boolean inited = false;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		extras = intent.getExtras();
		if (!inited) {
			//timer.scheduleAtFixedRate(new periodicUpdate(), 0, 10000);
			inited = true;
		} else {
			new Thread(thread).start();
		}
		return Service.START_NOT_STICKY;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
	    filter.addAction("ServiceReturnIntent");
	    registerReceiver(receiver, filter);
		showNotification();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void showNotification() {
		// prepare intent which is triggered if the
		// notification is selected

		Intent intentOpen = new Intent(this, MainActivity.class);
		PendingIntent pIntentOpen = PendingIntent.getActivity(this, 0, intentOpen, 0);

		Intent intentKill = new Intent("SelfKillIntent");
		PendingIntent pIntentKill = PendingIntent.getService(this, 0, intentKill, 0);
		
		// build notification
		// the addAction re-use the same intent to keep the example short
		Notification n  = new Notification.Builder(this)
		        .setContentTitle("Temp Monitor")
		        .setContentText("running")
		        .setSmallIcon(R.drawable.icon)
		        .setContentIntent(pIntentOpen)
		        .setAutoCancel(true)
		        .addAction(R.drawable.iconsmall, "Kill", pIntentKill).build();
		    
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(0, n); 
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() { 
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			System.out.println("Service: Incoming broadcast action = " + action);
		}
	};

	    
	    
	private class periodicUpdate extends TimerTask {
		  @Override
		  public void run() {
			  //new DownloadDataTask().execute("");
			  new Thread(thread).start();
		  }
	}
	private class DownloadDataTask extends AsyncTask<String, Void, String> {
		/** The system calls this to perform work in a worker thread and
		 * delivers it the parameters given to AsyncTask.execute() */
		@Override
		protected String doInBackground(String... str) {
			try {
				//String str = Connect(extras.getString("url"));
				return ConnectSSH(extras.getString("host"), extras.getString("path"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}

		@Override
		protected void onPostExecute(String str) {
			try {
				System.out.println("Received = " + str);
				Intent retIntent = new Intent("ServiceReturnIntent");
				retIntent.putExtra("str", str);
				sendBroadcast(retIntent);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	Thread thread = new Thread()
	{
	    @Override
	    public void run() {
	        try {
	    		//String str = Connect(extras.getString("url"));
	    		String str = ConnectSSH(extras.getString("host"), extras.getString("path"));
	    		// System.out.println("Received = " + str);
	    		Intent retIntent = new Intent("ServiceReturnIntent");
	    		retIntent.putExtra("str", str);
	    		sendBroadcast(retIntent);	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	};

		
	private static String Connect(String url) {
		String result = null;
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
		HttpConnectionParams.setSoTimeout(httpParameters, 6000);

		DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);

		// Execute the request
		HttpResponse response;
		try {
			System.out.println("Trying = " + url);
			response = httpclient.execute(httpget);
			// Examine the response status
			Log.i("Praeda", response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);
				// now you have the string representation of the HTML request
				instream.close();
			}

		} catch (Exception e) {
			System.out.println("Exception " + e.toString());
		}
		return result;
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + '\n');
				// tempRecord.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static String ConnectSSH(String host, String path){
		String result = null;
		try{
			JSch jsch=new JSch();

			Session session=jsch.getSession("pi", host, 22);
			session.setPassword("raspberry");

			session.setConfig("StrictHostKeyChecking", "no");

			session.connect(5000); // making a connection with timeout.

			Channel channel=session.openChannel("exec");

			InputStream in=channel.getInputStream();			 
			//((ChannelExec)channel).setErrStream(System.err);

			((ChannelExec)channel).setCommand("tail -n 100 " + path);

			channel.connect(5000);

			byte[] tmp=new byte[1024];
			while(true) {
				int i=in.read(tmp, 0, 1024);
				if(i<0)break;
				//System.out.print(result = result + new String(tmp, 0, i));
			}
			Thread.sleep(500);
			System.out.println("exit-status: "+channel.getExitStatus());
			channel.disconnect();
			session.disconnect();			

		}
		catch(Exception e){
			System.out.println(e);
		}
		return result;
	}

}