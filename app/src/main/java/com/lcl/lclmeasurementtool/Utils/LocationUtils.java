package com.lcl.lclmeasurementtool.Utils;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

@ProvidedTypeConverter
public class LocationUtils {

    public static LatLng toLatLng(Location l) {
        return new LatLng(l.getLatitude(), l.getLongitude());
    }

    @TypeConverter
    public static String fromLatLng(@NonNull LatLng latLng) {
        return latLng.latitude + ", " + latLng.longitude;
    }

    @TypeConverter
    public static LatLng fromString(@NonNull String val) {
        String[] tmp = val.split(",( )*");
        return new LatLng(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
    }

}
