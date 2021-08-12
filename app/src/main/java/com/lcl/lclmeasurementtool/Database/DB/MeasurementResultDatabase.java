package com.lcl.lclmeasurementtool.Database.DB;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lcl.lclmeasurementtool.Database.Entity.Connectivity;
import com.lcl.lclmeasurementtool.Database.Entity.ConnectivityDAO;
import com.lcl.lclmeasurementtool.Database.Entity.Converters;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrength;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrengthDAO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Connectivity.class, SignalStrength.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class MeasurementResultDatabase extends RoomDatabase {

    public abstract ConnectivityDAO connectivityDAO();
    public abstract SignalStrengthDAO signalStrengthDAO();

    private static volatile MeasurementResultDatabase instance;
    private static final int NUMBER_OF_THREADS = 5;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MeasurementResultDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (MeasurementResultDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                                    MeasurementResultDatabase.class,
                            "measurement_result_db").addTypeConverter(new Converters()).build();
                }
            }
        }

        return instance;
    }
}
