package com.mattmellor.smartshoegrapher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final UdpClient client = new UdpClient("18.111.41.17",2391,5007,45);
    private boolean changedConnectionStatus = false;
    private boolean listenerExists = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MATT!", "onCreateMethod");
    }

    public void onClickPingButton(View view){
        UdpClient.UdpServerAcknowledger udpPinger = client.new UdpServerAcknowledger();
        udpPinger.start();
        if(!changedConnectionStatus){
            TextView connection = (TextView) findViewById(R.id.connection_status);
            connection.setText(" Connected ");
            changedConnectionStatus = true;
        }
    }

    public void onClickStartStreaming(View view){
        if(!listenerExists) {
            listenerExists = true;
            client.setStreamData(true);
            UdpClient.UdpDataListener listener = client.new UdpDataListener();
            listener.start();
        }
    }

    public void onClickStopStreaming(View view){
        if(listenerExists){
            client.setStreamData(false); //Effectively kills the listener thread
            listenerExists = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
