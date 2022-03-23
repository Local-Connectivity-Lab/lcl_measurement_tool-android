package com.lcl.lclmeasurementtool.Database.Entity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public abstract class AbstractViewModel<T> extends AndroidViewModel {
    public AbstractViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract LiveData<List<T>> getAll();
    public abstract List<T> getAllSync();
    public abstract void insert(T data);
}
