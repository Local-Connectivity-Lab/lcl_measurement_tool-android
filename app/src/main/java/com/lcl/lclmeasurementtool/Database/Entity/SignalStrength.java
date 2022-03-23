package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;

import java.util.Arrays;
import java.util.List;

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

    public int getSignalStrength() {
        return signalStrength;
    }

    @NonNull
    public String getTimestamp() {
        return timestamp;
    }

    public int getLevel() {
        return level;
    }

    @NonNull
    public LatLng getLocation() {
        return location;
    }

    public String getLocationString() {
        return location.latitude + "|" + location.longitude;
    }

    public static String[] getHeader() {
        return new String[]{"timestamp", "signal_strength", "level", "latitude", "longitude"};
    }

    @Override
    public String[] toCSV() {
        return new String[]{timestamp, String.valueOf(signalStrength), String.valueOf(level), String.valueOf(location.latitude), String.valueOf(location.longitude)};
    }
}
