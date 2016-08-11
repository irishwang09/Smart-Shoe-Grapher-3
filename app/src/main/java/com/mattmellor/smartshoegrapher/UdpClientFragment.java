package com.mattmellor.smartshoegrapher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Matthew on 8/11/2016.
 * Holds the data about the UDP Connection and contains the wiring
 *
 */

public class UdpClientFragment extends Fragment {

    private Button ping;
    private Button start;
    private Button stop;

    private UdpClient client = new UdpClient("18.111.41.17",2391,5007,45);
    private boolean changedConnectionStatus = false;
    private boolean listenerExists = false;

    //Is it bad that the wiring is contained here????
    //What if this fragment isn't part of the view
    //^then there will be no data...
    //That is why start and stop shouldn't be here
    //TODO: Initialize the UDP client outside of this thread


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View frag = inflater.inflate(R.layout.udp_client_fragment_layout, container, false);

        //Ping Button Listener
        ping = (Button) frag.findViewById(R.id.ping);
        ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPing(frag);
            }
        });

        //Start Button Listener
        start = (Button) frag.findViewById(R.id.start_streaming);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStart();
            }
        });

        //Stop Button Listener
        stop = (Button) frag.findViewById(R.id.stop_streaming);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStop();
            }
        });

        return frag;
    }

    /**
     * Starts a thread to ping the server
     */
    public void onClickPing(View frag){
        UdpClient.UdpServerAcknowledger udpPinger = client.new UdpServerAcknowledger();
        udpPinger.start();
        if(!changedConnectionStatus){
            TextView connection = (TextView) frag.findViewById(R.id.connection_status);//What to do here?
            connection.setText(" Connected ");
            changedConnectionStatus = true;
        }
    }

    /**
     * Starts a thread to ping and then receive data from
     * the server
     */
    public void onClickStart(){
        if(!listenerExists) {
            listenerExists = true;
            client.setStreamData(true);
            UdpClient.UdpDataListener listener = client.new UdpDataListener();
            listener.start();
        }
    }

    /**
     * Stops the thread the receives data from the server
     */
    public void onClickStop(){
        if(listenerExists){
            client.setStreamData(false); //Effectively kills the listener thread
            listenerExists = false;
        }
    }


}
