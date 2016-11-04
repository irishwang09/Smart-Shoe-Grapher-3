package UserDataDataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Matthew on 11/3/2016.
 * Provides access to the dataBase 'UDPUserData.db' by
 *  either creating or opening the dataBase
 */

public class UDPDataBaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "UDPUserData.db";

    public UDPDataBaseHelper(Context context){
        //The line below corresponds to
        //SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(UDPDatabaseContract.UdpDataEntry.SQL_CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //We shouldn't have to use this function bc we shouldn't have to update
        //the schema/layout of the database
        db.execSQL(UDPDatabaseContract.UdpDataEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
