package com.lcl.lclmeasurementtool.Database.Entity;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.lcl.lclmeasurementtool.Database.db.MeasurementResultDatabase;

import java.util.List;

/**
 * The signal view model interfacing between database and the view
 */
public class SignalViewModel extends AbstractViewModel<SignalStrength> {

    private final MeasurementResultDatabase db;

    public SignalViewModel(@NonNull Application application) {
        super(application);
        db = MeasurementResultDatabase.getInstance(application);
    }

    @Override
    public LiveData<List<SignalStrength>> getAll() {
        return db.signalStrengthDAO().retrieveAllSignalStrengths();
    }

    @Override
    public void insert(SignalStrength data) {
        MeasurementResultDatabase.databaseWriteExecutor.execute(() -> {
            Log.d("Signal_ViewModel", "[insert called]");
            db.signalStrengthDAO().insert(data);
        });
    }
}
