package com.example.navigation;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

public class CrosswalkDetectionGeofenceBroadcastReceiver extends GeofenceBroadcastReceiver {

    /**
     * MESSAGE PUBLICATION POST-CROSSWALK DETECTED FEATURE
     *
     * Publish PubNub message to start Voiceflow workflow if user is proximal to a crosswalk (GEOFENCE_TRANSITION_ENTER is a success)
     */
    @Override
    protected void mapTransition(Context context, int transition) {

        switch (transition) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "Crosswalk Detection GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                System.out.println("onReceive: Crosswalk Detection GEOFENCE TRANSITION ENTER detected. Publishing CROSSWALK_DETECTED message to " + Constants.laurenzChannelName);
                pubnubMain(Constants.CROSSWALK_DETECTED);                           // PUBNUB PUBLICATION
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Crosswalk Detection GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                System.out.println("onReceive: Crosswalk Detection GEOFENCE TRANSITION EXIT detected");
                break;

            default:
                Toast.makeText(context, "Crosswalk Detection GEOFENCE_TRANSITION_UNKNOWN", Toast.LENGTH_SHORT).show();
                System.out.println("onReceive: Crosswalk Detection Geofence transition code " + transition + " detected. Unknown Action. Pass.");
                break;

        }

    }

}
