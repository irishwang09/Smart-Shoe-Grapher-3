package com.mattmellor.smartshoegrapher;

import android.graphics.Color;
import android.graphics.DashPathEffect;
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

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

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
    private MyPlotUpdater plotUpdater;
    private GraphDataSource dataSource;

    private ArrayList<DynamicSeries> seriesList;


    private boolean listenerExists = false;
    private int xcounter = 0;
    private int xBound = 1000;

    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);
        graphContainer = (LinearLayout) frag.findViewById(R.id.graph);

        //Code until the end of this method is a place holder
        plot = (XYPlot) frag.findViewById(R.id.plot);
        //TODO: Make plot redraw be on a background thread
        //Change the setting^
        plotUpdater = new MyPlotUpdater(plot);
        dataSource = new GraphDataSource();

        //Display only whole numbers in domain labels
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM);


        DynamicSeries sensor1 = new DynamicSeries(dataSource, 0 , "Sensor 1", 10000);
        DynamicSeries sensor2 = new DynamicSeries(dataSource, 1 , "Sensor 2", 10000);
        DynamicSeries sensor3 = new DynamicSeries(dataSource, 2 , "Sensor 3", 10000);
        DynamicSeries sensor4 = new DynamicSeries(dataSource, 3 , "Sensor 4", 10000);
        DynamicSeries sensor5 = new DynamicSeries(dataSource, 4 , "Sensor 5", 10000);
        DynamicSeries sensor6 = new DynamicSeries(dataSource, 5 , "Sensor 6", 10000);

        seriesList = new ArrayList<>(Arrays.asList(sensor1,sensor2, sensor3, sensor4, sensor5, sensor6));


        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.rgb(0, 200, 0), null, null, null);
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        series1Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        plot.addSeries(sensor1, series1Format); //TODO Change the format
        plot.addSeries(sensor2, series1Format);
        plot.addSeries(sensor3, series1Format);
        plot.addSeries(sensor4, series1Format);
        plot.addSeries(sensor5, series1Format);
        plot.addSeries(sensor6, series1Format);

        dataSource.addObserver(plotUpdater); //Will make the plotUpdater update the graph when dataSource notifies it

        plot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        plot.setDomainStepValue(5);

        plot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        plot.setRangeStepValue(10);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("###.#"));

        // uncomment this line to freeze the range boundaries:
        plot.setRangeBoundaries(0, 10000, BoundaryMode.FIXED);

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        plot.getGraph().getDomainGridLinePaint().setPathEffect(dashFx);
        plot.getGraph().getRangeGridLinePaint().setPathEffect(dashFx);


        return frag;
    }


    //-------------Get Data, Manipulate Data & Notify PlotUpdater-----------
    public class GraphDataSource extends Thread{

        private MyObservable notifier = new MyObservable();

        class MyObservable extends Observable {
            @Override
            public void notifyObservers() {
                setChanged();
                super.notifyObservers();
            }
        }

        public void addObserver(Observer observer){
            notifier.addObserver(observer);
        }

        public void removeObserver(Observer observer){
            notifier.deleteObserver(observer);
        }

        public void run(){
            Looper.prepare();
            //Do something
            handler = new Handler(){
                public void handleMessage(Message msg){
                    String aResponse = msg.getData().getString("data"); //Data received
                    Log.d("Goyle!", aResponse);
                    if(dataValid(aResponse)){
                        spliceDataAndAddData(aResponse);
                        notifier.notifyObservers(); //tells the graph to redraw
                    }
                }
            };
            Looper.loop(); //Waits for messages?
        }

        /**
         *
         * @param data string of the udp data
         * @return true if the data isn't corrupted..aka the correct length
         */
        private boolean dataValid(String data){
            return ((data.length() == 1350) );
        }

        /**
         *
         * @param data String of the entire data
         * @return ArrayList of ArrayLists.. Inner arrayLists are the
         * values of the individual sensors
         */
        private void spliceDataAndAddData(String data){
            String[] dataSplit = data.split(",");
            addDataToSensors(spliceToSensors(dataSplit, 1),1);
            addDataToSensors(spliceToSensors(dataSplit, 2),2);
            addDataToSensors(spliceToSensors(dataSplit, 3),3);
            addDataToSensors(spliceToSensors(dataSplit, 4),4);
            addDataToSensors(spliceToSensors(dataSplit, 5),5);
            addDataToSensors(spliceToSensors(dataSplit, 6),6);
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
            int xcount = xcounter;
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
                    xcount++;
                    break;
                }
                i += 6;
                xcount++;
            }
            return sensor;
        }

        private void addDataToSensors(ArrayList<Integer> sensor, Integer sensorNumber){
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

    //-----------------Plot Updater-------------------

    // redraws a plot whenever an update is received:
    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot; //Plot has the new values in it
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw(); //Redraw the plot...
        }
    }


    //---------------Data Representation------------------

    //TODO: figure out how to implement XYSeries in a reasonable manner
    class DynamicSeries implements XYSeries{
        private GraphDataSource datasource;
        private int seriesIndex;
        private String title;
        private int bounds;
        public ArrayList<Integer> data = new ArrayList<>();

        public DynamicSeries(GraphDataSource datasource, int seriesIndex, String title, int size) {
            this.datasource = datasource;
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
            return datasource.getX(seriesIndex, index);
        }

        @Override
        public Number getY(int index) {
            return datasource.getY(seriesIndex, index);
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
        if(!listenerExists) {
            listenerExists = true;
            client = new UdpClient(hostname, remotePort, localPort, 45);
            client.setStreamData(true);
            dataSource.start(); //TODO: This might be a problem
            UdpClient.UdpDataListener listener = client.new UdpDataListener(handler); //When we press start graphing.. We pass handler object.
            listener.start();
        }
    }

    /**
     * Tell the data listener to stop listening to data
     */
    public void stopGraphing(){
        if (listenerExists) {
            client.setStreamData(false);
            listenerExists = false;
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
