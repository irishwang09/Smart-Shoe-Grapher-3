package Fragments;

import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mattmellor.smartshoegrapher.R;
import com.mattmellor.smartshoegrapher.UdpClient;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.NumericAxis;
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.drawing.utility.ColorUtil;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import SciChartUserClasses.SciChartBuilder;


/**
 * Created by Matthew on 8/15/2016.
 * Fragment to hold a single graph and its underlying UDP Data Collection
 *
 */

public class GraphFragment extends Fragment {

    //UDP Settings
    private UdpClient client;
    private String hostname;
    private int remotePort;
    private int localPort;

    //Allows Communication With Other Threads Outside GraphFragment class
    private Handler handler;

    private boolean listenerExists = false;
    private int xBound = 10000; //We want to be able to change this
    private int yBound = 5000;
    private boolean applyBeenPressed = false;

    private SciChartSurface plotSurface;
    private GraphDataSource dataSource;
    protected final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

    //The following are the lists that we actually add the udp sensor data to...
    //
    private final IXyDataSeries<Double, Double> dataSeriesSensor1 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor2 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor3 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor4 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor5 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor6 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private ArrayList<IXyDataSeries<Double,Double>> dataSeriesList = new ArrayList<>(Arrays.asList(dataSeriesSensor1,dataSeriesSensor2,
            dataSeriesSensor3, dataSeriesSensor4, dataSeriesSensor5, dataSeriesSensor6));
    private ArrayList<Double> xCounters = new ArrayList<>(Arrays.asList(0.0,0.0,0.0,0.0,0.0,0.0));

