package com.mattmellor.smartshoegrapher;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.scichart.core.framework.UpdateSuspender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static com.mattmellor.smartshoegrapher.MainActivity.mode;

/**
 * Created by Matt Mellor on 8/5/2016.
 * Class that represents a udp wifi client
 * That will accept incoming datagrams(packets)
 * Outer class contains the information about the Udp connection
 *
 * Inner classes are responsible for
 *      1. Pinging the server
 *      2. Reading data from the server
 *
 * Reasoning for this formatting is to avoid having the UI Main thread
 * touch the UDP threads (Avoids errors)
 * Additionally the Android Won't Allow UDP Connection on the Main Thread
 *
 * Benefits of this approach
 *      1. Scalability
 *          Since each instance of UdpClient will have a separate instance of
 *          threads that ping the server and read the data, code should be scalable to
 *          allow for multiple udp connections at once.
 */

public class UdpClient  {

    private DatagramSocket receiveSocket;
    private String serverAddress; //Ip address/hostname
    private int remoteServerPort;
    private int localPort;
    private int bufferLength = 82;
    private int dataSetsPerPacket; // statically determined on esp side
    public DatagramPacket rcvdPacket;
    private boolean streamData = true;

    /**
     * @param ipAddress : String representing the ip Address/hostname of the remote server
     * @param remoteServerPort : int representing the value of the remote port of the server
     * @param localPort: int representing the value of the local port of the server
     * @param dataSetsPerPacket: int representing the number of data sets per packet
     */
    public UdpClient(String ipAddress, int remoteServerPort, int localPort, int dataSetsPerPacket){
        this.remoteServerPort = remoteServerPort;
        this.serverAddress = ipAddress;
        this.dataSetsPerPacket = dataSetsPerPacket;
        this.localPort = localPort;
    }

    /**
     * Inner class for sending a message to the remote server
     * This is a separate class because all networking actions can't occur
     * on the main UI thread
     */
    public class UdpServerAcknowledge extends Thread{

        private Handler handler;

        public UdpServerAcknowledge(Handler handler){
            this.handler = handler;
        }

        /**
         * value to be called by the thread
         */
        public void run(){
            acknowledgeServer();
        }

        private void acknowledgeServer() {
            DatagramSocket pingSocket = null;
            DatagramSocket rplySocket = null;
            String mess = "Ping";
            InetAddress address;
            DatagramPacket packet;
            DatagramPacket rPacket;
            byte[] buf = new byte[8];
            boolean fail = false;
            int port = 5013;
            try {
                pingSocket = new DatagramSocket(port);
                pingSocket.setReuseAddress(true);
                address = InetAddress.getByName("smartshoegrapher.dynamic-dns.net");
                //address = InetAddress.getByName("18.111.32.1");
                packet = new DatagramPacket(mess.getBytes(), mess.length(), address, port);
                pingSocket.send(packet);
                Log.d("MATT!", "About to wait to receive packet");
                rPacket = new DatagramPacket(buf, buf.length);
                rplySocket = new DatagramSocket(5003);
                rplySocket.setSoTimeout(3000);
                rplySocket.receive(rPacket);
                //pingSocket.setSoTimeout(3000);
                //pingSocket.receive(rPacket);
                String received = new String(rPacket.getData(), 0, rPacket.getLength());
                if (received.length() > 0){
                    Log.d("MATT!", "Successful Response from server");
                    threadMsg("success");
                }
            }catch(SocketTimeoutException e){
                //Send a message to the fragment
                Log.d("MATT!", "TimeoutException in ping");
                fail = true;
            }catch (SocketException e){
                e.printStackTrace();
                Log.e("MATT!", "socket exception");
                fail = true;
            }catch(UnknownHostException e){
                e.printStackTrace();
                Log.e("MATT!", "unknown host exception in ping test");
                fail = true;
            }catch(IOException e) {
                e.printStackTrace();
                Log.e("MATT!", "IOException");
                fail = true;
            }catch(Exception e){
                Log.e("MATT!", "General exception");
                e.printStackTrace();
                fail = true;
            }finally {
                if(fail){
                    threadMsg("fail");
                }
                if(pingSocket != null){
                    pingSocket.close();
                }
                if (rplySocket != null){
                    rplySocket.close();
                }
            }
        }

        private void threadMsg(String msg) {
            if (!msg.equals(null) && !msg.equals("")) {
                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", msg);
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        }
    }


    /**
     * Class for listening to Udp remote server for a long time
     * as a separate thread
     */
    public class RplyListener extends Thread {
        DatagramSocket rplySocket = null;
        byte[] buffer = new byte[16];
        DatagramPacket rplyPacket = new DatagramPacket(buffer, buffer.length);

        public RplyListener()
        {
        }

        public void run()
        {
            while (true)
            {
                try {
                    rplySocket = new DatagramSocket(5004);
                } catch (SocketException e) {
                    Log.e("IRIS", "could not create new DatagramSocket");
                }
                try {
                    rplySocket.receive(rplyPacket);
                } catch (IOException e) {
                    Log.e("IRIS", "could not receive packet");
                }
                String rply = new String(buffer, 0, rplyPacket.getLength());
                Log.e("Recieved", rply);
                if (rply.length() > 0){
                    Log.d("MATT!", "Successful Response from server");
                    //threadMsg("success");
                }
            }
        }
    }
    public class UdpDataListener extends Thread {

