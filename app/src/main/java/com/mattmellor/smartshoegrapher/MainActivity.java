package com.mattmellor.smartshoegrapher;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends FragmentActivity implements UdpSettingsFragment.OnDataPass, UdpStartStopFragment.PassStartStopData{

    private String hostname;
    private int remotePort;
    private int localPort;
    private UdpClient client;

    private UdpSettingsFragment settingsFragment;
    private GraphFragment graphFragment;
    private LinearLayout graphContainer;
    private GraphView graphHandle;
    private int xMin = 0;
    private int xMax = 10000;
    private ArrayList<LineGraphSeries<DataPoint>> series = new ArrayList<>();

    private ArrayList<Integer> xCounters = new ArrayList<>(Arrays.asList(0,0,0,0,0,0));
    private boolean listenerExists = false;
    private LineGraphSeries<DataPoint> sensor1;
    private LineGraphSeries<DataPoint> sensor2;
    private LineGraphSeries<DataPoint> sensor3;
    private LineGraphSeries<DataPoint> sensor4;
    private LineGraphSeries<DataPoint> sensor5;
    private LineGraphSeries<DataPoint> sensor6;
    private int xcounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.graph_fragment);

        settingsFragment = (UdpSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.client_fragment_layout); //This is null???
        settingsFragment.setActivityHandler(this.mHandler);

        graphContainer = (LinearLayout) findViewById(R.id.bg);
        graphHandle = (GraphView) findViewById(R.id.graph_test);

        graphHandle.setTitle("Value vs Count");
        graphHandle.getViewport().setXAxisBoundsManual(true);
        graphHandle.getViewport().setMaxX(xMax); //Want these to be dynamically programmed
        graphHandle.getViewport().setMinX(0);
        graphHandle.getViewport().setYAxisBoundsManual(true);
        graphHandle.getViewport().setMaxY(4500);
        graphHandle.getViewport().setMinY(0);

        sensor1 = new LineGraphSeries<>();
        sensor1.appendData(new DataPoint(2000,3000),false, xMax);
        sensor2 = new LineGraphSeries<>();
        sensor3 = new LineGraphSeries<>();
        sensor4 = new LineGraphSeries<>();
        sensor5 = new LineGraphSeries<>();
        sensor6 = new LineGraphSeries<>();

        series.add(sensor1);
        series.add(sensor2);
        series.add(sensor3);
        series.add(sensor4);
        series.add(sensor5);
        series.add(sensor6);

        for(LineGraphSeries<DataPoint> s: series){
            graphHandle.addSeries(s);
        }

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
            else if(aResponse.equals("fail")){
                settingsFragment.reportPingResult(false);
                Log.d("MATT!", "Unsucessful Ping");
            }
            else{ //message about the data
                //TODO: wrap this in asyn task??
                if (dataValid(aResponse)) {
                    aResponse = aResponse.replaceAll("\\s", "");
                    ArrayList<ArrayList<DataPoint>> data = spliceData(aResponse);

                    int j = 0;
                    while(j < data.get(0).size()){ //
                        int i = 0;
                        for(ArrayList<DataPoint> sensorList: data){
                            try {
                                series.get(i).appendData(sensorList.get(j), true, xMax); //get the ith data point
                            }catch (Exception e){
                                //Left blank
                            }
                            i++;
                        }
                        j++;
                    }
                }
            }
        }
    };

    private boolean dataValid(String data){
        return ((data.length() == 1350) );
    }

    /**
     *
     * @param data String of the entire data
     * @return ArrayList of ArrayLists.. Inner arrayLists are the
     * values of the individual sensors
     */
    private ArrayList<ArrayList<DataPoint>> spliceData(String data){
        ArrayList<ArrayList<DataPoint>> DataPoints = new ArrayList<>();
        String[] dataSplit = data.split(",");
        DataPoints.add(spliceToSensors(dataSplit, 1));
        DataPoints.add(spliceToSensors(dataSplit, 2));
        DataPoints.add(spliceToSensors(dataSplit, 3));
        DataPoints.add(spliceToSensors(dataSplit, 4));
        DataPoints.add(spliceToSensors(dataSplit, 5));
        DataPoints.add(spliceToSensors(dataSplit, 6));
        xcounter = xcounter + 45;
        return DataPoints;
    }

    /**
     *
     * @param dataSplit data to split into individual sensor array
     *                  must contain only string representations of numbers
     * @param sensorNumber which sensors to collect the data points of
     * @return ArrayList<DataPoint> List of DataPoint values for an individual
     * sensor
     */
    private ArrayList<DataPoint> spliceToSensors(String[] dataSplit, int sensorNumber){

        sensorNumber -= 1;
        int xcount = xcounter;
        ArrayList<DataPoint> sensor = new ArrayList<>();
        int i = sensorNumber;
        int dataSize = dataSplit.length - 1;

        while(true){
            DataPoint xy;
            String num = "1";
            if(i < 6){ //This is the base case...add the first set of data
                num = dataSplit[i];
                xy = new DataPoint(xcount, Integer.parseInt(num));
                //Log.d("MATT!",xy.toString());//This could throw an error there
                sensor.add(xy);
            }else if((i) <= dataSize && i >= 6){ //Will start to get hit after the second time
                num = dataSplit[i];
                xy = new DataPoint(xcount, Integer.parseInt(num));
                //Log.d("MATT!",xy.toString());
                sensor.add(xy);
            }else{
                xcount++;
                break;
            }
            i += 6;
            xcount++;
        }
        return sensor;
    }


    @Override //Passes Data from the UdpClient Fragment to main activity
    public void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        this.hostname = verifiedHostname;
        this.localPort = verifiedLocalPort;
        this.remotePort = verifiedRemotePort;
        //graphFragment.updateHostname(hostname);
        // graphFragment.updateLocalPort(localPort);
        // graphFragment.updateRemotePort(remotePort);
        Log.d("MATT!", "Verified Host: " + verifiedHostname);
        Log.d("MATT!", "Verified Local Port : " + verifiedLocalPort);
        Log.d("MATT!", "Verified Remote Port : " + verifiedRemotePort);
    }

    @Override
    public void onDataPassUdpReset(String defaultHostname, int defaultLocalPort, int defaultRemotePort) {
        this.hostname = defaultHostname;
        this.localPort = defaultLocalPort;
        this.remotePort = defaultRemotePort;
        //graphFragment.updateHostname(hostname);
        //graphFragment.updateLocalPort(localPort);
        //graphFragment.updateRemotePort(remotePort);
        Log.d("MATT!", "default hostname: " + defaultHostname);
        Log.d("MATT!", "default remotePort: " + defaultRemotePort);
        Log.d("MATT!", "default localPort: " + defaultLocalPort);
    }

    @Override
    public void startGraphing() {
        //graphFragment.startGraphing();
        if(!listenerExists) {
            listenerExists = true;
            client = new UdpClient(hostname, remotePort, localPort, 45);
            client.setStreamData(true);
            UdpClient.UdpDataListener listener = client.new UdpDataListener(mHandler);
            listener.start();
        }
    }

    @Override
    public void stopGraphing() {
        //graphFragment.stopGraphing();
        if (listenerExists) {
            client.setStreamData(false);
            listenerExists = false;
        }
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


    //TODO: implement an asynchronous task for working on graphing???
    //I think perhaps it is better to just




}
