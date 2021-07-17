package com.lcl.lclmeasurementtool;

import com.lcl.lclmeasurementtool.Utils.ConvertUtils;
import com.lcl.lclmeasurementtool.Utils.DataTransferRateUnit;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ConvertUtilUnitTest {
    @Test
    public void testUnitConversion() {
        double data = 100.0;
        double[] actual = new double[] {
                100, 12.5, 0.1, 0.0125, 0.0001, 0.0000125,          // kb to all other
                800, 100, 0.8, 0.1, 0.0008, 0.0001,                 // kB to all other
                100000, 12500, 100, 12.5, 0.1, 0.0125,              // mb to all other
                800000, 100000, 800, 100, 0.8, 0.1,                 // mB to all other
                100000000, 12500000, 100000, 12500, 100, 12.5,      // gb to all other
                800000000, 100000000, 800000, 100000, 800, 100      // gB to all other
        };

        List<List<DataTransferRateUnit>> res = findAllCombination();

        for (int i = 0; i < res.size(); i++) {
            List<DataTransferRateUnit> list = res.get(i);
            assertEquals(
                    ConvertUtils.convert(list.get(0), list.get(1), data),  actual[i], 0.001
            );
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidData() {
        ConvertUtils.convert(DataTransferRateUnit.Kilobit, DataTransferRateUnit.Megabit, -100);
    }

    // TODO: should also include tests for very large/small numbers

    private List<List<DataTransferRateUnit>> findAllCombination() {
        List<List<DataTransferRateUnit>> res = new ArrayList<>();
        find(0, DataTransferRateUnit.values(), new ArrayList<>(), res);
        return res;
    }

    private void find(int level, DataTransferRateUnit[] arr,
                      List<DataTransferRateUnit> list,
                      List<List<DataTransferRateUnit>> res) {
        // terminator
        if (list.size() == 2) {
            res.add(new ArrayList<>(list));
            return;
        }

        for (int i = level; i < arr.length; i++) {
            // current logic
            list.add(arr[i]);

            // drill down
            find(level, arr, list, res);

            // reset
            list.remove(list.size() - 1);
        }
    }
}