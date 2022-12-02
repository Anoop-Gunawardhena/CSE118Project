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
import android.graphics.Color;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.navigation.databinding.ActivityMapsBinding;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
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
    private LocEntry GOOGLEPLEX = new LocEntry("GOOGLEPLEX", new LatLng(37.424041076107144, -122.08142909056079));

    // Geofencing fields
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();

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

        /**
         * MARKER SETUP
         */
        addMarkersMain();

        System.out.println("Finished adding markers. My Location Setup");

        /**
         * MY LOCATION SETUP
         */
        myLocationSetupMain();

        System.out.println("My Location enabled. Geofencing");

        /**
         * GEOFENCING SETUP
         */
        geofenceSetupMain();
    }

    /**
     * MARKER SETUP HELPER METHODS
     */

    /**
     * Add Markers Main Helper Method
     */
    private void addMarkersMain() {

        // 1. Add all markers to map
        mMap.addMarker(new MarkerOptions().position(CSE_BUILDING.latLng).title(CSE_BUILDING.name));
        mMap.addMarker(new MarkerOptions().position(CSE_CROSSWALK.latLng).title(CSE_CROSSWALK.name));
        mMap.addMarker(new MarkerOptions().position(GOOGLEPLEX.latLng).title(GOOGLEPLEX.name));

        // 2. Center camera on CSE_BUILDING marker and set zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLng(CSE_BUILDING.latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));

    }


    /**
     * LOCATIONS PERMISSIONS HELPER METHODS
     */

    /**
     * My Location Setup Main Helper Method
     */
    private void myLocationSetupMain() {

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

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
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Location permissions granted. Enabling user location...");
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        System.out.println("Location permissions not granted. Requesting user for location permissions...");
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_FINE_LOCATION, true);
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_COARSE_LOCATION, true);
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_BACKGROUND_LOCATION, true);

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
                        Manifest.permission.ACCESS_COARSE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        permission.ACCESS_BACKGROUND_LOCATION)) {
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


    /**
     * GEOFENCING HELPER METHODS
     */

    /**
     * Geofence Setup Main Helper Method
     */
    private void geofenceSetupMain() {

        geofencingClient = LocationServices.getGeofencingClient(this);

        // Add Geofences to geofenceList
        addGeofenceToList(CSE_CROSSWALK, Constants.GEOFENCE_RADIUS_IN_METERS);  // 1. CSE_CROSSWALK Geofence
//        addGeofenceToList(GOOGLEPLEX, Constants.GEOFENCE_RADIUS_IN_METERS);  // 2. GOOGLEPLEX Geofence

        System.out.println("geofenceList: " + geofenceList.toString());

        // Add all Geofences to geofencingClient
        addGeofencesToClient();

    }

    /**
     * Helper method to add Geofence to geofenceList
     */
    private void addGeofenceToList(LocEntry locEntry, float geofenceRadius) {

        // Add geofence to geofenceList
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(locEntry.name)

                .setCircularRegion(
                        locEntry.latLng.latitude,
                        locEntry.latLng.longitude,
                        geofenceRadius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        // Add visual geofence circle to CSE_CROSSWALK geofence
        mMap.addCircle(new CircleOptions().center(locEntry.latLng)
                        .radius(geofenceRadius)
                        .strokeColor(Color.RED).strokeWidth(4f))
                .setFillColor(Color.argb(64, 255, 0, 0));

    }

    /**
     * Helper method to add all Geofences to Geofencing Client
     */
    @SuppressLint("MissingPermission")
    private void addGeofencesToClient() {

        // Add all Geofences in geofenceList to Geofencing Client to publish to application geofencing API
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())

            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    System.out.println("onSuccess: Geofences added successfully to Geofencing Client to publish to application geofencing API");
                }
            })

            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("onFailure: ");
                    System.out.println("Beginning of geofencingClient.addGeofences.onFailure() Exception --- ");
                    e.printStackTrace();
                    System.out.println(" --- End of geofencingClient.addGeofences.onFailure() Exception");
                }

            });

    }

    /**
     * Specifies geofences and initial triggers
     */
    private GeofencingRequest getGeofencingRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();

    }

    /**
     * Defines a broadcast receiver for geofence transitions
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

}