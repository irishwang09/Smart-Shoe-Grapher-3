package Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;

/**
 * Created by haripriyamehta on 4/3/17.
 */

public class GraphSettingsPopupFragment extends DialogFragment {
//Right now blank

    //Look at InputUserSettingsPopupFragment for inspiration
    //DialogFragment -> meant to be popups (google it or try to look at code of InputUserSettingsPopupFragment/WirelessPairingActivity
    private GraphSettingsPopupFragment.OnDataPassGraphSettings dataPassHandle;


    //TODO: In the done button listener the method to call with just be 'dismiss()'

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity a;
        if(context instanceof Activity){
            a = (Activity) context;
            dataPassHandle = (GraphSettingsPopupFragment.OnDataPassGraphSettings) a;
            Log.d("HP!", "Made it here");
        }
        //Use dataPassHandle like dataPassHandle.methodName(inputs...);
    }


    /**
     * Interface methods implemented by wireless pairing activity to allow for communication between the activity
     * and the fragment
     * We only pass verified input
     */
    public interface OnDataPassGraphSettings{
        //void onDataPassUdpSettings(String hostname,int localPort, int remotePort);
        //boolean isLocalPortUsed(String localPort);
    }



}

