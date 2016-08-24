package com.mattmellor.smartshoegrapher;

import android.content.Context;
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
import android.widget.Toast;

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

    private Handler handler;

    private XYPlot plot;
    private GraphDataSource dataSource;
    private Redrawer redrawer;

    private ArrayList<DynamicSeries> seriesList;

    private boolean listenerExists = false;
    private int xBound = 10000;
    private boolean redrawerBeenInitialized = false;
    private int redraw_count = 0;
    private boolean applyBeenPressed = false;

    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);

        //Code until the end of this method is a place holder
        plot = (XYPlot) frag.findViewById(R.id.dynamic_plot);

        //Display only whole numbers in domain labels
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat((new DecimalFormat("0")));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat((new DecimalFormat("0")));
//        plot.setDomainLabel("Count");
//        plot.setRangeLabel("Sensor Values (V)");

        DynamicSeries sensor1 = new DynamicSeries(0 , "1", xBound);
//        DynamicSeries sensor2 = new DynamicSeries(1 , "2", xBound);
//        DynamicSeries sensor3 = new DynamicSeries(2 , "3", xBound);
//        DynamicSeries sensor4 = new DynamicSeries(3 , "4", xBound);
//        DynamicSeries sensor5 = new DynamicSeries(4 , "5", xBound);
//        DynamicSeries sensor6 = new DynamicSeries(5 , "6", xBound);

        //seriesList = new ArrayList<>(Arrays.asList(sensor1,sensor2, sensor3, sensor4, sensor5, sensor6));
        seriesList = new ArrayList<>(Arrays.asList(sensor1));

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.GREEN, null, null, null);
        //series1Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series1Format.getLinePaint().setStrokeWidth(2);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.BLUE, null, null, null);
        //series2Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series2Format.getLinePaint().setStrokeWidth(2);

        LineAndPointFormatter series3Format = new LineAndPointFormatter(Color.RED, null, null, null);
        //series3Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series3Format.getLinePaint().setStrokeWidth(2);

        LineAndPointFormatter series4Format = new LineAndPointFormatter(Color.LTGRAY, null, null, null);
        series4Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series4Format.getLinePaint().setStrokeWidth(2);

        LineAndPointFormatter series5Format = new LineAndPointFormatter(Color.MAGENTA, null, null, null);
        //series5Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series5Format.getLinePaint().setStrokeWidth(2);

        LineAndPointFormatter series6Format = new LineAndPointFormatter(Color.WHITE, null, null, null);
        //series6Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series6Format.getLinePaint().setStrokeWidth(2);


        plot.addSeries(sensor1, series1Format);         //Ways to make this faster???
//        plot.addSeries(sensor2, series2Format);
//        plot.addSeries(sensor3, series3Format);
//        plot.addSeries(sensor4, series4Format);
//        plot.addSeries(sensor5, series5Format);
//        plot.addSeries(sensor6, series6Format);

        plot.setRangeBoundaries(0, 4500, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, xBound, BoundaryMode.FIXED);
        plot.setLinesPerRangeLabel(3);
        plot.setLinesPerDomainLabel(3);


        dataSource = new GraphDataSource();
        dataSource.start();

        //redrawer = new Redrawer(plot, 30, false);

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
                    //Log.d("MATT!", aResponse);
                    if(dataValid(aResponse)){
                        //Log.d("MATT!", aResponse);
                        spliceDataAndAddData(aResponse);
//                        if(redraw_count == 5){
//                            plot.redraw();
//                            redraw_count = 0;
//                        }
//                        redraw_count++;
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


        //TODO: Change this to make it more robust
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
            String num = "";
            while(true){
                if(i < 6){ //This is the base case...add the first set of data
                    num = dataSplit[i];
                    try {
                        sensor.add(Integer.parseInt(num)); //TODO: Change this.....
                    }catch (Exception e){
                        //Corrupt data
                    }
                }else if((i) <= dataSize && i >= 6){ //Will start to get hit after the second time
                    num = dataSplit[i];
                    try {
                        sensor.add(Integer.parseInt(num));
                    }catch (Exception e){
                        //Corrupt data
                    }
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
            if(dataSize + sensor.size() > xBound){
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
        if(applyBeenPressed) {
            if (!listenerExists) {
                resetGraph();
                listenerExists = true;
                client = new UdpClient(hostname, remotePort, localPort, 45);
                client.setStreamData(true);
                UdpClient.UdpDataListener listener = client.new UdpDataListener(handler); //Handler has been waiting in the background for data(Since onCreateView)..It is the handler for this fragment
                listener.start();
                if (!redrawerBeenInitialized) {
                    redrawer = new Redrawer(plot, 30, true);
                    redrawerBeenInitialized = true;
                }else{
                    redrawer.run();
                }
            }
        }
        else{
            Context context = getActivity();
            CharSequence text = "Apply UDP Settings";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private void resetGraph(){
        for(DynamicSeries s: seriesList){
            s.resetData();
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
            redrawerBeenInitialized = false;
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


    public void setApplyBeenPressed(boolean pressedOrNot){
        applyBeenPressed = pressedOrNot;
    }

}
