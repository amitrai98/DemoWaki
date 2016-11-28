package com.android.amitrai.demowaki;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.ServerSocket;
import java.net.Socket;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
	EditText editText1, editText2;
	Button button1;
	SeekBar volume;
	TextView textView1;
	String ip;
	int port;
	MediaStreamServer mss;
	MediaStreamClient msc;
	private ServerSocket serverSocket;
	Socket socket;
	public static String MYIP = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// initialize layout variables
		editText1 = (EditText) findViewById(R.id.editText1);
		editText2 = (EditText) findViewById(R.id.editText2);
		button1 = (Button) findViewById(R.id.button1);
		volume = (SeekBar) findViewById(R.id.volume);
		volume.setMax(100);
		volume.setProgress(100);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView1.append("Current IP: "+getLocalIpAddress()+"\n");
		editText1.setText(getLocalIpAddress());
		port = Integer.valueOf(editText2.getText().toString());
		mss = new MediaStreamServer(MainActivity.this, port);
		ip = getLocalIpAddress();
		MYIP = ip;
		textView1.append("Starting server\n");
//		mss = new MediaStreamServer(MainActivity.this, port);

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					ServerSocket serverSocket = new ServerSocket(port);
//					while (true){
//						serverSocket.accept();
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();

		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(button1.getText().toString().equals("Start")) {
					button1.setText("Stop");
					ip = editText1.getText().toString();
					port = Integer.valueOf(editText2.getText().toString());
					if(!ip.isEmpty()){//.equals("127.0.0.1") || ip.equals("0.0.0.0")) {
						textView1.append("Starting server\n");
//						mss = new MediaStreamServer(MainActivity.this, port);
					}
					if(!ip.equals("0.0.0.0")) {
						textView1.append("Starting client, " + ip + ":" + port + "\n");
						msc = new MediaStreamClient(MainActivity.this, ip, port);
					}
				}
				else if(button1.getText().toString().equals("Stop")) {
					button1.setText("Start");
					if(mss!=null) {
						textView1.append("Stopping server\n");
						mss.stop();
					}
					if(msc!=null) {
						textView1.append("Stopping client\n");
						msc.stop();
					}
				}
			}
		});

		volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				float vol = (float)(arg0.getProgress())/(float)(arg0.getMax());
				if(msc!=null) msc.setVolume(vol, vol);
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
		});

		BroadcastReceiver receiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals("tw.rascov.MediaStreamer.ERROR")) {
					textView1.append("Error: " + intent.getStringExtra("msg") + "\n");
					button1.setText("Start");
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction("tw.rascov.MediaStreamer.ERROR");
		registerReceiver(receiver, filter);



//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				port = Integer.valueOf(editText2.getText().toString());
//
//				try {
//					serverSocket = new ServerSocket(port);
//				} catch (IOException e) {
//					e.printStackTrace();
//
//				}
//
//				while (true) {
//					try {
//						socket = serverSocket.accept();
//					} catch (IOException e) {
//						System.out.println("I/O error: " + e);
//					}
//					// new threa for a client
//					new EchoThread(socket).start();
//				}
//			}
//		}).start();



	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//			System.exit(0);
//		}
//		return super.onKeyDown(keyCode, event);
//	}

//	public String getLocalIpAddress() {
//		try {
//			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//				NetworkInterface intf = en.nextElement();
//				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//					InetAddress inetAddress = enumIpAddr.nextElement();
//					if (!inetAddress.isLoopbackAddress()) {
//						return inetAddress.getHostAddress().toString();
//					}
//				}
//			}
//		}
//		catch (SocketException e) { e.printStackTrace(); }
//		return null;
//	}


	public String getLocalIpAddress() {
		try {
			WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
			Log.e(TAG, ""+ipAddress);
			return ipAddress;
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
		}
		return null;
	}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			// Do your thing
			EchoThread.isPlaying = false;

			if (editText1.getText().toString().equalsIgnoreCase(getLocalIpAddress()))
				return false;

			textView1.append("Starting client, " + ip + ":" + port + "\n");
			msc = new MediaStreamClient(MainActivity.this, ip, port);
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			MediaStreamClient.isRecording = false;
			if (msc != null)
				msc.stop();


			return true;

		}else if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			System.exit(0);
		}
		return super.onKeyDown(keyCode, event);
	}


}