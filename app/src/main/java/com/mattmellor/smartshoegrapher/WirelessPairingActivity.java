package com.mattmellor.smartshoegrapher;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import UserDataDataBase.UDPDataBaseHelper;

/**
 * Created by Matthew on 10/20/2016.
 * This is a public class to allow for connecting to multiple different remote
 * Wifi -UDP Servers
 */

public class WirelessPairingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.wireless_pairing_layout);
    }

    //TODO: Allow for dynamic creation of UDP Connection Fragments

    //Open or Create the UDPSensor SQL database
    UDPDataBaseHelper mDbHelper = new UDPDataBaseHelper(getApplicationContext());
    SQLiteDatabase db = mDbHelper.getWritableDatabase();


}
