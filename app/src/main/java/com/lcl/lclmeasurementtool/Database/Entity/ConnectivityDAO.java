package com.lcl.lclmeasurementtool.Database.Entity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.TypeConverters;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
@TypeConverters({Converters.class})
public interface ConnectivityDAO {

    // CREATE
    @Insert
    public void insert(Connectivity... connectivities);

    // READ
    @Query("SELECT * FROM connectivity_table")
    public List<Connectivity> retrieveAllConnectivities();

    @Query("SELECT * FROM connectivity_table WHERE time_stamp >= :d1 AND time_stamp <= :d2")
    public List<Connectivity> retrieveConnectivitiesBetweenDates(String d1, String d2);

    @RawQuery
    public List<Connectivity> query(SupportSQLiteQuery query);

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
