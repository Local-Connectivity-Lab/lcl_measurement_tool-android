package com.lcl.lclmeasurementtool.Database.Entity;

import android.app.Application;

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

    public void insert(Connectivity data) {
        MeasurementResultDatabase.databaseWriteExecutor.execute(() -> {
            db.connectivityDAO().insert(data);
        });
    }
}
