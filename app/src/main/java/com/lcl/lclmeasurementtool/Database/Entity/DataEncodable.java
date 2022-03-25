package com.lcl.lclmeasurementtool.Database.Entity;

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
