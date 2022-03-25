package com.lcl.lclmeasurementtool.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A utility class that provides functionalities to serialize object
 */
public class SerializationUtils {

    /**
     * Serialize object to an array of bytes.
     *
     * @param o object to be serialized
     * @return  a byte array representing the serialized object
     * @throws JsonProcessingException if the serialization process failed
     */
    public static byte[] serializeToBytes(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(o);
    }
}
