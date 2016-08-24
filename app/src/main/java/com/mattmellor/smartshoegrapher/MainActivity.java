package com.mattmellor.smartshoegrapher;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends FragmentActivity implements UdpSettingsFragment.OnDataPass, UdpStartStopFragment.PassStartStopData{

    private String hostname;
    private int remotePort;
    private int localPort;

    private UdpSettingsFragment settingsFragment;
    private GraphFragment graphFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.graph_fragment);
    }


    /**
     * Handler to receive messages from different threads
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //Gets the task from the incoming Message object
            String aResponse = msg.getData().getString("message");
            if (aResponse.equals("success")) {
                settingsFragment.reportPingResult(true);
                Log.d("MATT!", "Succesful Ping");
            }
            else {
                settingsFragment.reportPingResult(false);
                Log.d("MATT!", "Unsucessful Ping");
            }
        }
    };



    @Override //Passes Data from the UdpClient Fragment to main activity
    public void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        this.hostname = verifiedHostname;
        this.localPort = verifiedLocalPort;
        this.remotePort = verifiedRemotePort;
        graphFragment.updateHostname(hostname);
        graphFragment.updateLocalPort(localPort);
        graphFragment.updateRemotePort(remotePort);
        Log.d("MATT!", "Verified Host: " + verifiedHostname);
        Log.d("MATT!", "Verified Local Port : " + verifiedLocalPort);
        Log.d("MATT!", "Verified Remote Port : " + verifiedRemotePort);
    }

    @Override
    public void onDataPassUdpReset(String defaultHostname, int defaultLocalPort, int defaultRemotePort) {
        this.hostname = defaultHostname;
        this.localPort = defaultLocalPort;
        this.remotePort = defaultRemotePort;
        graphFragment.updateHostname(hostname);
        graphFragment.updateLocalPort(localPort);
        graphFragment.updateRemotePort(remotePort);
        Log.d("MATT!", "default hostname: " + defaultHostname);
        Log.d("MATT!", "default remotePort: " + defaultRemotePort);
        Log.d("MATT!", "default localPort: " + defaultLocalPort);
    }

    @Override
    public void applyBeenPressed() {
        graphFragment.setApplyBeenPressed(true);
    }

    @Override
    public void updatesBeingMadeStopGraphing() {
        graphFragment.setApplyBeenPressed(false);
        graphFragment.stopGraphing();
    }

    @Override
    public void startGraphing() {
        graphFragment.startGraphing();
    }

    @Override
    public void stopGraphing() {
        graphFragment.stopGraphing();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.udp_settings_button) {
            FragmentManager fm = getSupportFragmentManager();
            settingsFragment = UdpSettingsFragment.newInstance();
            settingsFragment.setActivityHandler(this.mHandler);
            settingsFragment.show(fm, "MATT!");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //TODO: implement an asynchronous task for working on graphing???
    //I think perhaps it is better to just


}
