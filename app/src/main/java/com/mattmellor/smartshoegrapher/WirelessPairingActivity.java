package com.mattmellor.smartshoegrapher;

import android.app.Activity;

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import UserDataDataBase.UDPDataBaseHelper;
import UserDataDataBase.UDPDatabaseContract;

/**
 * Created by Matthew on 10/20/2016.
 * This is a public class to allow for connecting to multiple different remote
 * Wifi -UDP Servers
 */

public class WirelessPairingActivity extends Activity {

    //Fields
    UDPDataBaseHelper mDbHelper = new UDPDataBaseHelper(getApplicationContext());
    SQLiteDatabase db = mDbHelper.getWritableDatabase();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.wireless_pairing_layout);
    }

    //TODO: Allow for dynamic creation of UDP Connection Fragments




    public void addUDPSettingsToDataBase(String IPAddress, Integer localPort, Integer remotePort){
        //Adding a row to the database
        ContentValues row_value = new ContentValues();
        row_value.put(UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_IP_HOST, IPAddress);
        row_value.put(UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_LOCAL_PORT, localPort);
        row_value.put(UDPDatabaseContract.UdpDataEntry.COLUMN_NAME_REMOTE_PORT, remotePort);
        long newRowId = db.insert(UDPDatabaseContract.UdpDataEntry.TABLE_NAME, null, row_value);
    }

    public void readUDPSettingsFromDataBase(){
        throw new RuntimeException("Unimplemented");
    }

}
