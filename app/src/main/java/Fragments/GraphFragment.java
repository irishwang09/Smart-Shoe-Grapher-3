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
    private int xBound = 1_000_000; //
    private int yBound = 5000;
    private boolean applyBeenPressed = false;

    private SciChartSurface plotSurface;
    private GraphDataSource dataSource;
    protected final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

    private final IXyDataSeries<Integer, Integer> dataSeriesSensor1 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor2 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor3 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor4 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor5 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();
    private final IXyDataSeries<Integer, Integer> dataSeriesSensor6 = sciChartBuilder.newXyDataSeries(Integer.class, Integer.class).build();

    //TODO: Test this...
    private ArrayList<IXyDataSeries<Integer,Integer>> dataSeriesList = new ArrayList<>(Arrays.asList(dataSeriesSensor1,dataSeriesSensor2,
            dataSeriesSensor3, dataSeriesSensor4, dataSeriesSensor5, dataSeriesSensor6));//, dataSeriesSensor7,dataSeriesSensor8,
           // dataSeriesSensor9, dataSeriesSensor10, dataSeriesSensor11, dataSeriesSensor12));

    private ArrayList<Integer> xCounters = new ArrayList<>(Arrays.asList(0,0,0,0,0,0));
    private int xCounter = 0;


    @Override //inflate the fragment view in the mainActivity view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.graph_fragment, container, false);

        plotSurface = (SciChartSurface) frag.findViewById(R.id.dynamic_plot);

        //Ensure that the license is added & SciChart Runs
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

        UpdateSuspender.using(plotSurface, new Runnable() {
            @Override
            public void run() {
                final NumericAxis xAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0,xBound).build();

                final NumericAxis yAxis = sciChartBuilder.newNumericAxis().withVisibleRange(0,yBound).build();

                //These are wrappers for the series we added the data to...It contains the formatting
                final FastLineRenderableSeries rs1 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor1).withStrokeStyle(ColorUtil.argb(0xFF, 0x40, 0x83, 0xB7)).build(); //Light Blue Color
                final FastLineRenderableSeries rs2 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor2).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xA5, 0x00)).build(); //Light Pink Color
                final FastLineRenderableSeries rs3 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor3).withStrokeStyle(ColorUtil.argb(0xFF, 0xE1, 0x32, 0x19)).build(); //Orange Red Color
                final FastLineRenderableSeries rs4 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor4).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0xFF)).build(); //White color
                final FastLineRenderableSeries rs5 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor5).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0xFF, 0x99)).build(); //Light Yellow color
                final FastLineRenderableSeries rs6 = sciChartBuilder.newLineSeries().withDataSeries(dataSeriesSensor6).withStrokeStyle(ColorUtil.argb(0xFF, 0xFF, 0x99, 0x33)).build(); //Light Orange color

                Collections.addAll(plotSurface.getXAxes(), xAxis);
                Collections.addAll(plotSurface.getYAxes(), yAxis);
                Collections.addAll(plotSurface.getRenderableSeries(), rs1, rs2, rs3, rs4, rs5, rs6);//,rs7, rs8, rs9, rs10, rs11, rs12);
            }
        });

        dataSource = new GraphDataSource(); //Run the data receiving & handling on a separate thread
        dataSource.start();

        return frag;
    }

    //-------------Get Data, Manipulate Data & Notify PlotUpdater-----------
    public class GraphDataSource extends Thread{

        public void run(){
            Looper.prepare();
            //Get the data from the UDP Data Class when its available
            handler = new Handler(){
                public void handleMessage(Message msg){
                    String sensorData = msg.getData().getString("data"); //Data received

                    if(dataValid(sensorData)){
                        sensorData = sensorData.replaceAll("\\s", "");
                        final String[] dataSplit = sensorData.split(","); //split the data at the commas
                        final ArrayList<ArrayList<Integer>> data = spliceDataIntoPointsSets(dataSplit);
//                        final ArrayList<ArrayList<Integer>> splicedIntoPoints1 = spliceDataWithXAndYSets(dataSplit, 1);
//                        final ArrayList<ArrayList<Integer>> splicedIntoPoints2 = spliceDataWithXAndYSets(dataSplit, 2);
//                        final ArrayList<ArrayList<Integer>> splicedIntoPoints3 = spliceDataWithXAndYSets(dataSplit, 3);
//                        final ArrayList<ArrayList<Integer>> splicedIntoPoints4 = spliceDataWithXAndYSets(dataSplit, 4);
//                        final ArrayList<ArrayList<Integer>> splicedIntoPoints5 = spliceDataWithXAndYSets(dataSplit, 5);
//                        final ArrayList<ArrayList<Integer>> splicedIntoPoints6 = spliceDataWithXAndYSets(dataSplit, 6);

                        UpdateSuspender.using(plotSurface, new Runnable() {    //This updater graphs the values
                                @Override
                                public void run() {
                                    addDataToSeries(data);
//                                    addDataToIXySeriesForUpdate(splicedIntoPoints1,1);
//                                    addDataToIXySeriesForUpdate(splicedIntoPoints2,2);
//                                    addDataToIXySeriesForUpdate(splicedIntoPoints3,3);
//                                    addDataToIXySeriesForUpdate(splicedIntoPoints4,4);
//                                    addDataToIXySeriesForUpdate(splicedIntoPoints5,5);
//                                    addDataToIXySeriesForUpdate(splicedIntoPoints6,6);
                                }
                            });
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
            return ((data.length() == 1350));
        }

        //Operational cost of this method should be constant...
        private ArrayList<ArrayList<Integer>> spliceDataWithXAndYSets(String[] dataSplit, int sensorSeriesNumber){
            ArrayList<Integer> xVals = new ArrayList<>();
            ArrayList<Integer> yVals = new ArrayList<>();
            ArrayList<ArrayList<Integer>> dataPoints = new ArrayList<>();

            sensorSeriesNumber -= 1;
            int xcounter = xCounters.get(sensorSeriesNumber);
            if(xcounter == 0){
                dataSeriesList.get(sensorSeriesNumber).clear();
            }
            int i = sensorSeriesNumber;
            int dataSize = dataSplit.length - 1;
            String num = "";
            while(true){
                if(i < 6){ //This is the base case...add the first set of data
                    num = dataSplit[i];

                    try {
                        if(xcounter > xBound){ //What should I do when this is the case?
                            xcounter = 0;
                            break; //Lose a little data/chop off the end
                        }
                        xVals.add(xcounter);
                        yVals.add(Integer.parseInt(num));
                    }catch (Exception e){
                        //Corrupt data
                    }

                }else if((i) <= dataSize && i >= 6){ //Will start to get hit after the second time
                    num = dataSplit[i];

                    try {
                        if(xcounter > xBound){
                            xcounter = 0;
                            break;
                        }
                        xVals.add(xcounter);
                        yVals.add(Integer.parseInt(num));
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



        private void addDataToIXySeriesForUpdate(ArrayList<ArrayList<Integer>> dataPoints, int sensorSeriesNumber){
            ArrayList<Integer> xVals = dataPoints.get(0);
            ArrayList<Integer> yVals = dataPoints.get(1);
            sensorSeriesNumber -= 1;
            dataSeriesList.get(sensorSeriesNumber).append(xVals,yVals);
        }

        private void addDataToSeries(ArrayList<ArrayList<Integer>> data){
            //Add x_1, y_1 set to IXySeries
            dataSeriesList.get(0).append(data.get(0), data.get(1));
            dataSeriesList.get(1).append(data.get(2), data.get(3));
            dataSeriesList.get(2).append(data.get(4), data.get(5));
            dataSeriesList.get(3).append(data.get(6), data.get(7));
            dataSeriesList.get(4).append(data.get(8), data.get(9));
            dataSeriesList.get(5).append(data.get(10), data.get(11));
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
//        for(int i = 0 ;  i <= 5; i++ ){
//            xCounters.set(i, 0);
//        }
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
