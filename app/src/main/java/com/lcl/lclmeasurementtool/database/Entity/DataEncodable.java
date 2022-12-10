package com.lcl.lclmeasurementtool.database.Entity;

/**
 * Data encodable interface
 */
public interface DataEncodable {

    /**
     * Transform the data to a string array
     * @return array of strings of the data in the entity
     */
    String[] toArray();
}
