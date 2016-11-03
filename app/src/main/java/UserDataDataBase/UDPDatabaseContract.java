package UserDataDataBase;

import android.provider.BaseColumns;

/**
 * Created by Matthew on 11/3/2016.
 * This class is a SQLite database Contract class
 * It is meant to specify the layout of the database
 */

public final class UDPDatabaseContract {

    //To avoid accidentally instantiating this class
    private UDPDatabaseContract(){};


    /* Inner class that defines the table contents*/
    public static class UdpDataEntry implements BaseColumns{
        public static final String TABLE_NAME = "UserUDPSensors";
        public static final String COLUMN_NAME_IP_HOST = "IP/HOSTNAME";
        public static final String COLUMN_NAME_LOCAL_PORT = "LOCAL_PORT";
        public static final String COLUMN_NAME_REMOTE_PORT = "REMOTE_PORT";

    }

}
