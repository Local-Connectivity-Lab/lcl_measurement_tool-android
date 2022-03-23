package com.lcl.lclmeasurementtool.Database.Entity;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.lcl.lclmeasurementtool.Database.DB.MeasurementResultDatabase;

import java.util.List;

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
    public List<SignalStrength> getAllSync() {
        return db.signalStrengthDAO().retrieveAllSignalStrengthsSynchronous();
    }

    public void insert(SignalStrength data) {
        MeasurementResultDatabase.databaseWriteExecutor.execute(() -> {
            Log.d("Signal_ViewModel", "[insert called]");
            db.signalStrengthDAO().insert(data);
        });
    }
}
