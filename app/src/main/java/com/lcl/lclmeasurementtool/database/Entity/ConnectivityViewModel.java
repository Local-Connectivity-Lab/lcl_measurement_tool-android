package com.lcl.lclmeasurementtool.database.Entity;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.lcl.lclmeasurementtool.database.db.MeasurementResultDatabase;

import java.util.List;

/**
 * The connectivity measurement view model interfacing between database and the view
 */
public class ConnectivityViewModel extends AbstractViewModel<Connectivity> {

    private final MeasurementResultDatabase db;
    public ConnectivityViewModel(@NonNull Application application) {
        super(application);
        db = MeasurementResultDatabase.getInstance(application);
    }

    @Override
    public LiveData<List<Connectivity>> getAll() {
        return db.connectivityDAO().retrieveAllConnectivities();
    }

    @Override
    public void insert(Connectivity data) {
        MeasurementResultDatabase.databaseWriteExecutor.execute(() -> {
            Log.d("Connectivity_ViewModel", "[insert called]");
            db.connectivityDAO().insert(data);
        });
    }
}