    private int refreshCount = 0;


    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);

        //Code until the end of this method is a place holder
        plotSurface = (SciChartSurface) frag.findViewById(R.id.dynamic_plot);

        try{
            plotSurface.setRuntimeLicenseKey(
                    "<LicenseContract>\n" +
                            "<Customer>Trial Ext</Customer>\n" +
                            "<OrderId />\n <LicenseCount>1</LicenseCount>\n" +
                            " <IsTrialLicense>true</IsTrialLicense>\n" +
                            "<SupportExpires>11/27/2016 00:00:00</SupportExpires>\n\n " +
                            " <ProductCode>SC-ANDROID-2D-PRO</ProductCode>\n\n" +
                            " <KeyCode>d7cf477553b6d058bd23bffb5226013a8a817eec968c665224c8d33d9dc4db50425689a229503215526c8500fe32bbc9fbc449b732e61f361d477b10d7c967f5da5c1a7e60e9843d38640764eb33d0a534580705f0427f2fa111f391cd6b6e5d8652d68144f956b8115ba61cba0b18f599a1758bbfacf810b909cb899eac7b71926a09238b765c86846ddacd23001441083221</KeyCode>\n" +
                            "</LicenseContract>" );
        }catch (Exception e){
            e.printStackTrace();
        }

        dataSource = new GraphDataSource(); //Run the data handling on a separate thread
        dataSource.start();

        UpdateSuspender.using(plotSurface, new Runnable() {
            @Override
            public void run() {
                final NumericAxis xAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0,xBound).build();

                final NumericAxis yAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0, yBound).build();

                //These are wrappers for the series we added the data to...It contains the formatting
                //TODO: Try changing these to FastLineRenderableSeries
//                final IRenderableSeries rs1 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor1).withStrokeStyle(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).build(); //Light Blue Color
//                final IRenderableSeries rs2 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor2).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xA5, 0x00)).build(); //Light Pink Color
//                final IRenderableSeries rs3 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor3).withStrokeStyle(ColorUtil.argb(0xFF, 0xE1, 0x32, 0x19)).build(); //Orange Red Color
//                final IRenderableSeries rs4 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor4).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0xFF)).build(); //White color
//                final IRenderableSeries rs5 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor5).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0x99)).build(); //Light Yellow color
//                final IRenderableSeries rs6 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor6).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0x99, 0x33)).build(); //Light Orange color


                final FastLineRenderableSeries rs1 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor1).withStrokeStyle(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).build(); //Light Blue Color
                final FastLineRenderableSeries rs2 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor2).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xA5, 0x00)).build(); //Light Pink Color
                final FastLineRenderableSeries rs3 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor3).withStrokeStyle(ColorUtil.argb(0xFF, 0xE1, 0x32, 0x19)).build(); //Orange Red Color
                final FastLineRenderableSeries rs4 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor4).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0xFF)).build(); //White color
                final FastLineRenderableSeries rs5 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor5).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0x99)).build(); //Light Yellow color
                final FastLineRenderableSeries rs6 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor6).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0x99, 0x33)).build(); //Light Orange color

                Collections.addAll(plotSurface.getXAxes(), xAxis);
                Collections.addAll(plotSurface.getYAxes(), yAxis);
                Collections.addAll(plotSurface.getRenderableSeries(), rs1, rs2, rs3, rs4, rs5, rs6);
            }
        });


        //TODO Change to using FastLineRenderableSeries
        //How to make this change????
        //Look at the performance demo

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
                        aResponse = aResponse.replaceAll("\\s", "");
                        final String[] dataSplit = aResponse.split(","); //split the data at the commas

                        if(refreshCount == 1) { //Only update the graph every 3rd data packet
                            UpdateSuspender.using(plotSurface, new Runnable() {    //This updater graphs the values
                                @Override
                                public void run() {
                                    spliceDataAndAddData(dataSplit); //Want this to include basically only appending
                                    //TODO: Test this....
                                }
                            });
                            refreshCount = 0;
                        }else refreshCount++;

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
         * @param dataSplit String[] of the entire data
         * @return ArrayList of ArrayLists.. Inner arrayLists are the
         * values of the individual sensors
         */
        private void spliceDataAndAddData(String[] dataSplit){
            addToSensorSeries(dataSplit, 1); //TODO: Need to think of a better way to do this...
            addToSensorSeries(dataSplit, 2);
            addToSensorSeries(dataSplit, 3);
            addToSensorSeries(dataSplit, 4);
            addToSensorSeries(dataSplit, 5);
            addToSensorSeries(dataSplit, 6);
        }

        /**
         *
         * @param dataSplit data to split into individual sensor array
         *                  must contain only string representations of numbers
         * @param sensorSeriesNumber which sensors to collect the data points of
         * @return ArrayList<DataPoint> List of DataPoint values for an individual
         * sensor
         */
        private void addToSensorSeries(String[] dataSplit, int sensorSeriesNumber){
            sensorSeriesNumber -= 1;
            double xcounter = xCounters.get(sensorSeriesNumber);
            int i = sensorSeriesNumber;
            int dataSize = dataSplit.length - 1;
            String num = "";
            while(true){
                if(i < 6){ //This is the base case...add the first set of data
                    num = dataSplit[i];
                    try {
                        if(xcounter > xBound){
                            xcounter = 0;
                            dataSeriesList.get(sensorSeriesNumber).clear();
                        }
                        dataSeriesList.get(sensorSeriesNumber).append(xcounter, Double.parseDouble(num)); //appends every number...
                    }catch (Exception e){
                        //Corrupt data
                    }
                }else if((i) <= dataSize && i >= 6){ //Will start to get hit after the second time
                    num = dataSplit[i];
                    try {
                        if(xcounter > xBound){
                            xcounter = 0;
                            dataSeriesList.get(sensorSeriesNumber).clear();
                        }
                        dataSeriesList.get(sensorSeriesNumber).append(xcounter, Double.parseDouble(num));
                    }catch (Exception e){
                        //Corrupt data
                    }
                }else{
                    break;
                }
                xcounter++;
                i += 6;
            }
            xCounters.set(sensorSeriesNumber,xcounter);
        }


        //Want to be able to splice the data so it can be added in sets for the graphing
        //Essentially do what is done in addToSensorSeries but instead of adding the data to the IXySeries
        //Add it to a regular list so that we can add the ordered data to IXySeries in bulk
        private ArrayList<ArrayList<Double>> spliceDataWithXAndYSets(String[] dataSplit, int sensorSeriesNumber){
            ArrayList<Double> xVals = new ArrayList<>();
            ArrayList<Double> yVals = new ArrayList<>();
            ArrayList<ArrayList<Double>> dataPoints = new ArrayList<>();

            sensorSeriesNumber -= 1;
            double xcounter = xCounters.get(sensorSeriesNumber);
            int i = sensorSeriesNumber;
            int dataSize = dataSplit.length - 1;
            String num = "";
            while(true){
                if(i < 6){ //This is the base case...add the first set of data
                    num = dataSplit[i];
                    try {
                        if(xcounter > xBound){ //What should I do when this is the case?
                            xcounter = 0;
                        }
                        xVals.add(xcounter);
                        yVals.add(Double.parseDouble(num));
                    }catch (Exception e){
                        //Corrupt data
                    }
                }else if((i) <= dataSize && i >= 6){ //Will start to get hit after the second time
                    num = dataSplit[i];
                    try {
                        if(xcounter > xBound){ //TODO: need to figure out what to do here....
                            xcounter = 0;   //TODO: How to clear the bounds
                        }
                        xVals.add(xcounter);
                        yVals.add(Double.parseDouble(num));
                    }catch (Exception e){
                        //Corrupt data
                    }
                }else{
                    break;
                }
                xcounter++;
                i += 6;
            }
            xCounters.set(sensorSeriesNumber,xcounter);
            dataPoints.add(xVals);
            dataPoints.add(yVals);
            return dataPoints;
        }


        private void addDataToIXySeriesForUpdate(ArrayList<ArrayList<Double>> dataPoints, int sensorSeriesNumber){
            ArrayList<Double> xVals = dataPoints.get(0);
            ArrayList<Double> yVals = dataPoints.get(1);
            sensorSeriesNumber -= 1;
            dataSeriesList.get(sensorSeriesNumber).append(xVals,yVals); //This will cause an error
            //TODO: test the following function after testing the code with the
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
        for(int i = 0 ;  i <= 5; i++ ){
            xCounters.set(i, 0.0);
        }
        UpdateSuspender.using(plotSurface, new Runnable() {
            @Override
            public void run() {
                for(IXyDataSeries<Double, Double> dataSeriesSensor : dataSeriesList){
                    dataSeriesSensor.clear();
                }
            }
        });
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


    public void setApplyBeenPressed(boolean pressedOrNot){
        applyBeenPressed = pressedOrNot;
    }

}
