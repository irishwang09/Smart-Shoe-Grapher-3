package com.mattmellor.smartshoegrapher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MATT!", "onCreateMethod");
    }

    public void onClickPingButton(View view){
        new Thread(new Runnable() {
            public void run(){
                String mess = "Android connection";
                DatagramSocket socket = null;
                try{
                    socket = new DatagramSocket(5006);
                    InetAddress serverAdd = InetAddress.getByName("18.111.41.17");
                    DatagramPacket packet;
                    packet = new DatagramPacket(mess.getBytes(), mess.length(), serverAdd, 2391);
                    socket.send(packet);
                    Log.d("MATT!", "end of packet sending");
                    //try to receive data
                    byte[] buf2 = new byte[1352];
                    int i = 0;
                    DatagramPacket receivedPacket;
                    Thread.sleep(1000);//Sleep for 1 second
                    while(i<=30){
                        receivedPacket = new DatagramPacket(buf2, buf2.length);
                        socket.receive(receivedPacket);
                        String received = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                        String[] val = received.substring(0,received.length() -2).split(",");
                        ArrayList<String> valsAsArrayList = new ArrayList<String>(Arrays.asList(val));
                        Log.d("MATT!", "" + valsAsArrayList.size());
                        i++;
                    }

                }catch (SocketException e){
                    //e.printStackTrace();
                    Log.e("MATT!", "socket exception");
                }catch(UnknownHostException e){
                    //e.printStackTrace();
                    Log.e("MATT!", "unknown host exception");
                }catch(IOException e){
                    //e.printStackTrace();
                    Log.e("MATT!", "IOException");
                }catch(Exception e){
                    Log.e("MATT!", "General exception");
                    e.printStackTrace();
                }finally {
                    if(socket != null){
                        socket.close();
                        Log.d("MATT!", "Made it to finally");
                    }
                }
            }
        }).start();


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
