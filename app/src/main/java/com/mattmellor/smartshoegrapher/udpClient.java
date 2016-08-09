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
 *          Since each instance of UdpClient will have a seperate instance of
 *          threads that ping the server and read the data, code should be scalable to
 *          allow for multiple udp connections at once.
 */

public class UdpClient {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int remoteServerPort;
    private int localPort;
    private int dataSetsPerPacket;
    public DatagramPacket rcvdPacket;
    private final boolean streamData = true;

    public UdpClient(String ipAddress, int remoteServerPort, int localPort, int dataSetsPerPacket){
        try {
            Log.d("MATT!", "Attempting to Initialize Object");
            this.remoteServerPort = remoteServerPort;
            this.serverAddress = InetAddress.getByName(ipAddress);
            this.dataSetsPerPacket = dataSetsPerPacket;
            this.localPort = localPort;
            Log.d("MATT!", "Object Initialized");
        }catch(Exception e){
            //Log.e("MATT!", "Object Initialization Failed");
        }
    }

    public class UdpServerAcknowledger extends Thread{

        public void run(){
            acknowledgeServer();
        }

        public void acknowledgeServer(){
            String mess = "Android connection";
            DatagramPacket packet;
            try{
                socket = new DatagramSocket(localPort);
                packet = new DatagramPacket(mess.getBytes(), mess.length(), serverAddress, remoteServerPort);
                socket.send(packet);
                //Log.d("MATT!", "end of packet sending");
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
                    //Log.d("MATT!", "Made it to finally");
                }
            }
        }
    }


    private class UdpDataListener extends Thread {
        //Implements thread
        //TODO implement this class
        public void listenToServer() throws IOException{
            //Implement method to get data from user about hostname port, etc.
            byte[] buf = new byte[1352];
            int i = 0;
            String received = "";
            while(i < 50){
                rcvdPacket = new DatagramPacket(buf, buf.length);
                socket.receive(rcvdPacket);
                received = new String(rcvdPacket.getData(), 0, rcvdPacket.getLength());
                String[] val = received.substring(0, received.length() -2).split(",");
                //Put the data somewhere another thread can use
                i++;
            }
        }
    }


    //---------------Getter and Setter Methods()------------------
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getRemoteServerPort() {
        return remoteServerPort;
    }

    public void setRemoteServerPort(int remoteServerPort) {
        this.remoteServerPort = remoteServerPort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getDataSetsPerPacket() {
        return dataSetsPerPacket;
    }

    public void setDataSetsPerPacket(int dataSetsPerPacket) {
        this.dataSetsPerPacket = dataSetsPerPacket;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }
}
