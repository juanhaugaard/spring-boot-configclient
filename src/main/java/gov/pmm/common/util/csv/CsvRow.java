/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

import java.util.List;

/**
 * Created by juan.haugaard on 1/27/2017.
 */
public interface CsvRow {
    int colIndexByName(String name);

    boolean columnExists(int index);

    boolean columnExists(String name);

    boolean valueExists(int index);

    boolean valueExists(String name);

    String getValue(int index);

    String getValue(String name);

    CsvField getField(int index);

    CsvField getField(String name);

    List<String> colNames();

    String colName(int index);

    List<String> currentValues();

    int rowNumber();
}
