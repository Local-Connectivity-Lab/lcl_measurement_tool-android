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
        assertEquals(
                ConvertUtils.convert(DataTransferRateUnit.Kilobit, DataTransferRateUnit.Kilobyte, 1000),
                125, 0.1
        );

        assertEquals(
                ConvertUtils.convert(DataTransferRateUnit.Kilobit, DataTransferRateUnit.Megabit, 1000),
                1, 0.1
        );

        assertEquals(
                ConvertUtils.convert(DataTransferRateUnit.Kilobit, DataTransferRateUnit.Megabit, 1000),
                1, 0.1
        );

        List<List<DataTransferRateUnit>> res = findAllCombination();

        for (List<DataTransferRateUnit> list : res) {
            System.out.println(list);
        }

    }

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