package com.mattmellor.smartshoegrapher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import Fragments.GraphFragment;
import Fragments.UdpSettingsFragment; //UdpSettings Fragment is depreciated TODO: Change to InputUserSettingsPopupFragment
import SciChartUserClasses.SciChartBuilder;


public class MainActivity extends AppCompatActivity implements UdpSettingsFragment.OnDataPass{

    private String hostname;
    private int remotePort;
    private int localPort;

    private GraphFragment graphFragment;
    private SettingsCardAdapter mAdapter;
    private boolean currentlyGraphing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SciChartBuilder.init(this); //This is important for GraphFragment to initialize it
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //set the layout of the activity
        graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.graph_fragment);

        //Create a Scrolling list for the start stop Sensor Pairing and Graph Settings buttons
        RecyclerView recyclerSettingsCardsList = (RecyclerView) findViewById(R.id.recycler_view_settings_cards_list);
        recyclerSettingsCardsList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerSettingsCardsList.setLayoutManager(layoutManager);
        ArrayList<String> settingCardTitles = new ArrayList<>(Arrays.asList("Start Stop", "Sensor Pairing", "Graph Settings"));
        mAdapter = new MainActivity.SettingsCardAdapter(settingCardTitles);
        recyclerSettingsCardsList.setAdapter(mAdapter); //Adapter is what we use to manage add/remove views
    }

    @Override //Passes Data from the UdpClient Fragment to main activity
    public void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        //TODO: This is old code that will have to be replaced.
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

    //This is used to create the underlying code for the list of buttons created in SettingsCardHolder
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

    //This
    private class SettingCardHolder extends RecyclerView.ViewHolder{

        private TextView cardTitle;
        private View item_view;

        private SettingCardHolder (View itemView){  //This must be called at least once per item...
            super(itemView);
            item_view = itemView;
            cardTitle = (TextView) itemView.findViewById(R.id.setting_card_title);
        }

        //OnClick button listener will bring up the Wireless Pairing Activity to get user UDP Setting Data
        //TODO: Need to get the UDP Sensor Data back to MainActivity
        //which could be done by
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

        //Button listener to get data from the user on the size of the graph that they want
        private View.OnClickListener graphSettingsButtonListener = new View.OnClickListener(){

            public void onClick(View v){
                //TODO: Implement this
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
                item_view.setOnClickListener(graphSettingsButtonListener);
            }
        }

    }


}
