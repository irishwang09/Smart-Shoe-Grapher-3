package com.mattmellor.smartshoegrapher;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

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
    private boolean listenerExists = false;
    private LinearLayout graphContainer;
    private Handler handler;
    private int xcounter = 0;
    private XYPlot plot;
    //private Handler defined below

    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);
        graphContainer = (LinearLayout) frag.findViewById(R.id.graph);

        //Code until the end of this method is a place holder
        plot = (XYPlot) frag.findViewById(R.id.plot);

        // create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
        Number[] series2Numbers = {5, 2, 10, 5, 20, 10, 40, 20, 80, 40};

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getContext(),
                R.xml.line_point_formatter_with_labels);

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getContext(),
                R.xml.line_point_formatter_with_labels_2);

        // add an "dash" effect to the series2 line:
        series2Format.getLinePaint().setPathEffect(
                new DashPathEffect(new float[] {

                        // always use DP when specifying pixel sizes, to keep things consistent across devices:
                        PixelUtils.dpToPix(20),
                        PixelUtils.dpToPix(15)}, 0));

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        series2Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.addSeries(series2, series2Format);

        // reduce the number of range labels

        //plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
       // plot.getGraphWidget().setDomainLabelOrientation(-45);

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
            GraphLooper graphLooper = new GraphLooper();
            graphLooper.start(); //initializes the handler in the next line (below)
            UdpClient.UdpDataListener listener = client.new UdpDataListener(handler);
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


    }

    /**
     *
     * @param data string of the udp data
     * @return true if the data isn't corrupted..aka the correct length
     * TODO: add a regex test (again...)
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
    //private ArrayList<ArrayList<DataPoint>> spliceData(String data){
    private void spliceData(String data){
        //ArrayList<ArrayList<DataPoint>> DataPoints = new ArrayList<>();
        String[] dataSplit = data.split(",");
//        DataPoints.add(spliceToSensors(dataSplit, 1));
//        DataPoints.add(spliceToSensors(dataSplit, 2));
//        DataPoints.add(spliceToSensors(dataSplit, 3));
//        DataPoints.add(spliceToSensors(dataSplit, 4));
//        DataPoints.add(spliceToSensors(dataSplit, 5));
//        DataPoints.add(spliceToSensors(dataSplit, 6));
        xcounter = xcounter + 45;
        //return DataPoints;
    }

    /**
     *
     * @param dataSplit data to split into individual sensor array
     *                  must contain only string representations of numbers
     * @param sensorNumber which sensors to collect the data points of
     * @return ArrayList<DataPoint> List of DataPoint values for an individual
     * sensor
     */
    //private ArrayList<DataPoint> spliceToSensors(String[] dataSplit, int sensorNumber){
    private void spliceToSensors(String[] dataSplit, int sensorNumber){
        sensorNumber -= 1;
        int xcount = xcounter;
        //ArrayList<DataPoint> sensor = new ArrayList<>();
        int i = sensorNumber;
        int dataSize = dataSplit.length - 1;

        while(true){
            //DataPoint xy;
            String num = "1";
            if(i < 6){ //This is the base case...add the first set of data
                num = dataSplit[i];
                //xy = new DataPoint(xcount, Integer.parseInt(num));
                //Log.d("MATT!",xy.toString());//This could throw an error there
               // sensor.add(xy);
            }else if((i) <= dataSize && i >= 6){ //Will start to get hit after the second time
                num = dataSplit[i];
                //xy = new DataPoint(xcount, Integer.parseInt(num));
                //Log.d("MATT!",xy.toString());
                //sensor.add(xy);
            }else{
                xcount++;
                break;
            }
            i += 6;
            xcount++;
        }
        //return sensor;
    }


}
