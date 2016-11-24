package com.android.amitrai.demowaki;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EditText editText1, editText2;
    Button button1;
    SeekBar volume;
    TextView textView1;
    String ip;
    int port;
    MediaStreamServer mss;
    MediaStreamClient msc;
    private String TAG = getClass().getSimpleName();
    private boolean speeaking = false;

    public static String MYIP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void init(){
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
        String ipadd = getLocalIpAddress();
        MYIP = ipadd;
        editText1.setText(ipadd);

        ip = editText1.getText().toString();
        port = Integer.valueOf(editText2.getText().toString());

        mss = new MediaStreamServer(MainActivity.this, port);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button1.getText().toString().equals("Start")) {
                    button1.setText("Stop");

                    if(ip.equals("127.0.0.1") || ip.equals("0.0.0.0")) {
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
    }


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
}
