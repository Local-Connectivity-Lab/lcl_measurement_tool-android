package com.lcl.lclmeasurementtool.database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;

/**
 * A class holding the signal strength entity
 */
@Entity(tableName = "signal_strength_table")
@TypeConverters({LocationUtils.class})
public class SignalStrength implements DataEncodable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "time_stamp")
    public String timestamp;

    @ColumnInfo(name = "signal_strength")
    public int signalStrength;

    @ColumnInfo(name = "signal_strength_level")
    public int level;

    @NonNull
    public LatLng location;

    public SignalStrength(@NonNull String timestamp, int signalStrength, int level, @NonNull LatLng location) {
        this.timestamp = timestamp;
        this.signalStrength = signalStrength;
        this.level = level;
        this.location = location;
    }

    /**
     * Retrieve the signal strength as an integer
     * @return the integer representation of the signal strength
     */
    public int getSignalStrength() {
        return signalStrength;
    }

    /**
     * Retrieve the timestamp when the signal strength data is collected
     * @return the string representation of the timestamp
     */
    @NonNull
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Retrieve the signal strength level
     * @return the integer representation of the signal strength level
     * @see com.lcl.lclmeasurementtool.Utils.SignalStrengthLevel
     */
    public int getLevel() {
        return level;
    }

    /**
     * Retrieve the location where the signal strength data is collected
     * @return the location in latlng
     */
    @NonNull
    public LatLng getLocation() {
        return location;
    }

    /**
     * Retrieve the location in string format
     * @return the string representation of the location
     */
    public String getLocationString() {
        return location.latitude + "|" + location.longitude;
    }

    /**
     * Retrieve the header of the signal strength table
     * @return the headers of the signal strength table
     */
    public static String[] getHeader() {
        return new String[]{"timestamp", "signal_strength", "level", "latitude", "longitude"};
    }

    @Override
    public String[] toArray() {
        return new String[]{timestamp, String.valueOf(signalStrength), String.valueOf(level), String.valueOf(location.latitude), String.valueOf(location.longitude)};
    }
}
