package com.lcl.lclmeasurementtool.database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;

/**
 * A class holding the connectivity measurement entity
 */
@Entity(tableName = "connectivity_table")
@TypeConverters({LocationUtils.class})
public class Connectivity implements DataEncodable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "time_stamp")
    public String timestamp;

    public double ping;

    public double upload;

    public double download;

//    public double packetLoss;

    @NonNull
    public LatLng location;

    public Connectivity(@NonNull String timestamp, double ping, double upload, double download, @NonNull LatLng location) {
        this.timestamp = timestamp;
        this.ping = ping;
        this.upload = upload;
        this.download = download;
        this.location = location;
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
     * Retrieve the ping stat
     * @return the ping stat
     */
    public double getPing() {
        return ping;
    }

    /**
     * Retrieve the upload speed
     * @return the upload speed in Mbps
     */
    public double getUpload() {
        return upload;
    }

    /**
     * Retrieve the download speed
     * @return the download speed in Mbps
     */
    public double getDownload() {
        return download;
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

//    public double getPacketLoss() {
//        return packetLoss;
//    }

    /**
     * Retrieve the header of the signal strength table
     * @return the headers of the signal strength table
     */
    public static String[] getHeader() {
        return new String[]{"timestamp", "ping", "upload", "download", "latitude", "longitude"};
    }

    @Override
    public String[] toArray() {
        return new String[]{timestamp, String.valueOf(ping), String.valueOf(upload), String.valueOf(download), String.valueOf(location.latitude), String.valueOf(location.longitude)};
    }
}

