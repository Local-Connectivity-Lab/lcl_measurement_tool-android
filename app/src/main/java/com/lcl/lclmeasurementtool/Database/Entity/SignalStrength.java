package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "signal_strength_table")
public class SignalStrength {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "time_stamp")
    public String timestamp;

    @ColumnInfo(name = "signal_strength")
    public int signalStrength;

    @ColumnInfo(name = "signal_strength_level")
    public int level;

//    @Nullable
//    public LatLng location;

    public SignalStrength(@NonNull String timestamp, int signalStrength, int level) {
        this.timestamp = timestamp;
        this.signalStrength = signalStrength;
        this.level = level;
//        this.location = location;
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

    //    @NonNull
//    public LatLng getLocation() {
//        return location;
//    }
}
