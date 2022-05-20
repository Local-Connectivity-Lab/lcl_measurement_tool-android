package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.TypeConverters;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.lcl.lclmeasurementtool.Utils.LocationUtils;

import java.util.List;

/**
 * A DAO for signal strength
 */
@Dao
@TypeConverters({LocationUtils.class})
public interface SignalStrengthDAO {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insert(SignalStrength... signalStrengths);

    // READ
    @Query("SELECT * FROM signal_strength_table")
    public LiveData<List<SignalStrength>> retrieveAllSignalStrengths();

    @Query("SELECT * FROM signal_strength_table")
    public List<SignalStrength> retrieveAllSignalStrengthsSynchronous();

    @Query("SELECT * FROM signal_strength_table WHERE time_stamp >= :d1 AND time_stamp <= :d2")
    public LiveData<List<SignalStrength>> retrieveSignalStrengthBetweenDates(String d1, String d2);


    // UPDATE

    @Update
    public void updateSignalStrength(SignalStrength... signalStrength);

    // DELETE
    @Delete
    public void deleteSignalStrengths(SignalStrength... signalStrengths);

    @Query("DELETE FROM signal_strength_table")
    public void deleteAll();

    @Query("DELETE FROM signal_strength_table WHERE time_stamp >= :d1 AND time_stamp <= :d2")
    public void deleteSignalStrengthBetweenDates(String d1, String d2);
}
