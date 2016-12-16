package com.mattmellor.smartshoegrapher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import Fragments.GraphFragment;
import Fragments.UdpSettingsFragment;
import SciChartUserClasses.SciChartBuilder;


public class MainActivity extends AppCompatActivity implements UdpSettingsFragment.OnDataPass{

    private String hostname;
    private int remotePort;
    private int localPort;

    private UdpSettingsFragment settingsFragment;
    private GraphFragment graphFragment;
    private SettingsCardAdapter mAdapter;
    private boolean currentlyGraphing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SciChartBuilder.init(this); //This is important for GraphFragment...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.graph_fragment);

        //Create a Scrolling list
        RecyclerView recyclerSettingsCardsList = (RecyclerView) findViewById(R.id.recycler_view_settings_cards_list);
        recyclerSettingsCardsList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerSettingsCardsList.setLayoutManager(layoutManager);
        ArrayList<String> settingCardTitles = new ArrayList<>(Arrays.asList("Start Stop", "Sensor Pairing", "Graph Settings"));
        mAdapter = new MainActivity.SettingsCardAdapter(settingCardTitles);
        recyclerSettingsCardsList.setAdapter(mAdapter); //Adapter is what we use to manage add/remove views
    }


    /**
     * Handler to receive messages from different threads
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //Gets the task from the incoming Message object
            String aResponse = msg.getData().getString("message");
            if (aResponse.equals("success")) {
                settingsFragment.reportPingResult(true);
                Log.d("MATT!", "Succesful Ping");
            }
            else {
                settingsFragment.reportPingResult(false);
                Log.d("MATT!", "Unsucessful Ping");
            }
        }
    };


    @Override //Passes Data from the UdpClient Fragment to main activity
    public void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        this.hostname = verifiedHostname;
        this.localPort = verifiedLocalPort;
        this.remotePort = verifiedRemotePort;
        graphFragment.updateHostname(hostname);
        graphFragment.updateLocalPort(localPort);
        graphFragment.updateRemotePort(remotePort);
        Log.d("MATT!", "Verified Host: " + verifiedHostname);
        Log.d("MATT!", "Verified Local Port : " + verifiedLocalPort);
        Log.d("MATT!", "Verified Remote Port : " + verifiedRemotePort);
    }

    @Override
    public void onDataPassUdpReset(String defaultHostname, int defaultLocalPort, int defaultRemotePort) {
        this.hostname = defaultHostname;
        this.localPort = defaultLocalPort;
        this.remotePort = defaultRemotePort;
        graphFragment.updateHostname(hostname);
        graphFragment.updateLocalPort(localPort);
        graphFragment.updateRemotePort(remotePort);
        Log.d("MATT!", "default hostname: " + defaultHostname);
        Log.d("MATT!", "default remotePort: " + defaultRemotePort);
        Log.d("MATT!", "default localPort: " + defaultLocalPort);
    }

    @Override
    public void applyBeenPressed() {
        graphFragment.setApplyBeenPressed(true);
    }

    @Override
    public void updatesBeingMadeStopGraphing() {
        graphFragment.setApplyBeenPressed(false);
        graphFragment.stopGraphing();
    }

    private void startGraphing() {
        graphFragment.startGraphing();
    }

    private void stopGraphing() {
        graphFragment.stopGraphing();
    }

    //-------------Code for RecyclerView-----------

    private class SettingsCardAdapter extends RecyclerView.Adapter<SettingCardHolder>{
        //dataSet will just contain 3 entries: Start/Stop, Sensor Pairing, Graph Settings
        private ArrayList<String> mdataSet;

        private SettingsCardAdapter(ArrayList<String> dataSet){
            mdataSet = dataSet;
        }

        //Create new views
        @Override
        public MainActivity.SettingCardHolder onCreateViewHolder(ViewGroup parent, int viewType){
            //This is called whenever a new instance of ViewHolder is created
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View v = layoutInflater.inflate(R.layout.setting_card_view,parent,false);
            return new MainActivity.SettingCardHolder(v);
        }

        // Replace the contents of a view (invoked by layout manager)
        @Override
        public void onBindViewHolder(MainActivity.SettingCardHolder ph, int position){
            //Called whenever the SO binds the view with the data...in otherwords the
            //data is shown in the UI
            String title = mdataSet.get(position);
            ph.cardTitle.setText(mdataSet.get(position));
            ph.setOnClickListenerHolder(title);
        }

        @Override
        public int getItemCount(){
            return mdataSet.size();
        }
    }


    private class SettingCardHolder extends RecyclerView.ViewHolder{

        private TextView cardTitle;
        private View item_view;

        private SettingCardHolder (View itemView){  //This must be called at least once per item...
            super(itemView);
            item_view = itemView;
            cardTitle = (TextView) itemView.findViewById(R.id.setting_card_title);
        }

        private View.OnClickListener startSensorPairingListener = new View.OnClickListener(){

            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), WirelessPairingActivity.class);
                startActivity(intent);
            }
        };

        private View.OnClickListener startStopButtonListener = new View.OnClickListener(){

            public void onClick(View v){
                if(currentlyGraphing){
                    stopGraphing();  //TODO: This needs to be improved
                    currentlyGraphing = false;
                }
                else{
                    startGraphing();
                    currentlyGraphing = true;
                }
            }
        };

        private void setOnClickListenerHolder(String cardTitle){
            if(cardTitle.equals("Sensor Pairing")){
                item_view.setOnClickListener(startSensorPairingListener);
            }
            else if(cardTitle.equals("Start Stop")){
                item_view.setOnClickListener(startStopButtonListener);
            }
            else{  //Equals the button for starting the Graph Settings Button
                //TODO: Set the onClick Listener here
            }
        }

    }


}
