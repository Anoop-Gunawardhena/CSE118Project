package com.example.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.navigation.TextToSpeech.ObservableObject;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

/**
 * GEOFENCE BROADCAST RECEIVER CLASS
 * <p>
 * Includes MESSAGE PUBLICATION POST-CROSSWALK DETECTED feature
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    /**
     * PUBNUB INSTANCE VARIABLES
     **/

    @Override
    public void onReceive(Context context, Intent intent) {

        // Get Geofence Event onReceive Intent
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            System.out.println("onReceive: " + GeofenceStatusCodes.getStatusCodeString(event.getErrorCode()));
            return;
        }

        // Perform map transition for all triggering Geofences of event
        for (Geofence geofence : event.getTriggeringGeofences()) {

            String geofenceRequestId = geofence.getRequestId();
            float geofenceRadius = geofence.getRadius();

            System.out.println("onReceive: Geofence id = " + geofenceRequestId);
            System.out.println("onReceive: Geofence radius = " + geofenceRadius);

            // Publish 'crosswalk detected' message to Laurenz's PubNub Channel
            if (geofenceRequestId.equals(Constants.CSE_CROSSWALK.name)) {
                mapTransitionCrosswalkDetection(context, event.getGeofenceTransition());
            }

            // Publish 'veering off path' message to Laurenz's PubNub Channel
            else if (geofenceRequestId.equals(Constants.CSE_WALKING_STRAIGHT_TOP.name) || geofenceRequestId.equals(Constants.CSE_WALKING_STRAIGHT_BOTTOM.name)) {
                mapTransitionWalkingStraight(context, event.getGeofenceTransition());
            } else {
                System.out.println("onReceive: NOT A KNOWN GEOFENCE TYPE TRIGGERED");
            }

        }

    }

    /**
     * MESSAGE PUBLICATION POST-CROSSWALK DETECTED FEATURE
     * Publish PubNub message to start Voiceflow workflow if user is proximal to a crosswalk (GEOFENCE_TRANSITION_ENTER is a success)
     */

    /**
     * Publish 'crosswalk detected' message to Laurenz's PubNub Channel
     */
    private void mapTransitionCrosswalkDetection(Context context, int transition) {

        switch (transition) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "Crosswalk Detection GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                //The user entered the crosswalk geofence and is then asked if he wants to cross
                ObservableObject.getInstance().updateValue(Constants.CROSSWALK_DETECTED);
                System.out.println("mapTransition: Crosswalk Detection GEOFENCE TRANSITION ENTER detected. Publishing CROSSWALK_DETECTED message to " + Constants.laurenzChannelName);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Crosswalk Detection GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                System.out.println("mapTransition: Crosswalk Detection GEOFENCE TRANSITION EXIT detected");
                break;

            default:
                Toast.makeText(context, "Crosswalk Detection GEOFENCE_TRANSITION_UNKNOWN", Toast.LENGTH_SHORT).show();
                System.out.println("mapTransition: Crosswalk Detection Geofence transition code " + transition + " detected. Unknown Action. Pass.");
                break;

        }

    }


    /**
     * Publish 'veering off path' message to Laurenz's PubNub Channel
     */
    private void mapTransitionWalkingStraight(Context context, int transition) {

        switch (transition) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                System.out.println("onReceive: Walking Straight GEOFENCE TRANSITION ENTER detected");
                //The broadcast recevier and the activity exchange message over the observer pattern
                ObservableObject.getInstance().updateValue("Walking straight");
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                System.out.println("onReceive: Walking Straight GEOFENCE TRANSITION EXIT detected. Publishing CROSSWALK_DETECTED message to " + Constants.laurenzChannelName);
                //The broadcast recevier and the activity exchange message over the observer pattern
                ObservableObject.getInstance().updateValue("Not walking straight");
                break;

            default:
                Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_UNKNOWN", Toast.LENGTH_SHORT).show();
                System.out.println("onReceive: Walking Straight Geofence transition code " + transition + " detected. Unknown Action. Pass.");
                break;

        }

    }
}