package Fragments;

import android.content.Context;

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

import com.mattmellor.smartshoegrapher.R;
import com.mattmellor.smartshoegrapher.UdpClient;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.NumericAxis;
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

    //Communication With Other Threads Outside GraphFragment class
    private Handler handler;

    private boolean listenerExists = false;
    private int xBound = 10000;
    private int yBound = 5000;
    private boolean applyBeenPressed = false;

    private SciChartSurface plotSurface;
    private GraphDataSource dataSource;
    protected final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

    private final IXyDataSeries<Double, Double> dataSeriesSensor1 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor2 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor3 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor4 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor5 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();
    private final IXyDataSeries<Double, Double> dataSeriesSensor6 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).build();


    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);

        //Code until the end of this method is a place holder
        plotSurface = (SciChartSurface) frag.findViewById(R.id.dynamic_plot);

        dataSource = new GraphDataSource();
        dataSource.start();

        UpdateSuspender.using(plotSurface, new Runnable() {
            @Override
            public void run() {
                final NumericAxis xAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0,xBound).build();

                final NumericAxis yAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0, yBound).build();

                final IRenderableSeries rs1 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor1).withStrokeStyle(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).build(); //Light Blue Color
                final IRenderableSeries rs2 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor2).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xA5, 0x00)).build(); //Light Pink Color
                final IRenderableSeries rs3 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor3).withStrokeStyle(ColorUtil.argb(0xFF, 0xE1, 0x32, 0x19)).build(); //Orange Red Color
                final IRenderableSeries rs4 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor4).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0xFF)).build(); //White color
                final IRenderableSeries rs5 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor5).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0x99)).build(); //Light Yellow color
                final IRenderableSeries rs6 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor6).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0x99, 0x33)).build(); //Light Orange color

                Collections.addAll(plotSurface.getXAxes(), xAxis);
                Collections.addAll(plotSurface.getYAxes(), yAxis);
                Collections.addAll(plotSurface.getRenderableSeries(), rs1, rs2, rs3, rs4, rs5, rs6);
            }
        });


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
//                            plotSurface.redraw();
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
//            sensorNumber--;
//            int dataSize = seriesList.get(sensorNumber).data.size();
//            if(dataSize + sensor.size() > xBound){
//                seriesList.get(sensorNumber).resetData();
//            }
//            seriesList.get(sensorNumber).data.addAll(sensor);
        }

        public Number getX(int series, int index){
            return index;
        }

        public Number getY(int series, int index){
            //return seriesList.get(series).data.get(index); //TODO: Double check this so no null pointer ref
            throw new RuntimeException("Unimplemented");
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
