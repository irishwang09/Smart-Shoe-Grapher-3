package com.mattmellor.smartshoegrapher;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Matthew on 8/5/2016.
 * Class that represents a udp wifi client
 * That will accept incoming datagrams(packets)
 */

public class UdpClient implements Runnable {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int remoteServerPort;
    private int localPort;
    private int dataSetsPerPacket;
    public DatagramPacket rcvdPacket;


    public UdpClient(String ipAddress, int remoteServerPort, int localPort, int dataSetsPerPacket){
        try {
            Log.d("MATT!", "Attempting to Initialize Object");
            this.remoteServerPort = remoteServerPort;
            this.serverAddress = InetAddress.getByName(ipAddress);
            this.dataSetsPerPacket = dataSetsPerPacket;
            this.localPort = localPort;
            this.socket = new DatagramSocket(localPort);
            Log.d("MATT!", "Object Initialized");
        }catch(Exception e){
            Log.e("MATT!", "Object Initialization Failed");
        }

    }

    public void acknowledgeServer(){ //Need to implement a try catch
        try {
            byte[] buf = "Android Phone Connection ".getBytes();
            InetAddress address = InetAddress.getByName("18.111.41.17");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 2391);
            //DatagramSocket socket2 = new DatagramSocket(5005);
            socket.send(packet);
            socket.close();
        }catch (Exception e){
            Log.d("MATT!", "Caught Acknowledge Server Exception");
            socket.close();
        }
    }


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

    @Override
    public void run(){
        //listen to server for data
        try {
            listenToServer();
        } catch (IOException e) {
            e.printStackTrace();
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
