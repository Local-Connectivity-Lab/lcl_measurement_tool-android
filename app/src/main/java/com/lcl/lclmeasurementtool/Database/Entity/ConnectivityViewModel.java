package com.lcl.lclmeasurementtool.Database.Entity;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.lcl.lclmeasurementtool.Database.DB.MeasurementResultDatabase;

import java.util.List;

public class ConnectivityViewModel extends AbstractViewModel<Connectivity> {

    private final MeasurementResultDatabase db;
    public ConnectivityViewModel(@NonNull Application application) {
        super(application);
        db = MeasurementResultDatabase.getInstance(application);
    }

    public LiveData<List<Connectivity>> getAll() {
        return db.connectivityDAO().retrieveAllConnectivities();
    }

    @Override
    public List<Connectivity> getAllSync() {
        return db.connectivityDAO().retrieveAllConnectivitiesSynchronous();
    }

    public void insert(Connectivity data) {
        MeasurementResultDatabase.databaseWriteExecutor.execute(() -> {
            Log.d("Connectivity_ViewModel", "[insert called]");
            db.connectivityDAO().insert(data);
        });
    }
}
