package com.mattmellor.smartshoegrapher;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Matthew on 8/11/2016.
 * Fragment that sends start or stop button presses to the main activity
 * and then eventually to graph fragment
 */

public class UdpStartStopFragment extends Fragment {

    private Button start;
    private Button stop;
    private PassStartStopData dataPassHandle;

    //House the start and stop buttons here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View frag = inflater.inflate(R.layout.udp_stream_control_fragment, container, false);

        //Start Button Listener
        start = (Button) frag.findViewById(R.id.start_streaming);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPassHandle.startGraphing(); //send the start stop signal to main activity
            }
        });

        //Stop Button Listener
        stop = (Button) frag.findViewById(R.id.stop_streaming);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPassHandle.stopGraphing(); //Send the start/stop signal to main activity
            }
        });

        return frag;
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity a;
        if(context instanceof Activity){
            a = (Activity) context;
            dataPassHandle = (UdpStartStopFragment.PassStartStopData) a;
        }
    }


    public interface PassStartStopData{
        public void startGraphing();
        public void stopGraphing();
    }

}
