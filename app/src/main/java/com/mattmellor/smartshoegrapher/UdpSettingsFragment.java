package com.mattmellor.smartshoegrapher;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Matthew on 8/11/2016.
 * Holds the data about the UDP Connection and contains the wiring
 *
 */

public class UdpSettingsFragment extends Fragment {

    private Button ping;
    private Button apply;
    private Button reset;
    private EditText hostnameEditText;
    private EditText localPortEditText;
    private EditText remotePortEditText;

    private String unverifiedHostname;
    private String unverifiedRemotePort;
    private String unverifiedLocalPort;
    private final String defaultHostname = "footsensor1.dynamic-dns.net";
    private final String defaultRemotePort = "2391";
    private final String defaultLocalPort = "5005";

    private UdpClient client = new UdpClient("18.111.41.17",2391,5007,45);
    private boolean changedConnectionStatus = false;

    OnDataPass dataPassHandle;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity a;
        if(context instanceof Activity){
            a = (Activity) context;
            dataPassHandle = (OnDataPass) a;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View frag = inflater.inflate(R.layout.udp_client_fragment_layout, container, false);

        ping = (Button) frag.findViewById(R.id.ping);
        apply = (Button) frag.findViewById(R.id.apply_button);
        reset = (Button) frag.findViewById(R.id.reset_button);
        hostnameEditText = (EditText) frag.findViewById(R.id.remote_hostname);
        localPortEditText = (EditText) frag.findViewById(R.id.local_port);
        remotePortEditText = (EditText) frag.findViewById(R.id.remote_port);

        //Don't need to communicate with the main thread/UI for this
        ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPing(frag);
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unverifiedHostname = hostnameEditText.getText().toString();
                unverifiedRemotePort = remotePortEditText.getText().toString();
                unverifiedLocalPort = localPortEditText.getText().toString();
                applyClickedPassData();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostnameEditText.setText(defaultHostname);
                localPortEditText.setText(defaultLocalPort);
                remotePortEditText.setText(defaultRemotePort);
                resetClickedPassDefaults();
            }
        });
        return frag;
    }


    //------------Helper functions for the onClick Listeners--------

    /**
     * Starts a thread to ping the server
     */
    public void onClickPing(View frag){
        UdpClient.UdpServerAcknowledger udpPinger = client.new UdpServerAcknowledger();
        udpPinger.start();
        if(!changedConnectionStatus){
            TextView connection = (TextView) frag.findViewById(R.id.connection_status);//What to do here?
            connection.setText("@string/connectedComplete");
            changedConnectionStatus = true;
        }
    }

    public interface OnDataPass{
         public void onDataPassUdpSettings(String unverifiedHostname,String unverifiedLocalPort, String unverifiedRemotePort);
         public void onDataPassUdpReset(String defaultHostname, String defaultLocalPort, String defaultRemotePort);
    }


    //TODO...should be careful about passing around bad hostnames
    public void applyClickedPassData(){  //Call this when apply has been clicked
        dataPassHandle.onDataPassUdpSettings(unverifiedHostname,unverifiedLocalPort,unverifiedRemotePort);
    }

    public void resetClickedPassDefaults(){
        dataPassHandle.onDataPassUdpReset(defaultHostname, defaultLocalPort, defaultRemotePort);
    }

}
