package com.mattmellor.smartshoegrapher;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
 *
 * Benefits of this approach
 *      1. Scalability
 *          Since each instance of UdpClient will have a separate instance of
 *          threads that ping the server and read the data, code should be scalable to
 *          allow for multiple udp connections at once.
 */

public class UdpClient {

    private DatagramSocket pingSocket;
    private DatagramSocket receiveSocket;
    private InetAddress serverAddress;
    private int remoteServerPort;
    private int localPort;
    private int dataSetsPerPacket;
    public DatagramPacket rcvdPacket;
    private boolean streamData = true;

    /**
     * @param ipAddress : String representing the ip Address/hostname of the remote server
     * @param remoteServerPort : int representing the value of the remote port of the server
     * @param localPort: int representing the value of the local port of the server
     * @param dataSetsPerPacket: int representing the number of data sets per packet
     */
    public UdpClient(String ipAddress, int remoteServerPort, int localPort, int dataSetsPerPacket){
        try {
            this.remoteServerPort = remoteServerPort;
            this.serverAddress = InetAddress.getByName(ipAddress);
            this.dataSetsPerPacket = dataSetsPerPacket;
            this.localPort = localPort;
        }catch(Exception e){
            Log.e("MATT!", "Object Initialization Failed");
        }
    }

    /**
     * Inner class for sending a message to the remote server
     * This is a separate class because all networking actions can't occur
     * on the main UI thread
     */
    public class UdpServerAcknowledger extends Thread{

        /**
         * value to be called by the thread
         */
        public void run(){
            acknowledgeServerNoReceive();
        }

        private void acknowledgeServerNoReceive(){
            String mess = "Ping";
            DatagramPacket packet;
            int pingLocalPort = localPort + 1; //Ensures that send a recieve sockets are different
            try{
                pingSocket = new DatagramSocket(pingLocalPort);
                packet = new DatagramPacket(mess.getBytes(), mess.length(), serverAddress, remoteServerPort);
                pingSocket.send(packet);
            }catch (SocketException e){
                e.printStackTrace();
                Log.e("MATT!", "socket exception");
            }catch(UnknownHostException e){
                e.printStackTrace();
                Log.e("MATT!", "unknown host exception");
            }catch(IOException e){
                e.printStackTrace();
                Log.e("MATT!", "IOException");
            }catch(Exception e){
                Log.e("MATT!", "General exception");
                e.printStackTrace();
            }finally {
                if(pingSocket != null){
                    pingSocket.close();
                }
            }
        }
    }

    /**
     * Class for listening to Udp remote server for a long time
     * as a separate thread
     */
    public class UdpDataListener extends Thread {
        //TODO figure out a why to stop the thread if needed
        //TODO figure out how to store the data for the graph to pick up

        public void run(){
            pingThenListenToServer();
        }

        private void pingThenListenToServer(){
            byte[] buf = new byte[1352]; //TODO calculate this number with a formula
            String received = "";
            //First try to connect to udp with a ping
            //Then start listening for data
            String mess = "Android Data Receiver";
            DatagramPacket packet;
            try{
                //Ping the server to tell server IP Address of Android phone
                receiveSocket = new DatagramSocket(localPort);
                packet = new DatagramPacket(mess.getBytes(), mess.length(), serverAddress, remoteServerPort);
                receiveSocket.send(packet);

                while (streamData) {
                        rcvdPacket = new DatagramPacket(buf, buf.length);
                        receiveSocket.receive(rcvdPacket);
                        received = new String(rcvdPacket.getData(), 0, rcvdPacket.getLength());
                        //String[] val = received.substring(0, received.length() -2).split(",");
                        Log.d("MATT!", received.substring(0, received.length() - 2));
                }
            }catch (SocketException e){
                e.printStackTrace();
                Log.e("MATT!", "socket exception");
            }catch(UnknownHostException e){
                e.printStackTrace();
                Log.e("MATT!", "unknown host exception");
            }catch(IOException e){
                e.printStackTrace();
                Log.e("MATT!", "IOException");
            }catch(Exception e){
                Log.e("MATT!", "General exception");
                e.printStackTrace();
            }finally {
                if(receiveSocket != null){
                    receiveSocket.close();
                }
            }

        }
    }


    //---------------Getter and Setter Methods()------------------

    /**
     * @return InetAddress representing the address of the remote server
     */
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    /**
     * @param serverAddress set the value of the remote server address
     *                      must be a InetAddress object
     */
    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * @return int remote port of the server
     */
    public int getRemoteServerPort() {
        return remoteServerPort;
    }

    /**
     * @param remoteServerPort set the remote server port
     *                         must be greater than 0
     */
    public void setRemoteServerPort(int remoteServerPort) {
        this.remoteServerPort = remoteServerPort;
    }

    /**
     * @return local port used
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * @param localPort to set to use for udp
     *                  must be greater than 0
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    /**
     * @return int representing the number of data sets per udp packet
     */
    public int getDataSetsPerPacket() {
        return dataSetsPerPacket;
    }

    /**
      * @param dataSetsPerPacket must be greater than 0
     */
    public void setDataSetsPerPacket(int dataSetsPerPacket) {
        this.dataSetsPerPacket = dataSetsPerPacket;
    }

    /**
     *
     * @param streamData
     */
    public void setStreamData(boolean streamData) {
        this.streamData = streamData;
    }
}
