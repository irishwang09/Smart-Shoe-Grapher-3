package com.mattmellor.smartshoegrapher;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;

import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;


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

    private LinearLayout graphContainer;
    private Handler handler;

    private XYPlot plot;
    private GraphDataSource dataSource;
    private Redrawer redrawer;

    private ArrayList<DynamicSeries> seriesList;


    private boolean listenerExists = false;
    private int xcounter = 0;
    private int xBound = 3000;
    private boolean redrawerBeenPressed = false;

    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);
        graphContainer = (LinearLayout) frag.findViewById(R.id.graph);

        //Code until the end of this method is a place holder
        plot = (XYPlot) frag.findViewById(R.id.dynamic_plot);


        //Display only whole numbers in domain labels
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat((new DecimalFormat("0")));


        DynamicSeries sensor1 = new DynamicSeries(0 , "S1", 3000);
//        DynamicSeries sensor2 = new DynamicSeries(1 , "S2", 100000);
//        DynamicSeries sensor3 = new DynamicSeries(2 , "S3", 100000);
//        DynamicSeries sensor4 = new DynamicSeries(3 , "S4", 100000);
//        DynamicSeries sensor5 = new DynamicSeries(4 , "S5", 100000);
//        DynamicSeries sensor6 = new DynamicSeries(5 , "S6", 100000);

        //seriesList = new ArrayList<>(Arrays.asList(sensor1,sensor2, sensor3, sensor4, sensor5, sensor6));
        seriesList = new ArrayList<>(Arrays.asList(sensor1));

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.rgb(0, 200, 0), null, null, null);
        series1Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series1Format.getLinePaint().setStrokeWidth(2);

        plot.addSeries(sensor1, series1Format);
//        plot.addSeries(sensor2, series1Format);
//        plot.addSeries(sensor3, series1Format);
//        plot.addSeries(sensor4, series1Format);
//        plot.addSeries(sensor5, series1Format);
//        plot.addSeries(sensor6, series1Format);

        plot.setRangeBoundaries(0, 4500, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 3000, BoundaryMode.FIXED);
        plot.setLinesPerRangeLabel(3);
        plot.setLinesPerDomainLabel(5);


        dataSource = new GraphDataSource();
        dataSource.start();

        redrawer = new Redrawer(plot, 30, false);

        return frag;
    }


    //-------------Get Data, Manipulate Data & Notify PlotUpdater-----------
    public class GraphDataSource extends Thread{

        public void run(){
            Looper.prepare();
            //Do something
            handler = new Handler(){
                public void handleMessage(Message msg){
                    String aResponse = msg.getData().getString("data"); //Data received
                    //Log.d("Goyle!", aResponse);
                    if(dataValid(aResponse)){
                        spliceDataAndAddData(aResponse);
                    }
                }
            };
            Looper.loop();
        }

        /**
         *
         * @param data string of the udp data
         * @return true if the data isn't corrupted..aka the correct length
         */
        private boolean dataValid(String data){
            return ((data.length() == 1350)); //TODO: implement a better regular expression
        }

        /**
         *
         * @param data String of the entire data
         * @return ArrayList of ArrayLists.. Inner arrayLists are the
         * values of the individual sensors
         */
        private void spliceDataAndAddData(String data){
            data = data.replaceAll("\\s", "");
            String[] dataSplit = data.split(",");
            addDataToSensors(spliceToSensors(dataSplit, 1),1);
//            addDataToSensors(spliceToSensors(dataSplit, 2),2);
//            addDataToSensors(spliceToSensors(dataSplit, 3),3);
//            addDataToSensors(spliceToSensors(dataSplit, 4),4);
//            addDataToSensors(spliceToSensors(dataSplit, 5),5);
//            addDataToSensors(spliceToSensors(dataSplit, 6),6);
        }

        /**
         *
         * @param dataSplit data to split into individual sensor array
         *                  must contain only string representations of numbers
         * @param sensorNumber which sensors to collect the data points of
         * @return ArrayList<DataPoint> List of DataPoint values for an individual
         * sensor
         */
        private ArrayList<Integer> spliceToSensors(String[] dataSplit, int sensorNumber){
            sensorNumber -= 1;
            ArrayList<Integer> sensor = new ArrayList<>();
            int i = sensorNumber;
            int dataSize = dataSplit.length - 1;

            while(true){
                //DataPoint xy;
                String num = "1";
                if(i < 6){ //This is the base case...add the first set of data
                    num = dataSplit[i];
                    sensor.add(Integer.parseInt(num));
                }else if((i) <= dataSize && i >= 6){ //Will start to get hit after the second time
                    num = dataSplit[i];
                    sensor.add(Integer.parseInt(num));
                }else{
                    break;
                }
                i += 6;
            }
            return sensor;
        }

        private void addDataToSensors(ArrayList<Integer> sensor, Integer sensorNumber){
            sensorNumber--;
            int dataSize = seriesList.get(sensorNumber).data.size();
            if(dataSize + sensor.size() > xBound){  //TODO Double check this...
                seriesList.get(sensorNumber).resetData();
            }
            seriesList.get(sensorNumber).data.addAll(sensor);
        }

        public Number getX(int series, int index){
            return index;
        }

        public Number getY(int series, int index){
            return seriesList.get(series).data.get(index); //TODO: Double check this so no null pointer ref
        }

    }



    //---------------Data Representation------------------

    class DynamicSeries implements XYSeries{
        private int seriesIndex;
        private String title;
        private int bounds;
        public ArrayList<Integer> data = new ArrayList<>();

        public DynamicSeries(int seriesIndex, String title, int size) {
            this.seriesIndex = seriesIndex;
            this.bounds = size;
            this.title = title;
            data.add(200);
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data.get(index);
        }

        public void resetData(){
            data.clear();
            data.add(0);
        }

    }


    //---------------GraphFragment methods---------------

    /**
     * If there isn't already a data listener create one
     * and start listening to data. Data listener notifies the UI thread
     * each time it has a new data packet full of valid data
     * UI thread then graphes it (not implemented)
     */
    public void startGraphing(){
        if(!listenerExists) {
            listenerExists = true;
            client = new UdpClient(hostname, remotePort, localPort, 45);
            client.setStreamData(true);
            UdpClient.UdpDataListener listener = client.new UdpDataListener(handler); //When we press start graphing.. We pass handler object.
            listener.start();
            if(!redrawerBeenPressed){
                redrawer.start();
                redrawerBeenPressed = true;
            }

        }
    }

    /**
     * Tell the data listener to stop listening to data
     */
    public void stopGraphing(){
        if (listenerExists) {
            client.setStreamData(false);
            listenerExists = false;
            redrawer.finish();
            redrawerBeenPressed = false;
        }
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


}
