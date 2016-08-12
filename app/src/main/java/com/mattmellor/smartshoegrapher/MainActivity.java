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


    //Fragments communicate down to the activity

    //UDP Connection settings
    private String hostname;
    private String remotePort;
    private String localPort;
    private UdpClient client;
    //Create the UDP client thread here as a result of the values given by the fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Connects up the fragments views
        Log.d("MATT!", "onCreateMethod");

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.setTitle("Volt Vs Count");

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 3),
                new DataPoint(1, 3),
                new DataPoint(2, 6),
                new DataPoint(3, 2),
                new DataPoint(4, 5)
        });
        graph.addSeries(series2);

        //Figure out how to pass an object to the fragments
        //Communicate between fragments

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

    @Override //Passes Data from the UdpClient Fragment
    public void onDataPassUdpSettings(String unverifiedHostname, String unverifiedLocalPort, String unverifiedRemotePort) {
        this.hostname = unverifiedHostname;
        this.localPort = unverifiedLocalPort;
        this.remotePort = unverifiedRemotePort;
        Log.d("MATT!", "Unverified Host: " + unverifiedHostname);
        Log.d("MATT!", "Unverified Local Port : " + unverifiedLocalPort);
        Log.d("MATT!", "Unverified Remote Port : " + unverifiedRemotePort);
    }

    @Override
    public void onDataPassUdpReset(String defaultHostname, String defaultLocalPort, String defaultRemotePort) {
        this.hostname = defaultHostname;
        this.localPort = defaultLocalPort;
        this.remotePort = defaultRemotePort;
        Log.d("MATT!", "default hostname: " + defaultHostname);
        Log.d("MATT!", "default remotePort: " + defaultRemotePort);
        Log.d("MATT!", "default localPort: " + defaultLocalPort);
    }


}
