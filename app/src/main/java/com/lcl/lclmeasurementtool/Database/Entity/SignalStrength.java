package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "signal_strength_table")
@TypeConverters({Converters.class})
public class SignalStrength {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "time_stamp")
    public String timestamp;

    @ColumnInfo(name = "signal_strength")
    public int signalStrength;

    @ColumnInfo(name = "signal_strength_level")
    public int level;

    @Nullable
    public LatLng location;

    public SignalStrength(@NonNull String timestamp, int signalStrength, int level, LatLng location) {
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

    @Nullable
    public LatLng getLocation() {
        return location;
    }
}
