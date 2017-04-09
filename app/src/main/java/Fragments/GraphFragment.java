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
import com.scichart.charting.visuals.axes.NumericAxis;
import com.scichart.charting.visuals.annotations.TextAnnotation;
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.drawing.utility.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import SciChartUserClasses.SciChartBuilder;


/**
 * Created by Matthew on 8/15/2016.
 * Fragment to hold a single graph and its underlying UDP Data Collection
 * Real Time Graphing is implemented in this class
 * TODO: This class needs to be changed to handle 24 sensors instead of 6
 * Which will be quite difficult
 */

public class GraphFragment extends Fragment {

    //UDP Settings
    private UdpClient client;  //This is the client that gives up all the the data
    private String hostname;  //hostname specifying the
    private int remotePort;
    private int localPort;

    //Allows Communication With Other Threads Outside GraphFragment class
    private Handler handler;

    private boolean listenerExists = false;
    private boolean startAlreadyPressed = false;
    private int xBound = 100_000;
    private int yBound = 5000;
    private String title = "Sensor Values vs. Number of Samples";
    private String xaxistitle = "Number of Samples";
    private String yaxistitle = "Sensor Values";

    private SciChartSurface plotSurface;
    private GraphDataSource dataSource; // has a handler to receive data
    protected final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

   // private ArrayList<>

    private final IXyDataSeries<Integer, Integer> dataSeriesSensor1 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor2 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor3 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor4 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor5 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor6 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();

    private final IXyDataSeries<Integer, Integer> dataSeriesSensor7 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor8 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor9 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor10 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor11 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor12 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();

    private ArrayList<IXyDataSeries<Integer,Integer>> dataSeriesList = new ArrayList<>(Arrays.asList(dataSeriesSensor1,dataSeriesSensor2,
            dataSeriesSensor3, dataSeriesSensor4, dataSeriesSensor5, dataSeriesSensor6, dataSeriesSensor7, dataSeriesSensor8, dataSeriesSensor9, dataSeriesSensor10,
            dataSeriesSensor11, dataSeriesSensor12));

