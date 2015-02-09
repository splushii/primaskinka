package com.example.primaskinka;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String EXTRA_MESSAGE = "com.example.primaskinka.MESSAGE";

	private static final String DEBUG_TAG = "HttpExample";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkInterwebz();
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

//	public void sendMessage(View view) {
//		Intent intent = new Intent(this, DisplayMessageActivity.class);
//
//		intent.putExtra(EXTRA_MESSAGE, message);
//		startActivity(intent);
//	}
	
	private void checkInterwebz() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = connMgr.getActiveNetworkInfo();
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (networkinfo != null && networkinfo.isConnected() && networkinfo.getTypeName().equals("WIFI") && (wifiInfo.getSSID().equals("\"herpderp\"") || wifiInfo.getSSID().equals("\"floff\""))) {
			AsyncTask<String, Void, String> connection = new DownloadWebpageTask()
					.execute("http://192.168.1.123/status.php");
		} else {
			Intent intent = new Intent(this, DisplayMessageActivity.class);
			intent.putExtra(EXTRA_MESSAGE, "Kontrollera internetanslutning. Wifi: " + networkinfo.getTypeName().equals("WIFI") + " wifiSSID: " + wifiInfo.getSSID().equals("herpderp"));
			startActivity(intent);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		checkInterwebz();
	}

	// Uses AsyncTask to create a task away from the main UI thread. This task
	// takes a
	// URL string and uses it to create an HttpUrlConnection. Once the
	// connection
	// has been established, the AsyncTask downloads the contents of the webpage
	// as
	// an InputStream. Finally, the InputStream is converted into a string,
	// which is
	// displayed in the UI by the AsyncTask's onPostExecute method.
	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			// params comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			final Context contextko = getApplicationContext();
			LinearLayout vlayout = new LinearLayout(contextko);
			vlayout.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams textpar = new LinearLayout.LayoutParams(
					0, LinearLayout.LayoutParams.WRAP_CONTENT);
			textpar.weight = 1;
			LinearLayout.LayoutParams buttpar = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			Scanner scan = new Scanner(result);
			int switchcount = Integer.valueOf(scan.nextLine());
			
			for (int i = 0; i < switchcount; i++) {
				String tmp = scan.nextLine();
				final String[] temp = tmp.split("\t");
				
				TextView charizard = new TextView(contextko);
				charizard.setTextSize(20);
				charizard.setTextColor(Color.BLACK);
				charizard.setText("\t" + temp[0]);
				LinearLayout hlayout = new LinearLayout(contextko);
				hlayout.setOrientation(LinearLayout.HORIZONTAL);
				
				hlayout.addView(charizard, textpar);
				Switch toggle = new Switch(contextko);
				if(temp[1].equals("ON")){
					toggle.setChecked(true);
				}
				toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo networkinfo = connMgr.getActiveNetworkInfo();
						if (networkinfo != null && networkinfo.isConnected()) {
							
							AsyncTask<String, Void, String> connection = new ToggleSwitch().execute("http://192.168.1.123/toggle.php?mode=" + (temp[1].equals("ON") ? "off" : "on") + "&name=" + temp[0]);
							temp[1] = temp[1].equals("ON") ? "OFF" : "ON";
						} else {
							Intent intent = new Intent(contextko, DisplayMessageActivity.class);
							intent.putExtra(EXTRA_MESSAGE, "Kontrollera internetanslutning");
							startActivity(intent);
						}
					}
				});
				hlayout.addView(toggle, buttpar);
				vlayout.addView(hlayout);
			}
			scan.close();
			setContentView(vlayout);
		}
	}

	private class ToggleSwitch extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			// params comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			
		}
	}
	
	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private String downloadUrl(String myurl) throws IOException {
		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.
		int len = 500;

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d(DEBUG_TAG, "The response is: " + response);
			is = conn.getInputStream();

			// Convert the InputStream into a string
			String contentAsString = readIt(is, len);
			return contentAsString;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Reads an InputStream and converts it to a String.
	public String readIt(InputStream stream, int len) throws IOException,
			UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}
}
