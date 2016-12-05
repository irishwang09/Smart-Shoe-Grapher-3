package com.mattmellor.smartshoegrapher;

import android.app.Activity;

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import UserDataDataBase.UDPDataBaseHelper;
import UserDataDataBase.UDPDatabaseContract;

/**
 * Created by Matthew on 10/20/2016.
 * This is a public class to allow for connecting to multiple different remote
 * Wifi -UDP Servers
 */

public class WirelessPairingActivity extends AppCompatActivity {

    //Fields
    //DataBase Access
    //UDPDataBaseHelper mDbHelper = new UDPDataBaseHelper(getApplicationContext());
    //SQLiteDatabase db = mDbHelper.getWritableDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.wireless_pairing_layout);

        //Get saved instances from rotating the screen

//        if(findViewById(R.id.pairing_fragment_container) != null) {
//
//            if(savedInstanceState != null){
//                return;
//                //If we are being restored from a previous state, then we don't need to do anything
//                // and should return or otherwise we will end up with overlapping fragments
//            }
//
//        }


        //TODO: Add recyclerView to the layout


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    //TODO Communicate with the fragment allows for adding more pairings
    //This communication will allow for communication from the




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
