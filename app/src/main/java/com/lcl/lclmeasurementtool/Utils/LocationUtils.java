package com.lcl.lclmeasurementtool.Utils;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

/**
 * A utility class for the Room database to understand complex location data
 */
@ProvidedTypeConverter
public class LocationUtils {

    /**
     * Convert location to latlng
     *
     * @param l a location to convert
     * @return  the latlng corresponds to the location information
     */
    public static LatLng toLatLng(Location l) {
        return new LatLng(l.getLatitude(), l.getLongitude());
    }

    /**
     * A type converter to convert latlng to its string representation
     * @param latLng the latlng object to be converted
     * @return       the string representation of the latlng object
     */
    @TypeConverter
    public static String fromLatLng(@NonNull LatLng latLng) {
        return latLng.latitude + ", " + latLng.longitude;
    }

    /**
     * A type converter to convert from string to latlng object
     * @param val  the string representation of the latlng
     * @return     a latlng object corresponds to the string representation
     */
    @TypeConverter
    public static LatLng fromString(@NonNull String val) {
        String[] tmp = val.split(",( )*");
        return new LatLng(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
    }

}
