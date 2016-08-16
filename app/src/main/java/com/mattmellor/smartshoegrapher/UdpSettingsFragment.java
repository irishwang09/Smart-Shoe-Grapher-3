package com.mattmellor.smartshoegrapher;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.mattmellor.smartshoegrapher.R.string.connectedComplete;

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

    private String hostname = "footsensor1.dynamic-dns.net";
    private int localPort = 5006;
    private int remotePort = 2391;
    private String unverifiedHostname;
    private String unverifiedRemotePort;
    private String unverifiedLocalPort;
    private final String defaultHostname = "footsensor1.dynamic-dns.net";
    private final int defaultRemotePort = 2391;
    private final int defaultLocalPort = 5006;
    private boolean applyPressed = false;

    private OnDataPass dataPassHandle;
    private Handler activityHandler;

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

        hostnameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyPressed = false;
            }
        });

        remotePortEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyPressed = false;
            }
        });

        localPortEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyPressed = false;
            }
        });

        //Don't need to communicate with the main thread/UI for this
        ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(applyPressed) {
                    onClickPing(frag);
                }
                else{
                    Context context = getActivity();
                    CharSequence text = "Apply Settings";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unverifiedHostname = hostnameEditText.getText().toString();
                unverifiedRemotePort = remotePortEditText.getText().toString();
                unverifiedLocalPort = localPortEditText.getText().toString();
                boolean validParameters = true;

                if(portValid(unverifiedLocalPort)){
                    localPort = convertStringToInt(unverifiedLocalPort);
                    Log.d("MATT!", "Local Port Valid");
                }
                else{
                    validParameters = false;
                    localPortEditText.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.vibrate));
                    Log.d("MATT!", "Local Port invalid");
                }

                if(portValid(unverifiedRemotePort)){
                    remotePort = convertStringToInt(unverifiedRemotePort);
                    Log.d("MATT!", "Remote Port Valid");
                }
                else{
                    Log.d("MATT!", "remotePort Invalid");
                    remotePortEditText.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.vibrate));
                    validParameters = false;
                }

                if(hostnameValid(unverifiedHostname)){
                    hostname = unverifiedHostname;
                }
                else{
                    hostnameEditText.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.vibrate));
                }

                if(validParameters) {
                    applyClickedPassData();
                    applyPressed = true;
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostnameEditText.setText(defaultHostname);
                localPortEditText.setText("" + defaultLocalPort);
                remotePortEditText.setText("" + defaultRemotePort);
                hostname = defaultHostname;
                localPort = defaultLocalPort;
                remotePort = defaultRemotePort;
                resetClickedPassDefaults();
                applyPressed = false;
            }
        });
        return frag;
    }


    /**
     * Starts a thread to ping the server
     */
    public void onClickPing(View frag){
        //set the activity handler
        UdpClient client = new UdpClient(hostname,remotePort,localPort,45); //Still want to pass this?
        UdpClient.UdpServerAcknowledger udpPinger = client.new UdpServerAcknowledger(this.activityHandler);
        udpPinger.start();
    }

    /**
     * Interface methods implemented by main activity to allow for communication between the activity
     * and the fragment
     * We only pass verified input
     */
    public interface OnDataPass{
         public void onDataPassUdpSettings(String hostname,int localPort, int remotePort);
         public void onDataPassUdpReset(String defaultHostname, int defaultLocalPort, int defaultRemotePort);
    }

    public void applyClickedPassData(){
        dataPassHandle.onDataPassUdpSettings(hostname,localPort,remotePort);
    }

    public void resetClickedPassDefaults(){
        dataPassHandle.onDataPassUdpReset(defaultHostname, defaultLocalPort, defaultRemotePort);
    }

    public void reportPingResult(boolean result){
        if(result){
            Context context = getActivity();
            CharSequence text = "Server Active: Reply Received";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else{
            Context context = getActivity();
            CharSequence text = "No Reply: Verify Settings";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void setActivityHandler(Handler handler){
        this.activityHandler = handler;
    }


    //-------------Helper Functions-------------
    /**
     * @param value string
     * @return true if string is a valid remote port or local port number
     */
    public int convertStringToInt(String value) {
        return Integer.parseInt(value);
    }

    /**
     *
     * @param port
     *          representing the local or remote port
     * @return boolean
     *         true if a valid port number 0 - 65535
     *         false if not a valid port number
     */
    public boolean portValid(String port){
        return port.matches("^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$");
    }

    /**
     *
     * @param hostname
     * @return true if hostname is a valid hostname or IP Address according to RFC 1123
     */
    public boolean hostnameValid(String hostname){
        String validIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        String validHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
        return (hostname.matches(validIpAddressRegex) || hostname.matches(validHostnameRegex));
    }

}
