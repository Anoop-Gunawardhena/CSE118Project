package com.example.navigation;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    // Color fields
    public static final int LIGHT_RED = Color.argb(64, 255, 0, 0);
    public static final int LIGHT_GREEN = Color.argb(64, 0, 255, 0);
    public static final int LIGHT_BLUE = Color.argb(64, 0, 0, 255);

    // Marker fields
    public static final LocEntry CSE_BUILDING = new LocEntry("CSE_BUILDING", new LatLng(32.881809, -117.233460));
    public static final LocEntry CSE_CROSSWALK = new LocEntry("CSE_CROSSWALK", new LatLng(32.881811, -117.233184));

    public static final LocEntry CSE_WALKING_STRAIGHT_0 = new LocEntry("CSE_WALKING_STRAIGHT_0", new LatLng(32.881868, -117.233136));
    public static final LocEntry CSE_WALKING_STRAIGHT_1 = new LocEntry("CSE_WALKING_STRAIGHT_1", new LatLng(32.881881, -117.233124));

    public static final LocEntry CSE_WALKING_STRAIGHT_TOP = new LocEntry("CSE_WALKING_STRAIGHT_TOP", new LatLng(32.881988, -117.233211));
    public static final LocEntry CSE_WALKING_STRAIGHT_BOTTOM = new LocEntry("CSE_WALKING_STRAIGHT_BOTTOM", new LatLng(32.881751, -117.233026));

    // Permissions fields
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static boolean permissionDenied = false;

    // Geofence constants
    public static final float GEOFENCE_RADIUS_FOR_CROSSWALK_DETECTION = (float) 50;
    public static final float GEOFENCE_RADIUS_FOR_WALKING_STRAIGHT = (float) 15;

    // Geofence Types
    public static final String CROSSWALK_DETECTION_GEOFENCE = "CROSSWALK_DETECTION";
    public static final String WALKING_STRAIGHT_GEOFENCE = "WALKING_STRAIGHT";

    // PubNub Channel detail fields
    public static final String UUID = "androidPubnub";

    // Rick Channel details
    public static final String rickChannelName = "androidToVoiceflowChannel";
    public static final String rickPublishKey = "pub-c-985fb8a7-d30c-40b9-8532-77173422dd55";
    public static final String rickSubscribeKey = "sub-c-b3862175-ffc5-4c24-9a0b-f238c09b2d26";

    // Laurenz Channel details
    public static final String laurenzChannelName = "Channel-Barcelona";
    public static final String laurenzPublishKey = "pub-c-f30c3204-e2ee-4907-9d2a-57f68b84a3e9";
    public static final String laurenzSubscribeKey = "sub-c-abee09f7-35bc-4433-b4ba-ba12da2c5028";

    // PubNub messages fields
    public static final String CROSSWALK_DETECTED = "There is a crosswalk detected, do you want to cross?";
    public static final String NOT_WALKING_STRAIGHT_DETECTED = "not walking straight detected";

}
