package com.lcl.lclmeasurementtool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.output.JsonStream;
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel;

import org.junit.Test;


public class ModelSerializationTest {


    @Test
    public void testSignalStrengthSerialization() {
        SignalStrengthReportModel model = new SignalStrengthReportModel("device", 123.123, 456.456, "time", "cellID", 1, 1);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String res = mapper.writeValueAsString(model);
            System.out.println(res);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
