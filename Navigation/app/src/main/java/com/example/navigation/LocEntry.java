package com.example.navigation;

import com.google.android.gms.maps.model.LatLng;

public class LocEntry {
    public String name;
    public LatLng latLng;

    public LocEntry(String n, LatLng lL) {
        name = n;
        latLng = lL;
    }
}
