package com.mattmellor.smartshoegrapher;

import android.app.Activity;

import android.content.ContentValues;
import android.content.Context;
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
    private UDPDataBaseHelper mDbHelper;
    private SQLiteDatabase db;
    private RecyclerView recycPairingList;
    private PairingListAdapter mAdapter;
    private ImageButton addSensor;
    private InputUserSettingsPopupFragment settingsFragment;
    private ArrayList<String> connectedHostnames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.wireless_pairing_layout);
        connectedHostnames = new ArrayList<>();

//        mDbHelper = new UDPDataBaseHelper(getApplicationContext());
//        db = mDbHelper.getWritableDatabase();
        //Recycler Pairing List (Scrollable List)
        //Used to dynamically add the pairedSensor views to Recycler List
        //TODO: Make sure that this is correct
        recycPairingList = (RecyclerView) findViewById(R.id.pairing_fragment_container);
        recycPairingList.setHasFixedSize(true);
        recycPairingList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PairingListAdapter(new ArrayList<ArrayList<String>>());
        recycPairingList.setAdapter(mAdapter); //Adapter is what we use to manage add/remove views

        //TODO: Check if old sensors exist add them to an old connected sensors list

        //Top Level Code to get a new Sensor from the user
        //This is button wiring for add new Sensor
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
                Context context = getBaseContext();
                CharSequence text = "Server Active: Reply Received";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Log.d("MATT!", "Succesful Ping");
            }
            else {
                Context context = getBaseContext();
                CharSequence text = "No Reply";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Log.d("MATT!", "Unsucessful Ping");
            }
        }
    };

    //Listener that brings up the UDPSettingsPopup for Sensor Adding
    private View.OnClickListener addSensorListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Bring up the UDPSettingsFragment to allow user input of new
            //sensors
            FragmentManager fm = getSupportFragmentManager();
            settingsFragment = InputUserSettingsPopupFragment.newInstance();
            settingsFragment.setActivityHandler(mHandler);
            settingsFragment.show(fm, "MATT!");
        }
    };

    @Override //Passes Data from the UdpClient Fragment to main activity
    public void onDataPassUdpSettings(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort) {
        //Send the Data to the DataBase
        //Check if data is already in the list
        //TODO: Don't let local port be reused
        if(!connectedHostnames.contains(verifiedHostname)) {
            //addUDPSettingsToDataBase(verifiedHostname, verifiedLocalPort, verifiedRemotePort); //TODO: How can I tell if this is working?
            //Add the verifiedSensor to the list of Connected Sensors
            addUDPSensorToConnectedList(verifiedHostname, verifiedLocalPort, verifiedRemotePort);
            connectedHostnames.add(verifiedHostname);
            //TODO: Add past sensors to list of connectedSenors
            Log.d("MATT!", "Passed Data/Connected");
        }
        else{
            Context context = getBaseContext();
            CharSequence text = "Already Connected";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            Log.d("MATT!", "Already Connected");
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    //-------------RecyclerView Backend/List of Connected Sensors -----------------
    private void addUDPSensorToConnectedList(String verifiedHostname, int verifiedLocalPort, int verifiedRemotePort){
        String verifiedLocalPortString = "" + verifiedLocalPort;
        String verifiedRemotePortString = "" + verifiedRemotePort;
        ArrayList<String> dataToAdd = new ArrayList<>(Arrays.asList(verifiedHostname,verifiedLocalPortString, verifiedRemotePortString));
        mAdapter.addDataSet(dataToAdd);
    }

    private void removeUDPSensorFromConnectedList(String hostname){
        mAdapter.removeDataSet(hostname);
        int index = 0;
        for(String host: connectedHostnames){
            if(host.equals(hostname)) break;
            index++;
        }
        connectedHostnames.remove(index); //Remove the requested hostname from the connectedSensorList
    }

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
            removeButton.setOnClickListener(removeButtonListener);
            pingButton.setOnClickListener(pingButtonListener);
        }


        private View.OnClickListener pingButtonListener = new View.OnClickListener(){

            public void onClick(View v){
                int localPortVal = Integer.parseInt(localPort.getText().toString());
                int remotePortVal = Integer.parseInt(remotePort.getText().toString());
                String remoteHostVal = remoteHost.getText().toString();
                UdpClient client = new UdpClient(remoteHostVal,remotePortVal,localPortVal,45);
                UdpClient.UdpServerAcknowledge udpPinger = client.new UdpServerAcknowledge(mHandler);
                udpPinger.start(); //Runs on a seperate thread then closes when done.
            }

        };

        private View.OnClickListener removeButtonListener = new View.OnClickListener(){

            public void onClick(View v){
                removeUDPSensorFromConnectedList(remoteHost.getText().toString()); //Remove the sensor in list by IDing the hostname
            }

        };

    }

    private class PairingListAdapter extends RecyclerView.Adapter<PairingHolder>{

        //Each entry is an array of ['hostname', 'localport', 'remoteport']
        private ArrayList<ArrayList<String>> mdataSet;

        public PairingListAdapter(ArrayList<ArrayList<String>> dataSet){
            //Empty on purpose
            mdataSet = dataSet;
        }

        //Create new views
        @Override
        public WirelessPairingActivity.PairingHolder onCreateViewHolder(ViewGroup parent, int viewType){
            //This is called whenever a new instance of ViewHolder is created
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View v = layoutInflater.inflate(R.layout.paired_sensor_fragment,parent,false);
            PairingHolder pairingholder = new PairingHolder(v);
            return pairingholder;
        }

        // Replace the contents of a view (invoked by layout manager)
        @Override
        public void onBindViewHolder(PairingHolder ph, int position){
            //Called whenever the SO binds the view with the data...in otherwords the
            //data is shown in the UI
            ph.remotePort.setText(mdataSet.get(position).get(1));
            ph.localPort.setText(mdataSet.get(position).get(2));
            ph.remoteHost.setText(mdataSet.get(position).get(0));
        }

        @Override
        public int getItemCount(){
            return mdataSet.size();
        }

        public void addDataSet(ArrayList<String> dataToAdd){
            mdataSet.add(dataToAdd);
            notifyItemInserted(getItemCount()-1); //Tell layout manager we have an update
        }

        public void removeDataSet(String hostname){
            int position = 1000;
            int index = 0;
            for(ArrayList sensorData: mdataSet){
                if(sensorData.get(0).equals(hostname)) position = index;
                index++;
            }
            if(position != 1000) {
                mdataSet.remove(position);
                notifyItemRemoved(position); //Tell layout manager we have an update
            }
        }

    }


    //----------DataBase Manipulation Methods---------
//
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
