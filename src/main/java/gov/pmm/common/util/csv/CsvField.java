/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

/**
 * Created by juan.haugaard on 1/27/2017.
 */
public interface CsvField {
    String getName();

    void setName(String name);

    String getValue();

    void setValue(String value);

    boolean valueExists();
}
