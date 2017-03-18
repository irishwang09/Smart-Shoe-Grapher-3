package com.mattmellor.smartshoegrapher;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

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
import SciChartUserClasses.SciChartBuilder;
import UserDataDataBase.UDPDataBaseHelper;
import UserDataDataBase.UDPDatabaseContract;


public class MainActivity extends AppCompatActivity {

    private String hostname;
    private int remotePort;
    private int localPort;

    private GraphFragment graphFragment;
    private SettingsCardAdapter mAdapter;
    private boolean currentlyGraphing = false;
    private SQLiteDatabase db;
    private ArrayList<String> hostnames;
    private ArrayList<Integer> localPorts;
    private ArrayList<Integer> remotePorts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SciChartBuilder.init(this); //This is important for GraphFragment to initialize it
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //set the layout of the activity
        graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.graph_fragment);
        hostnames = new ArrayList<>();
        localPorts = new ArrayList<>();
        remotePorts = new ArrayList<>();

        //Read from the UserUDPSettings database if it exists
        UDPDataBaseHelper mDbHelper = new UDPDataBaseHelper(getApplicationContext());
        //Does the following need to be changed? We probably don't want to create a database if there isn't
        //TODO:Test
        db = mDbHelper.getWritableDatabase(); //Creates a new database if one doesn't exist
        ArrayList<ArrayList<String>> pastSensors = readUDPSettingsFromDataBase();
        //If there are sensors already in the database...
        if(pastSensors != null){
            Log.d("MATT!", "Reading old sensors in onCreate of MainActivity");
            for(ArrayList<String> sensorData: pastSensors){
                String verifiedHostname = sensorData.get(0);
                String verfLocalPort = sensorData.get(1);
                int verifiedLocalPort = Integer.parseInt(verfLocalPort);
                int verifiedRemotePort = Integer.parseInt(sensorData.get(2));
                hostnames.add(verifiedHostname);
                localPorts.add(verifiedLocalPort);
                remotePorts.add(verifiedRemotePort);
            }
            onDataPassUdpSettings(hostnames.get(0), localPorts.get(0), remotePorts.get(0));
        }

        //Create a Scrolling list for the start stop Sensor Pairing and Graph Settings buttons
        RecyclerView recyclerSettingsCardsList = (RecyclerView) findViewById(R.id.recycler_view_settings_cards_list);
        recyclerSettingsCardsList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerSettingsCardsList.setLayoutManager(layoutManager);
        ArrayList<String> settingCardTitles = new ArrayList<>(Arrays.asList("Start Stop", "Sensor Pairing", "Graph Settings"));
        mAdapter = new MainActivity.SettingsCardAdapter(settingCardTitles);
        recyclerSettingsCardsList.setAdapter(mAdapter); //Adapter is what we use to manage add/remove views
    }

    //Passes Data from main activity to graphfragment
    private void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        this.hostname = verifiedHostname;
        this.localPort = verifiedLocalPort;
        this.remotePort = verifiedRemotePort;
        graphFragment.updateHostname(hostname);
        graphFragment.updateLocalPort(localPort);
        graphFragment.updateRemotePort(remotePort);
        Log.d("MATT!", "Passed to GF Verified Host: " + verifiedHostname);
        Log.d("MATT!", "Passed to GF Verified Local Port : " + verifiedLocalPort);
        Log.d("MATT!", "Passed to GF Verified Remote Port : " + verifiedRemotePort);
    }

    //This is attached to the start/stop button in the SettingsCardAdapter
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

    private class SettingCardHolder extends RecyclerView.ViewHolder{

        private TextView cardTitle;
        private View item_view;

        private SettingCardHolder (View itemView){  //This must be called at least once per item...
            super(itemView);
            item_view = itemView;
            cardTitle = (TextView) itemView.findViewById(R.id.setting_card_title);
        }

        //OnClick button listener will bring up the Wireless Pairing Activity to get user UDP Setting Data
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

    private ArrayList<ArrayList<String>> readUDPSettingsFromDataBase(){
        //These are the columns we are
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        //Set the query cursor to get the whole table -> thus all of the nulls
        Cursor cursor = db.query(UDPDatabaseContract.UdpDataEntry.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst(); //Moves the cursor to the first row
        int numRows = cursor.getCount();
        String remoteHostname;
        String localPort;
        String remotePort;
        for(int rowNumber = 0; rowNumber < numRows; rowNumber++){ //Loop through each row and get the column values
            remoteHostname = cursor.getString(0);
            localPort = cursor.getString(1);
            remotePort = cursor.getString(2);
            ArrayList<String> sensorSettings = new ArrayList<>(Arrays.asList(remoteHostname, localPort, remotePort));
            data.add(sensorSettings);
            cursor.moveToNext(); //Move to
        }
        cursor.close();
        return data;
    }

}
