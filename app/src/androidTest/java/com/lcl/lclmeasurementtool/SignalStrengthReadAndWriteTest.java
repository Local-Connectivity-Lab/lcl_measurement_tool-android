package com.lcl.lclmeasurementtool;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lcl.lclmeasurementtool.Database.db.MeasurementResultDatabase;
import com.lcl.lclmeasurementtool.Database.Entity.SignalStrengthDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class SignalStrengthReadAndWriteTest {

    private MeasurementResultDatabase db;
    private SignalStrengthDAO ssDAO;

    @Before
    public void create() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, MeasurementResultDatabase.class).build();
        ssDAO = db.signalStrengthDAO();
    }

    @After
    public void cleanUp() throws IOException {
        db.close();
    }

    @Test
    public void testSimpleWrite() {
//        ssDAO.insert(new SignalStrength("124",-98, 3));
//        Assert.assertEquals(1, ssDAO.retrieveAllSignalStrengths().size());
    }

    @Test
    public void testDuplicates() {

    }

    @Test
    public void testRead() {

    }
}
