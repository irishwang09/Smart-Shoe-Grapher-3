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


    private UdpClient client = new UdpClient("18.111.41.17",2391,5007,45);
    //This needs to be passed in ^
    private boolean changedConnectionStatus = false;

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

        //Don't need to communicate with the main thread/UI for this
        ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPing(frag);
            }
        });

        return frag;
    }

    //TODO add logic to connect the new buttons Reset and Apply


    //------------Helper functions for the onClick Listeners--------

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


}
