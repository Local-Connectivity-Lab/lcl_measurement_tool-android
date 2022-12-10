package com.lcl.lclmeasurementtool.database.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lcl.lclmeasurementtool.database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.database.Entity.ConnectivityDAO;
import com.lcl.lclmeasurementtool.database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.database.Entity.SignalStrengthDAO;
import com.lcl.lclmeasurementtool.Utils.LocationUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class holding the reference to the Room database
 */
@Database(entities = {Connectivity.class, SignalStrength.class}, version = 1)
@TypeConverters({LocationUtils.class})
public abstract class MeasurementResultDatabase extends RoomDatabase {

    public abstract ConnectivityDAO connectivityDAO();
    public abstract SignalStrengthDAO signalStrengthDAO();

    private static volatile MeasurementResultDatabase instance;
    private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Retrieve the instance of the database
     * @param context the context of the application
     * @return the database reference
     */
    public static MeasurementResultDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (MeasurementResultDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                                    MeasurementResultDatabase.class,
                            "measurement_result_db").build();
                }
            }
        }

        return instance;
    }
}
