package com.mattmellor.smartshoegrapher;

import android.app.Activity;

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Handler;

import Fragments.InputUserSettingsPopupFragment;
import Fragments.UdpSettingsFragment;
import UserDataDataBase.UDPDataBaseHelper;
import UserDataDataBase.UDPDatabaseContract;

/**
 * Created by Matthew on 10/20/2016.
 * This is a public class to allow for connecting to multiple different remote
 * Wifi -UDP Servers
 */

public class WirelessPairingActivity extends AppCompatActivity implements InputUserSettingsPopupFragment.OnDataPass{

    //Fields
    //DataBase Access
    //UDPDataBaseHelper mDbHelper = new UDPDataBaseHelper(getApplicationContext());
    //SQLiteDatabase db = mDbHelper.getWritableDatabase();
    private RecyclerView recycPairingList;
    private int numOfPairings;
    private ImageButton addSensor;
    private InputUserSettingsPopupFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.wireless_pairing_layout);
        numOfPairings = 0;

        //Recycler Pairing List (Scrollable List)
        //Used to dynamically add the pairedSensor views to Recycler List
        recycPairingList = (RecyclerView) findViewById(R.id.pairing_fragment_container);
        recycPairingList.setHasFixedSize(true);
        recycPairingList.setLayoutManager(new LinearLayoutManager(this));

        //Top Level Code to get a new Sensor from the user
        addSensor = (ImageButton) findViewById(R.id.add_sensor_pairing);
        addSensor.setOnClickListener(addSensorListener);

    }

    /**
     * Handler to receive messages from the UDPSettingsFragment Popup
     */
    private android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper()) {
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

    //Listener that brings up the UDPSettingsPopup for Sensor Adding
    private View.OnClickListener addSensorListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Bring up the UDPSettingsFragment
            FragmentManager fm = getSupportFragmentManager();
            settingsFragment = InputUserSettingsPopupFragment.newInstance();
            settingsFragment.setActivityHandler(mHandler);
            settingsFragment.show(fm, "MATT!");
        }
    };

    @Override //Passes Data from the UdpClient Fragment to main activity
    public void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        //TODO: Send the Data to the DataBase
        //TODO: Add the verifiedSensor to the list of Connected Sensors
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    //-------------Code for RecyclerView -----------------
    private class PairingHolder extends RecyclerView.ViewHolder {

        private TextView remotePort;
        private TextView localPort;
        private TextView remoteHost;
        private ImageButton removeButton;
        private Button pingButton;

        public PairingHolder (View itemView){  //This must be called at least once per item...
            super(itemView);
            remotePort = (TextView) itemView.findViewById(R.id.paired_sensor_remote_port_text_view);
            localPort = (TextView) itemView.findViewById(R.id.paired_sensor_local_port_text_view);
            remoteHost = (TextView) itemView.findViewById(R.id.remote_hostname_textview);
            removeButton = (ImageButton) itemView.findViewById(R.id.remove_pairing_image); //Press this button to remove the view
            pingButton = (Button) itemView.findViewById(R.id.ping_connected_pair);
            removeButton.setOnClickListener(pingButtonListener);
            pingButton.setOnClickListener(pingButtonListener);
        }

        public void bindPairingHolder(){
            //TODO: how to use the buttons?
        }

        private View.OnClickListener pingButtonListener = new View.OnClickListener(){

            public void onClick(View v){
                //TODO: Create the method to do something with this...
                Toast.makeText(getApplicationContext(), "Ping!", Toast.LENGTH_SHORT).show();
            }

        };

        private View.OnClickListener removeButtonListener = new View.OnClickListener(){

            public void onClick(View v){
                //TODO: Create the method to do something with this...
            }

        };

    }

    private class PairingListAdapter extends RecyclerView.Adapter<PairingHolder>{

        public PairingListAdapter(){
            //TODO:
        }

        @Override
        public WirelessPairingActivity.PairingHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View v = layoutInflater.inflate(R.layout.paired_sensor_fragment,parent,false);
            PairingHolder pairingholder = new PairingHolder(v);
            return pairingholder;
        }

        @Override
        public void onBindViewHolder(PairingHolder ph, int position){

        }

        @Override
        public int getItemCount(){
            int numberOfConnections = 2; //TODO: change this
            return numberOfConnections;
        }



    }


    //----------DataBase Manipulation Methods---------

//    public void addUDPSettingsToDataBase(String IPAddress, Integer localPort, Integer remotePort){
//        //Adding a row to the database
//        ContentValues row_value = new ContentValues();
//        row_value.put(UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_IP_HOST, IPAddress);
//        row_value.put(UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_LOCAL_PORT, localPort);
//        row_value.put(UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_REMOTE_PORT, remotePort);
//        long newRowId = db.insert(UDPDatabaseContract.UdpDataEntry.TABLE_NAME, null, row_value);
//    }
//
//    public ArrayList<ArrayList<String>> readUDPSettingsFromDataBase(){
//        //These are the columns we are
//        ArrayList<ArrayList<String>> data = new ArrayList<>();
//        String[] projection_Columns_sought = {
//                UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_IP_HOST,
//                UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_LOCAL_PORT,
//                UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_REMOTE_PORT
//        };
//        //Set the query cursor to get the whole table -> thus all of the nulls
//        Cursor cursor = db.query(UDPDatabaseContract.UdpDataEntry.TABLE_NAME, null, null, null, null, null, null);
//        cursor.moveToFirst(); //Moves the cursor to the first row
//        int numRows = cursor.getCount();
//        String remoteHostname;
//        String localPort;
//        String remotePort;
//        for(int rowNumber = 0; rowNumber < numRows; rowNumber++){ //Loop through each row and get the column values
//            remoteHostname = cursor.getString(0);
//            localPort = cursor.getString(1);
//            remotePort = cursor.getString(2);
//            ArrayList<String> sensorSettings = new ArrayList<String>(Arrays.asList(remoteHostname, localPort, remotePort));
//            data.add(sensorSettings);
//            cursor.moveToNext(); //Move to
//        }
//        cursor.close();
//        return data;
//    }
//
//    public void deleteSingleUDPDataSetting(String hostname){
//        String selection = UDPDatabaseContract.UdpDataEntry. COLUMN_NAME_IP_HOST + " LIKE ?";
//        String[] selectionArgs = {hostname}; //Matches the hostname string
//        //Deletes all rows with columns that have values that equal the variable hostname
//        db.delete(UDPDatabaseContract.UdpDataEntry.TABLE_NAME, selection, selectionArgs);
//    }
//
//    public void updateHostnameValue(String hostname){
//        //TODO: Search for hostname, delete old row, get new row
//    }



}
