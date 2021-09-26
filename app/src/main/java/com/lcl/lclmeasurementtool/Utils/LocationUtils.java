package com.lcl.lclmeasurementtool.Utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class LocationUtils {

    public static LatLng toLatLng(Location l) {
        return new LatLng(l.getLatitude(), l.getLongitude());
    }
}
