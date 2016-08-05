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

    public udpClient(String ipAddress, int remoteServerPort){
        this.remoteServerPort = remoteServerPort;
        //this.serverAddress = InetAddress.getByName(ipAddress);
    }

    //TODO: Add permissions to manifest folder

    /**
     *  Send a quick message to the server to
     *  allow the server to obtain the Android device
     *  IP address
     */
    public void acknowledgeServer(){




    }



}
