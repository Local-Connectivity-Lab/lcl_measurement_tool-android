package com.lcl.lclmeasurementtool.Database.Entity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.lcl.lclmeasurementtool.Database.DB.MeasurementResultDatabase;

import java.util.List;

public class SignalViewModel extends AndroidViewModel {

    private final MeasurementResultDatabase db;

    public SignalViewModel(@NonNull Application application) {
        super(application);
        db = MeasurementResultDatabase.getInstance(application);
    }

    public LiveData<List<SignalStrength>> getAllConnectivityResults() {
        return db.signalStrengthDAO().retrieveAllSignalStrengths();
    }

    public void insert(SignalStrength signalStrength) {
        MeasurementResultDatabase.databaseWriteExecutor.execute(() -> {
            db.signalStrengthDAO().insert(signalStrength);
        });
    }

    public void deleteAll() {
        MeasurementResultDatabase.databaseWriteExecutor.execute(() -> {
            db.signalStrengthDAO().deleteAll();
        });
    }
}
