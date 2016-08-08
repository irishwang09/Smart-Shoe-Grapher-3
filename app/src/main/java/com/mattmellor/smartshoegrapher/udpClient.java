package com.mattmellor.smartshoegrapher;

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


    public UdpClient(String ipAddress, int remoteServerPort, int localPort, int dataSetsPerPacket) throws IOException{
        this.remoteServerPort = remoteServerPort;
        this.serverAddress = InetAddress.getByName(ipAddress);
        this.dataSetsPerPacket = dataSetsPerPacket;
        this.localPort = localPort;
        this.socket = new DatagramSocket(localPort);
    }

    public void acknowledgeServer()throws IOException{ //Need to implement a try catch
        byte[] buf = "Android Phone Connection ".getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, remoteServerPort);
        socket.send(packet);
    }

    //TODO Needs work
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
        //TODO
        //Implementing runnable as opposed to extending thread seems like the right choice here
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
