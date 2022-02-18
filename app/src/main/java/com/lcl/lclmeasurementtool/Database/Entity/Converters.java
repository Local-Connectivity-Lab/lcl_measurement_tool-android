//package com.lcl.lclmeasurementtool.Database.Entity;
//
//import androidx.annotation.NonNull;
//import androidx.room.ProvidedTypeConverter;
//import androidx.room.TypeConverter;
//
//import com.google.android.gms.maps.model.LatLng;
//
//import java.time.Instant;
//import java.time.ZoneId;
//
//@ProvidedTypeConverter
//public class Converters {
//
//    @TypeConverter
//    public static String fromLatLng(@NonNull LatLng latLng) {
//        return latLng.latitude + ", " + latLng.longitude;
//    }
//
//    @TypeConverter
//    public static LatLng fromString(@NonNull String val) {
//        String[] tmp = val.split(",( )*");
//        return new LatLng(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
//    }
//}
