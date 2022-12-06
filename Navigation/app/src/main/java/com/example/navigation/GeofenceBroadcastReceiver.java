package com.example.navigation;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.navigation.TextToSpeech.ListenResultCallback;
import com.example.navigation.TextToSpeech.MessageReceivedCallback;
import com.example.navigation.TextToSpeech.Mic;
import com.example.navigation.TextToSpeech.PubNubUtils;
import com.example.navigation.TextToSpeech.Speaker;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * GEOFENCE BROADCAST RECEIVER CLASS
 *
 * Includes MESSAGE PUBLICATION POST-CROSSWALK DETECTED feature
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver implements ListenResultCallback, MessageReceivedCallback {

    /** PUBNUB INSTANCE VARIABLES **/
    // PubNub connection instance field
    private PubNubUtils pubnub = new PubNubUtils(this);

    @Override
    public void onReceive(Context context, Intent intent) {

        // Get Geofence Event onReceive Intent
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            System.out.println("onReceive: " + GeofenceStatusCodes.getStatusCodeString(event.getErrorCode()));
            return;
        }

        // Perform map transition for all triggering Geofences of event
        for (Geofence geofence: event.getTriggeringGeofences()) {

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
            }

            else {
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
                Speaker.speak(context, Constants.CROSSWALK_DETECTED);

                Mic.listen(context, this);
                System.out.println("mapTransition: Crosswalk Detection GEOFENCE TRANSITION ENTER detected. Publishing CROSSWALK_DETECTED message to " + Constants.laurenzChannelName);
                pubnub.publish(Constants.CROSSWALK_DETECTED);
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
            break;

        case Geofence.GEOFENCE_TRANSITION_EXIT:
            Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
            System.out.println("onReceive: Walking Straight GEOFENCE TRANSITION EXIT detected. Publishing CROSSWALK_DETECTED message to " + Constants.laurenzChannelName);
            pubnub.publish(Constants.NOT_WALKING_STRAIGHT_DETECTED);
            break;

        default:
            Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_UNKNOWN", Toast.LENGTH_SHORT).show();
            System.out.println("onReceive: Walking Straight Geofence transition code " + transition + " detected. Unknown Action. Pass.");
            break;

    }

}

    //Check if the user said yes or no
    @Override
    public void result(String result) {
        if (result.equals("yes")) {
            //Send Pubnub message to activate PI
            pubnub.publish("record");
        }
    }

    @Override
    public void message(String message) {

    }
}