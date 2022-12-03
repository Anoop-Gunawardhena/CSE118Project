package com.example.navigation;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

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
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    /** PUBNUB INSTANCE VARIABLES **/
    // PubNub connection instance field
    private PubNub pubnub;

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
            else if (geofenceRequestId.equals(Constants.CSE_WALKING_STRAIGHT_1.name)) {
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
                System.out.println("mapTransition: Crosswalk Detection GEOFENCE TRANSITION ENTER detected. Publishing CROSSWALK_DETECTED message to " + Constants.laurenzChannelName);
                pubnubMain(Constants.CROSSWALK_DETECTED);
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
            pubnubMain(Constants.NOT_WALKING_STRAIGHT_DETECTED);
            break;

        default:
            Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_UNKNOWN", Toast.LENGTH_SHORT).show();
            System.out.println("onReceive: Walking Straight Geofence transition code " + transition + " detected. Unknown Action. Pass.");
            break;

    }

}

    /**
     * PUBNUB HELPER METHODS
     */

    /**
     * PubNub connection and publication Main Helpher Method
     */
    public void pubnubMain(String message) {

        try {

            /**
             * PUBNUB CONNECTION SETUP
             */
            // Instantiate PubNub
            pubnub = createPubNubConnection();

            // Add subscribeCallBack listener to pubnub client
            pubnub.addListener(createSubscribeCallback());

            /**
             * PUBNUB PUBLISH/SUBSCRIBE
             */
            // Publish CROSSWALK_DETECTED message
            pubnub.publish().message(message).channel(Constants.laurenzChannelName).async(createPNCallback());

            // Subscribe to androidToVoiceflowChannel to receive CROSSWALK_DETECTED message
            pubnub.subscribe().channels(Arrays.asList(Constants.laurenzChannelName)).execute();

        } catch (PubNubException e) {
            System.out.println("*** EXCEPTION IN PUBNUB CONNECTION ***");
            e.printStackTrace();
        }

    }

    /**
     * Helper method to create PubNub Connection object which establishes connection to PubNub channel
     */
    public PubNub createPubNubConnection() throws PubNubException {

        PNConfiguration pnConfiguration = new PNConfiguration(Constants.UUID);
        pnConfiguration.setSubscribeKey(Constants.laurenzSubscribeKey);
        pnConfiguration.setPublishKey(Constants.laurenzPublishKey);

        return new PubNub(pnConfiguration);

    }

    /**
     * create PubNub Subscribe Callback object helper method
     */
    public SubscribeCallback createSubscribeCallback() {

        SubscribeCallback subscribeCallback = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    pubnub.reconnect();
                }
                else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    System.out.println("PubNub Android client connected to: " + Constants.laurenzChannelName);
                }
                else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                    System.out.println("PubNub Android client reconnected to: " + Constants.laurenzChannelName);
                }
                else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
                    pubnub.reconnect();
                }

            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {

                // Handle new message stored in message.message
                String messagePublisher = message.getPublisher();
                System.out.println("Message publisher: " + messagePublisher);
                System.out.println("Message Subscription: " + message.getSubscription());
                System.out.println("Message Channel: " + message.getChannel());
                System.out.println("Message: " + message.getMessage());

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {
            }

            @Override
            public void uuid(@NotNull PubNub pubNub, @NotNull PNUUIDMetadataResult pnuuidMetadataResult) {
            }

            @Override
            public void channel(@NotNull PubNub pubNub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {
            }

            @Override
            public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {
            }

            @Override
            public void messageAction(@NotNull PubNub pubNub, @NotNull PNMessageActionResult pnMessageActionResult) {
            }

            @Override
            public void file(@NotNull PubNub pubNub, @NotNull PNFileEventResult pnFileEventResult) {
            }

        };

        return subscribeCallback;
    }

    /**
     * create PubNub Callback object helper method
     * @return
     */
    public PNCallback<PNPublishResult> createPNCallback() {

        PNCallback<PNPublishResult> pnCallback = new PNCallback<PNPublishResult>() {

            @Override
            public void onResponse(PNPublishResult result, PNStatus status) {

                // Check whether request successfully completed or not.
                if (!status.isError()) {
                    // Message successfully published to specified channel.
                    System.out.println("Message successfully published to specified channel");
                }

                // Request processing failed.
                else {
                    status.retry();
                }

            }

        };

        return pnCallback;

    }

}