    private int xCounter = 0;
    private int xCounter2 = 0;
    private ArrayList<String[]> firstList = new ArrayList<>();
    private ArrayList<String[]> secondList = new ArrayList<>();

    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);

        plotSurface = (SciChartSurface) frag.findViewById(R.id.dynamic_plot);

        //Ensure that the license is added & SciChart Runs
        try{
            plotSurface.setRuntimeLicenseKey(
                    "<LicenseContract>\n" +
                            "<Customer>MIT</Customer>\n" +
                            "<OrderId>EDUCATIONAL-USE-0016</OrderId>\n" +
                            "<LicenseCount>5</LicenseCount>\n" +
                            " <IsTrialLicense>false</IsTrialLicense>\n" +
                            "<SupportExpires>05/31/2017 00:00:00</SupportExpires>\n\n " +
                            " <ProductCode>SC-ANDROID-2D-PRO</ProductCode>\n\n" +
                            " <KeyCode>57a1d37ef5811a3a3b905505a94bf08ba741a706b8768fdf434c05eb7eb2f5b58dc39039e24ff0c0e00b4385838e9ac44154fd7013b2836e7891a2281fe154a3b9915757a401e0978bc1624be61e2a53abc19a3af1f3fb11bdda0c794d1fa7bbad9acc094d884ed540cb3b841926710daa5ee7b433bb77b1d2fd317e8c499fd9db7e38973b4853351c22bc41c49cf4b5b5dc3b1c78d298313be1b071d649229f</KeyCode>\n" +
                            "</LicenseContract>" );
        }catch (Exception e){
            e.printStackTrace();
        }

        UpdateSuspender.using(plotSurface, new Runnable() {
            @Override
            public void run() {
                final NumericAxis xAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0,xBound).build();
                final NumericAxis yAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0,yBound).build();
                 /*String labelAnnotation = new TextAnnotation(); Still working on it*/
                //These are wrappers for the series we added the data to...It contains the formatting
                final FastLineRenderableSeries rs1 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor1).withStrokeStyle(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).build(); //Light Blue Color
                final FastLineRenderableSeries rs2 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor2).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xA5, 0x00)).build(); //Light Pink Color
                final FastLineRenderableSeries rs3 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor3).withStrokeStyle(ColorUtil.argb(0xFF, 0xE1, 0x32, 0x19)).build(); //Orange Red Color
                final FastLineRenderableSeries rs4 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor4).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0xFF)).build(); //White color
                final FastLineRenderableSeries rs5 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor5).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0x99)).build(); //Light Yellow color
                final FastLineRenderableSeries rs6 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor6).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0x99, 0x33)).build(); //Light Orange color
                final FastLineRenderableSeries rs7 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor7).withStrokeStyle(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).build(); //Light Blue Color
                final FastLineRenderableSeries rs8 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor8).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xA5, 0x00)).build(); //Light Pink Color
                final FastLineRenderableSeries rs9 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor9).withStrokeStyle(ColorUtil.argb(0xFF, 0xE1, 0x32, 0x19)).build(); //Orange Red Color
                final FastLineRenderableSeries rs10 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor10).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0xFF)).build(); //White color
                final FastLineRenderableSeries rs11 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor11).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0x99)).build(); //Light Yellow color
                final FastLineRenderableSeries rs12 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor12).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0x99, 0x33)).build(); //Light Orange color


                Collections.addAll(plotSurface.getXAxes(), xAxis);
                Collections.addAll(plotSurface.getYAxes(), yAxis);
                Collections.addAll(plotSurface.getRenderableSeries(), rs1, rs2, rs3, rs4, rs5, rs6,rs7, rs8, rs9, rs10, rs11, rs12);
            }
        });

        dataSource = new GraphDataSource(); //Run the data receiving & handling on a separate thread
        dataSource.start(); //Starts the thread running/open to receive data

        return frag; //have to return fragment at the end of onCreateView
    }

    //-------------Get Data, Manipulate Data & Notify PlotUpdater-----------
    public class GraphDataSource extends Thread{

        public void run(){
            Looper.prepare();
            //Get the data from the UDP Data Class when its available
            handler = new Handler(){
                public void handleMessage(Message msg){
                    String sensorData = msg.getData().getString("data"); //Data received
                    //msg.getData().
                    String sensorID = msg.getData().getString("clientID"); //TODO: Does this work?
                    if(dataValid(sensorData)){
                        sensorData = sensorData.replaceAll("\\s", "");
                        final String[] dataSplit = sensorData.split(","); //split the data at the commas
                        if(sensorID.equals("MATT")){
                            firstList.add(dataSplit);
                        }
                        if(sensorID.equals("ANDREW")){
                            secondList.add(dataSplit);
                        }
                        if(!firstList.isEmpty() && !secondList.isEmpty()){
                            final ArrayList<ArrayList<Integer>> data = spliceDataIntoPointsSets(firstList.remove(0));
                            final ArrayList<ArrayList<Integer>> data2 = spliceDataIntoPointsSet2(secondList.remove(0));
                            //TODO: this needs to be updated for the multiple sensors

                            UpdateSuspender.using(plotSurface, new Runnable() {    //This updater graphs the values
                                @Override
                                public void run() {
                                    addDataToSeries(data,data2); //Adding the data to the graph and drawing it
                                }
                                //TODO: Try adding UdpateSuspender somewhere else so that all 12 sensors are added at once
                                //TODO: ^continue above s.t. addDataToSeries(data) has data that is all of the data (12 series)
                            });
                        }
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
            return ((data.length() == 1350)); //TODO make more robust
        }

        private ArrayList<ArrayList<Integer>> spliceDataIntoPointsSets(String[] dataSplit){
            ArrayList<ArrayList<Integer>> orderedData = new ArrayList<>();
            ArrayList<Integer> x1 = new ArrayList<>();
            ArrayList<Integer> x2 = new ArrayList<>();
            ArrayList<Integer> x3 = new ArrayList<>();
            ArrayList<Integer> x4 = new ArrayList<>();
            ArrayList<Integer> x5 = new ArrayList<>();
            ArrayList<Integer> x6 = new ArrayList<>();
            ArrayList<Integer> y1 = new ArrayList<>();
            ArrayList<Integer> y2 = new ArrayList<>();
            ArrayList<Integer> y3 = new ArrayList<>();
            ArrayList<Integer> y4 = new ArrayList<>();
            ArrayList<Integer> y5 = new ArrayList<>();
            ArrayList<Integer> y6 = new ArrayList<>();

            int xval = xCounter;
            if(xCounter == 0){
                dataSeriesList.get(0).clear();
                dataSeriesList.get(1).clear();
                dataSeriesList.get(2).clear();
                dataSeriesList.get(3).clear();
                dataSeriesList.get(4).clear();
                dataSeriesList.get(5).clear();
                dataSeriesList.get(6).clear();
            }

            int dataLength = dataSplit.length;
            int i = 1;
            int num = 0;
            while (i < dataLength){
                if(i%6==0){
                    xval++;
                }
                if(xval == xBound){ //If we are at xBound... break out of adding data
                    xval = 0;
                    xCounter = 0;
                    break;
                }
                Log.d("MATT", dataSplit[i]); //TODO: Investigate this.. some kind of timing issue:
                num = Integer.parseInt(dataSplit[i]); //TODO: Investigate closing thread of clients
                switch(i){
                    case 1:
                        x1.add(xval);
                        y1.add(num);
                        break;
                    case 2:
                        x2.add(xval);
                        y2.add(num);
                        break;
                    case 3:
                        x3.add(xval);
                        y3.add(num);
                        break;
                    case 4:
                        x4.add(xval);
                        y4.add(num);
                        break;
                    case 5:
                        x5.add(xval);
                        y5.add(num);
                        break;
                    case 6:
                        x6.add(xval);
                        y6.add(num);
                        break;
                }
                i++;
            }
            xCounter = xval;
            orderedData.add(x1);
            orderedData.add(y1);
            orderedData.add(x2);
            orderedData.add(y2);
            orderedData.add(x3);
            orderedData.add(y3);
            orderedData.add(x4);
            orderedData.add(y4);
            orderedData.add(x5);
            orderedData.add(y5);
            orderedData.add(x6);
            orderedData.add(y6);
            return orderedData;
        }

        private ArrayList<ArrayList<Integer>> spliceDataIntoPointsSet2(String[] dataSplit){
            ArrayList<ArrayList<Integer>> orderedData = new ArrayList<>();
            ArrayList<Integer> x1 = new ArrayList<>();
            ArrayList<Integer> x2 = new ArrayList<>();
            ArrayList<Integer> x3 = new ArrayList<>();
            ArrayList<Integer> x4 = new ArrayList<>();
            ArrayList<Integer> x5 = new ArrayList<>();
            ArrayList<Integer> x6 = new ArrayList<>();
            ArrayList<Integer> y1 = new ArrayList<>();
            ArrayList<Integer> y2 = new ArrayList<>();
            ArrayList<Integer> y3 = new ArrayList<>();
            ArrayList<Integer> y4 = new ArrayList<>();
            ArrayList<Integer> y5 = new ArrayList<>();
            ArrayList<Integer> y6 = new ArrayList<>();

            int xval = xCounter2;
            if(xCounter2 == 0){
                dataSeriesList.get(6).clear();
                dataSeriesList.get(7).clear();
                dataSeriesList.get(8).clear();
                dataSeriesList.get(9).clear();
                dataSeriesList.get(10).clear();
                dataSeriesList.get(11).clear();
            }

            int dataLength = dataSplit.length;
            int i = 1;
            int num = 0;
            while (i < dataLength){
                if(i%6==0){
                    xval++;
                }
                if(xval == xBound){ //If we are at xBound... break out of adding data
                    xval = 0;
                    xCounter2 = 0;
                    break;
                }
                num = Integer.parseInt(dataSplit[i]);
                switch(i){
                    case 1:
                        x1.add(xval);
                        y1.add(num);
                        break;
                    case 2:
                        x2.add(xval);
                        y2.add(num);
                        break;
                    case 3:
                        x3.add(xval);
                        y3.add(num);
                        break;
                    case 4:
                        x4.add(xval);
                        y4.add(num);
                        break;
                    case 5:
                        x5.add(xval);
                        y5.add(num);
                        break;
                    case 6:
                        x6.add(xval);
                        y6.add(num);
                        break;
                }
                i++;
            }
            xCounter2 = xval;
            orderedData.add(x1);
            orderedData.add(y1);
            orderedData.add(x2);
            orderedData.add(y2);
            orderedData.add(x3);
            orderedData.add(y3);
            orderedData.add(x4);
            orderedData.add(y4);
            orderedData.add(x5);
            orderedData.add(y5);
            orderedData.add(x6);
            orderedData.add(y6);
            return orderedData;
        }

        private void addDataToSeries(ArrayList<ArrayList<Integer>> data, ArrayList<ArrayList<Integer>> data2){
            //Add x_1, y_1 set to IXySeries
            dataSeriesList.get(0).append(data.get(0), data.get(1));
            dataSeriesList.get(1).append(data.get(2), data.get(3));
            dataSeriesList.get(2).append(data.get(4), data.get(5));
            dataSeriesList.get(3).append(data.get(6), data.get(7));
            dataSeriesList.get(4).append(data.get(8), data.get(9));
            dataSeriesList.get(5).append(data.get(10), data.get(11));
            dataSeriesList.get(6).append(data2.get(0), data2.get(1));
            dataSeriesList.get(7).append(data2.get(2), data2.get(3));
            dataSeriesList.get(8).append(data2.get(4), data2.get(5));
            dataSeriesList.get(9).append(data2.get(6), data2.get(7));
            dataSeriesList.get(10).append(data2.get(8), data2.get(9));
            dataSeriesList.get(11).append(data2.get(10), data2.get(11));
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
          //if(!startAlreadyPressed) {
              if (!listenerExists) {
                  Log.d("MATT!", "Creating connection... pressed start graphing");
                  resetGraph();
                  listenerExists = true;
                  UdpClient client1 = new UdpClient("footsensor1.dynamic-dns.net", 2392,5006,45);
                  client1.setStreamData(true);
                  UdpClient.UdpDataListener listener1 = client1.new UdpDataListener(handler, "ANDREW");
                  listener1.start();
                  Log.d("MATT!", "Trying Client 2");
                  UdpClient client2 = new UdpClient("footsensor2.dynamic-dns.net", 2392, 5010, 45);
                  client2.setStreamData(true);
                  UdpClient.UdpDataListener listener2 = client2.new UdpDataListener(handler, "MATT");
                  listener2.start();

//                  client = new UdpClient(hostname, remotePort, localPort, 45); //Creates the client with the Updated hostname, remotePort, localPort
//                  client.setStreamData(true);
//                  UdpClient.UdpDataListener listener = client.new UdpDataListener(handler); //Handler has been waiting in the background for data(Since onCreateView)..It is the handler in GraphDataSource
//                  listener.start(); //TODO: Make sure to close listener thread
              }
              //startAlreadyPressed = true;
          //}
    }

    private void resetGraph(){
        xCounter = 0;
        UpdateSuspender.using(plotSurface, new Runnable() {
            @Override
            public void run() {
                for(IXyDataSeries<Integer, Integer> dataSeriesSensor : dataSeriesList){
                    dataSeriesSensor.clear();
                }
            }
        });
    }

    /**
     * Tell the data listener to stop listening to data
     */
    public void stopGraphing(){
        Log.d("MATT!", "Stop Graphing In Graph Fragment");
        if (listenerExists) {
            client.setStreamData(false);
            listenerExists = false;
        }
        //startAlreadyPressed = false;
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

    /* This is to update x-axis,y-axis and title*/
    public void updatexBound(int xBound) { this.xBound= xBound;}

    public void updateyBound(int yBound) { this.yBound= yBound;}

    public void updatetitle(String title) { this.title= title;}

    public void updatexaxistitle(String title) { this.title= title;}

    public void updateyaxistitle(String title) { this.title= title;}


}