        private Handler mhandler;
        private String clientID;

        public UdpDataListener(Handler handler, String clientID){
            this.mhandler = handler; //This will be used to pass data to the Graph Fragment
            this.clientID = clientID;
            //The handler specified here is the handler from the GraphDataSource inner class of GraphFragment
        }

        public void run(){

            if (mode)
            {
                DatagramSocket senderSocket = null;
                DatagramSocket listenerSocket = null;
                try {
                    String mess = "Android Data Receiver";
                    senderSocket = new DatagramSocket(localPort);
                    InetAddress address = InetAddress.getByName("smartshoegrapher.dynamic-dns.net");
                    DatagramPacket packet = new DatagramPacket(mess.getBytes(), mess.length(), address, localPort);
                    senderSocket.send(packet);
                    senderSocket.close();
                }
                catch (Exception e)
                {
                    Log.e("IRIS", "exception thrown");
                }
                while(true) {
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        listenerSocket = new DatagramSocket(5003);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    String recievedData;
                    try {
                        listenerSocket.receive(packet);
                    } catch (IOException e) {
                        Log.e("EXCEPTION THROWN", "could not receive packet");
                    }
                    recievedData = new String(buffer, 0, packet.getLength());
                    threadMsgLocal(recievedData);
                    listenerSocket.close();
                }

            }
            else while (true) localThread();
        }

        private void pingThenListenToServer(){
            //byte[] buf = new byte[1352]; //TODO calculate this number with a formula for changability
            byte[] buf = new byte[bufferLength];
            String received = "";
            InetAddress address;
            String mess = "Android Data Receiver";
            //DatagramPacket packet;
            try{
                //Ping the server to tell server IP Address of Android phone

                /*receiveSocket = new DatagramSocket(localPort);
                address = InetAddress.getByName(serverAddress);
                packet = new DatagramPacket(mess.getBytes(), mess.length(), address, remoteServerPort);
                receiveSocket.send(packet);*/

                while (streamData) {
                    /*rcvdPacket = new DatagramPacket(buf, buf.length);
                    receiveSocket.receive(rcvdPacket);
                    received = new String(rcvdPacket.getData(), 0, rcvdPacket.getLength());
                    //dataToSend = received.substring(0, received.length() - 2); //Get the data
                    Log.d("MATT", received);
                    if (!received.equals("IP port changed"))
                    {
                        threadMsg(received);
                    }
                     //TODO: change this back
                    //Log.d("MATT!", clientID);*/
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);;
                    DatagramSocket socket = null;
                    String recievedData;
                    try {
                        socket = new DatagramSocket(localPort);
                    } catch (SocketException e) {
                        //message.setText("EXCEPTION THROWN: could not create new DatagramSocket");
                        Log.e("EXCEPTION THROWN", "could not create new DatagramSocket");
                    }
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        Log.e("EXCEPTION THROWN", "could not receive packet");
                    }
                    recievedData = new String(buffer, 0, packet.getLength());
                    threadMsgLocal(recievedData);
                    socket.close();
                }
                receiveSocket.close();
/*            }catch (SocketException e){
                e.printStackTrace();
                Log.e("MATT!", "socket exception in listen");
            }catch(UnknownHostException e){
                e.printStackTrace();
                Log.e("MATT!", "unknown host exception in listen");
            }catch(IOException e){
                e.printStackTrace();
                Log.e("MATT!", "IOException");*/
            }catch(Exception e){
                Log.e("MATT!", "General exception");
                e.printStackTrace();
            }finally {
                if(receiveSocket != null){
                    receiveSocket.close();
                    Log.d("MATT!", "UDP Socket Closed");
                }
                Log.d("MATT!", "Made it here");
            }
        }
        private void localThread()
        {
            while (streamData)
            {
                int port = 5003;
                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);;
                DatagramSocket socket = null;
                String recievedData;
                try {
                    socket = new DatagramSocket(port);
                } catch (SocketException e) {
                    //message.setText("EXCEPTION THROWN: could not create new DatagramSocket");
                    Log.e("EXCEPTION THROWN", "could not create new DatagramSocket");
                }
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    Log.e("EXCEPTION THROWN", "could not receive packet");
                }
                recievedData = new String(buffer, 0, packet.getLength());
                threadMsgLocal(recievedData);
                socket.close();
            }

        }
        private void threadMsgLocal(String s)
        {
            Message msg = Message.obtain(); // Creates an new Message instance
            msg.obj = s; // Put the string into Message, into "obj" field.
            msg.setTarget(mhandler); // Set the Handler
            try
            {
                msg.sendToTarget();
            }
            catch (Exception e)
            {
                Log.e("IRIS!", "exception thrown");
            }
        }
    }


    //---------------Getter and Setter Methods()------------------


    /**
     *
     * @param streamData
     */
    public void setStreamData(boolean streamData) {
        this.streamData = streamData;
    }
}
