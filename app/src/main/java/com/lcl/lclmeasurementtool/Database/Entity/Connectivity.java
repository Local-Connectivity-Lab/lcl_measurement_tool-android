package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "connectivity_table")
@TypeConverters({Converters.class})
public class Connectivity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "time_stamp")
    public String timestamp;

    public double ping;

    public double upload;

    public double download;

    @NonNull
    public LatLng location;

    public Connectivity(@NonNull String timestamp, double ping, double upload, double download, @NonNull LatLng location) {
        this.timestamp = timestamp;
        this.ping = ping;
        this.upload = upload;
        this.download = download;
        this.location = location;
    }

    @NonNull
    public String getTimestamp() {
        return timestamp;
    }

    public double getPing() {
        return ping;
    }

    public double getUpload() {
        return upload;
    }

    public double getDownload() {
        return download;
    }

    @NonNull
    public LatLng getLocation() {
        return location;
    }
}

