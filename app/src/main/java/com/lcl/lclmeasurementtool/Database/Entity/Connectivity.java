package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;

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

    public String getLocationString() {
        return location.latitude + "|" + location.longitude;
    }

//    public double getPacketLoss() {
//        return packetLoss;
//    }

    public static String[] getHeader() {
        return new String[]{"timestamp", "ping", "upload", "download", "latitude", "longitude"};
    }

    public String[] toCSV() {
        return new String[]{timestamp, String.valueOf(ping), String.valueOf(upload), String.valueOf(download), String.valueOf(location.latitude), String.valueOf(location.longitude)};
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Connectivity that = (Connectivity) o;

        if (Double.compare(that.ping, ping) != 0) return false;
        if (Double.compare(that.upload, upload) != 0) return false;
        if (Double.compare(that.download, download) != 0) return false;
        if (!timestamp.equals(that.timestamp)) return false;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = timestamp.hashCode();
        temp = Double.doubleToLongBits(ping);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(upload);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(download);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + location.hashCode();
        return result;
    }
}

