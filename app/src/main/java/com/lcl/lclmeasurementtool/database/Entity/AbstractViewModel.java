package com.lcl.lclmeasurementtool.database.Entity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * An abstract viewmodel for data entity
 * @param <T> the type of the data entity stored in the database that will be retrieved
 */
public abstract class AbstractViewModel<T> extends AndroidViewModel {
    public AbstractViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Retrieve all data in a form of live data from the database
     * @return the live data of all data in the database for the given entity
     */
    public abstract LiveData<List<T>> getAll();

    /**
     * Insert a row of data into the database
     * @param data the data to be inserted
     */
    public abstract void insert(T data);
}
