package com.mattmellor.smartshoegrapher;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

/**
 * Created by Matthew on 8/15/2016.
 * Fragment to hold a single graph and its underlying UDP Data Collection
 *
 */

public class GraphFragment extends Fragment {

    private UdpClient client;
    private String hostname;
    private int remotePort;
    private int localPort;
    private boolean listenerExists = false;
    private LinearLayout graphContainer;
    private GraphView graphHandle;
    private LineGraphSeries<DataPoint> series;
    private Handler handler;
    private int xcounter = 0;
    //private Handler defined below

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);

        graphContainer = (LinearLayout) frag.findViewById(R.id.graph);

        graphHandle = new GraphView(getContext());
        graphHandle.setTitle("Value vs Count");
        graphHandle.getViewport().setXAxisBoundsManual(true);
        graphHandle.getViewport().setMaxX(6000); //Want these to be dynamically programmed
        graphHandle.getViewport().setMinX(0);
        graphHandle.getViewport().setYAxisBoundsManual(true);
        graphHandle.getViewport().setMaxY(4500);
        graphHandle.getViewport().setMinY(0);

        series = new LineGraphSeries<DataPoint>();
        graphHandle.addSeries(series);
        graphContainer.addView(graphHandle);

        return frag;
    }

    /**
     * Update the remote port
     * @param remotePort of udp server
     */
    public void updateRemotePort(int remotePort){
        this.remotePort = remotePort;
    }

    /**
     * Update the local port
     * @param localPort of udp server
     */
    public void updateLocalPort(int localPort){
        this.localPort = localPort;
    }

    /**
     * Update the hostname
     * @param hostname of udp server
     */
    public void updateHostname(String hostname){
        this.hostname = hostname;
    }

    public void startGraphing(){
        if(!listenerExists) {
            listenerExists = true;
            client = new UdpClient(hostname, remotePort, localPort, 45);
            client.setStreamData(true);
            GraphLooper graphLooper = new GraphLooper();
            graphLooper.start(); //initializes the handler in the next line (below)
            UdpClient.UdpDataListener listener = client.new UdpDataListener(handler);
            listener.start();
        }
    }

    public void stopGraphing(){
        if (listenerExists) {
            client.setStreamData(false);
            listenerExists = false;
        }
    }


    //Nested Class for Graphing Values
    //TODO determine if this is necessary...
    //TODO: Look at the two examples to see how they handle the graphing...
    public class GraphLooper extends Thread{

        //TODO: What is this thread doing???
        // Is this thread necessary or can the main thread do the graphing????
        public void run(){
            //Do something
            handler = new Handler(){
                public void handleMessage(Message msg){
                    String aResponse = msg.getData().getString("data"); //Data received
                    //TODO: How to handle the data
                    //resetData
                    //appendData
                }
            };
            Looper.loop(); //Waits for messages?
            //^Run the message queue in this thread
        }

        public void startGraphing(){

        }

        //TODO:
        public ArrayList<ArrayList<DataPoint>> spliceData(String data){
            throw new RuntimeException("Unimplemented");
        }

    }

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


}
