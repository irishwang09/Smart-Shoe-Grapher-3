package com.mattmellor.smartshoegrapher;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends FragmentActivity implements UdpSettingsFragment.OnDataPass{

    //UDP Connection settings
    private String hostname;
    private int remotePort;
    private int localPort;
    private UdpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Connects up the fragments views
        Log.d("MATT!", "onCreateMethod");
    }


    @Override //Passes Data from the UdpClient Fragment
    public void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        this.hostname = verifiedHostname;
        this.localPort = verifiedLocalPort;
        this.remotePort = verifiedRemotePort;
        Log.d("MATT!", "Verified Host: " + verifiedHostname);
        Log.d("MATT!", "Verified Local Port : " + verifiedLocalPort);
        Log.d("MATT!", "Verified Remote Port : " + verifiedRemotePort);
        //TODO: Notify the start and stop fragment & and the graph fragment of the changes
    }

    @Override
    public void onDataPassUdpReset(String defaultHostname, int defaultLocalPort, int defaultRemotePort) {
        this.hostname = defaultHostname;
        this.localPort = defaultLocalPort;
        this.remotePort = defaultRemotePort;
        Log.d("MATT!", "default hostname: " + defaultHostname);
        Log.d("MATT!", "default remotePort: " + defaultRemotePort);
        Log.d("MATT!", "default localPort: " + defaultLocalPort);
        //TODO: Notify the start and stop fragment & the graph fragment of the changes
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
