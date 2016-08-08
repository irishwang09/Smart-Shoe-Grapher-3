import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Matthew on 8/5/2016.
 */

public class udpClient {

    //Goal is so that this will be able to run in a separate thread than
    //the graphing

    private DatagramSocket socket;
    private int remoteServerPort;
    private InetAddress serverAddress;

    public udpClient(String ipAddress, int remoteServerPort, int localPort) throws IOException{
        this.remoteServerPort = remoteServerPort;
        this.socket = new DatagramSocket(localPort);
    }
    

    /**
     *  Send a quick message to the server to
     *  allow the server to obtain the Android device
     *  IP address
     */
    public void acknowledgeServer()throws IOException{ //Need to implement a try catch
        byte[] buf = "It is I Android. Grab my IP Address".getBytes();
        InetAddress address = InetAddress.getByName("footsensor1.dynamic-dns.net");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 2391);
        socket.send(packet);

        //Wait a reply in the button pushing code

    }

    public void listenToServer(){
        //Implement method to get data from user about hostname port, etc.
    }



}
