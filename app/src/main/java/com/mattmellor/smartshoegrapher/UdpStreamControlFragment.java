package com.mattmellor.smartshoegrapher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Matthew on 8/11/2016.
 */

public class UdpStreamControlFragment extends Fragment {


    //TODO: Clean up this class
    private UdpClient client = new UdpClient("18.111.41.17",2391,5007,45);
    private Button start;
    private Button stop;
    private boolean listenerExists = false;

    //House the start and stop buttons here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.udp_stream_control_fragment, container, false);

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
    public void onClickStop() {
        if (listenerExists) {
            client.setStreamData(false); //Effectively kills the listener thread
            listenerExists = false;
        }

    }
}
