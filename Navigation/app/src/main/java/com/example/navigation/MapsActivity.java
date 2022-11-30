package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.navigation.databinding.ActivityMapsBinding;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /** GOOGLE MAPS PLATFORMS INSTANCE VARIABLES **/
    // Maps fields
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // Permissions fields
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    // Marker fields
    private LocEntry CSE_BUILDING = new LocEntry("CSE_BUILDING", new LatLng(32.881809, -117.233460));
    private LocEntry CSE_CROSSWALK = new LocEntry("CSE_CROSSWALK", new LatLng(32.881854, -117.233143));

    // Geofencing fields
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();

    /** PUBNUB INSTANCE VARIABLES **/
    // PubNub connection instance field
    private PubNub pubnub;

    // Channel detail fields
    private final String UUID = "androidPubnub";

    // Rick Channel details
    private final String rickChannelName = "androidToVoiceflowChannel";
    private final String rickPublishKey = "pub-c-985fb8a7-d30c-40b9-8532-77173422dd55";
    private final String rickSubscribeKey = "sub-c-b3862175-ffc5-4c24-9a0b-f238c09b2d26";

    // Laurenz Channel details
    private final String laurenzChannelName = "Channel-Barcelona";
    private final String laurenzPublishKey = "pub-c-f30c3204-e2ee-4907-9d2a-57f68b84a3e9";
    private final String laurenzSubscribeKey = "sub-c-abee09f7-35bc-4433-b4ba-ba12da2c5028";

    // PubNub messages fields
    private final String CROSSWALK_DETECTED = "crosswalk detected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /**
         * MAPS API SETUP
         */
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        /**
         * PUBNUP CONNECTION SETUP
         */
        pubnubMain();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        System.out.println("Adding markers");

        // Set initial map position to be at CSE Building
        mMap.addMarker(new MarkerOptions().position(CSE_BUILDING.latLng).title(CSE_BUILDING.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(CSE_BUILDING.latLng));

        // Mark CSE Crosswalk
        mMap.addMarker(new MarkerOptions().position(CSE_CROSSWALK.latLng).title(CSE_CROSSWALK.name));

        mMap.moveCamera(CameraUpdateFactory.zoomTo(19));

        System.out.println("Finished adding markers. Geofencing");

        /**
         * MY LOCATION SETUP
         */
        // My Location Layer setup: Get current user location
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        /**
         * GEOFENCING SETUP
         */
        // Instantiate Geofencing API client
        geofencingClient = LocationServices.getGeofencingClient(this);

        // Add a geofence to geofenceList
        geofenceList.add(new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(CSE_CROSSWALK.name)

            .setCircularRegion(
                    CSE_CROSSWALK.latLng.latitude,
                    CSE_CROSSWALK.latLng.longitude,
                    Constants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                    Geofence.GEOFENCE_TRANSITION_EXIT)
            .build());

        // Add Geofences in geofenceList to Geofencing Client to publish to application geofencing API
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Geofences added
                    // ...
                    System.out.println("SUCCESS: Geofences added successfully to Geofencing Client to publish to application geofencing API");
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to add geofences
                    // ...
                    System.out.println("FAILURE: failed to add Geofences to Geofencing Client to publish to application geofencing API");
                }
            });

    }

    /**
     * GEOFENCING HELPER METHODS
     */

    /**
     * Specifies geofences and initial triggers
     * @return
     */
    private GeofencingRequest getGeofencingRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();

    }

    /**
     * Defines a broadcast receiver for geofence transitions
     * @return
     */
    private PendingIntent getGeofencePendingIntent() {

        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;

    }

    /**
     * LOCATIONS PERMISSIONS HELPER METHODS
     */

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Location permissions granted. Enabling user location...");
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        System.out.println("Location permissions not granted. Requesting user for location permissions...");
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_FINE_LOCATION, true);
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_COARSE_LOCATION, true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    /**
     * PUBNUB HELPER METHODS
     */


    public void pubnubMain() {

        try {

            /**
             * PUBNUB CONNECTION SETUP
             */
            // Instantiate PubNub
            pubnub = createPubNubConnection();

            // Add subscribeCallBack listener to pubnub client
            SubscribeCallback subscribeCallback = createSubscribeCallback();
            pubnub.addListener(subscribeCallback);

            /**
             * PUBNUB PUBLISH/SUBSCRIBE
             */
            // Publish CROSSWALK_DETECTED message
            pubnub.publish().message(CROSSWALK_DETECTED).channel(laurenzChannelName).async(createPNCallback());

            // Subscribe to androidToVoiceflowChannel to receive CROSSWALK_DETECTED message
            pubnub.subscribe().channels(Arrays.asList(laurenzChannelName)).execute();

        } catch (PubNubException e) {
            System.out.println("*** EXCEPTION IN PUBNUB CONNECTION ***");
            e.printStackTrace();
        }

    }

    /**
     * Helper method to create PubNub Connection object which establishes connection to PubNub
     * Channel
     * @return
     */
    public PubNub createPubNubConnection() throws PubNubException {

        PNConfiguration pnConfiguration = new PNConfiguration(UUID);
        pnConfiguration.setSubscribeKey(laurenzSubscribeKey);
        pnConfiguration.setPublishKey(laurenzPublishKey);

        return new PubNub(pnConfiguration);

    }

    /**
     * create PubNub Subscribe Callback object helper method
     * @return
     */
    public SubscribeCallback createSubscribeCallback() {

        SubscribeCallback subscribeCallback = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    // This event happens when radio / connectivity is lost
                    pubnub.reconnect();
                }
                else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    // Connect event. You can do stuff like publish, and know you'll get it.
                    // Or just use the connected event to confirm you are subscribed for
                    // UI / internal notifications, etc
                    System.out.println("PubNub Android client connected to: " + laurenzChannelName);
                }
                else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                    // Happens as part of our regular operation. This event happens when
                    // radio / connectivity is lost, then regained.
                    System.out.println("PubNub Android client reconnected to: " + laurenzChannelName);
                }
                else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
                    // Handle messsage decryption error. Probably client configured to
                    // encrypt messages and on live data feed it received plain text.
                    pubnub.reconnect();
                }

            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {

                // Handle new message stored in message.message
                if (message.getChannel() != null) {
                    // Message has been received on channel group stored in
                    // message.getChannel()
                }
                else {
                    // Message has been received on channel stored in
                    // message.getSubscription()
                }

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
                    // Handle message publish error. Check 'category' property to find out possible issue
                    // because of which request did fail.
                    // Request can be resent using: [status retry];
                    status.retry();
                }

            }

        };

        return pnCallback;

    }

}