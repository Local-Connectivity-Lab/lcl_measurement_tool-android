package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.TypeConverters;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.lcl.lclmeasurementtool.Utils.LocationUtils;

import java.util.List;

@Dao
@TypeConverters({LocationUtils.class})
public interface ConnectivityDAO {

    // CREATE
    @Insert
    public void insert(Connectivity... connectivities);

    // READ
    @Query("SELECT * FROM connectivity_table")
    public LiveData<List<Connectivity>> retrieveAllConnectivities();


    @Query("SELECT * FROM connectivity_table")
    public List<Connectivity> retrieveAllConnectivitiesSynchronous();


    @Query("SELECT * FROM connectivity_table WHERE time_stamp >= :d1 AND time_stamp <= :d2")
    public LiveData<List<Connectivity>> retrieveConnectivitiesBetweenDates(String d1, String d2);

    // UPDATE
    @Update
    public void updateConnectivity(Connectivity... connectivities);

    // DELETE
    @Delete
    public void deleteConnectivity(Connectivity... connectivities);

    @Query("DELETE FROM connectivity_table")
    public void deleteAll();

    @Query("DELETE FROM connectivity_table WHERE time_stamp >= :d1 AND time_stamp <= :d2")
    public void deleteConnectivityStatsBetweenDates(String d1, String d2);
}
