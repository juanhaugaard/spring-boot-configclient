/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by juan.haugaard on 1/27/2017.
 */
class CsvRowImpl implements CsvRow {
    private CsvParser parser;

    public CsvRowImpl(CsvParser parser) {
        if (parser == null)
            throw new IllegalStateException("parser cannot be null");
        this.parser = parser;
    }

    @Override
    public int colIndexByName(String name) {
        return parser.colIndexByName(name);
    }

    @Override
    public boolean columnExists(int index) {
        return parser.columnExists(index);
    }

    @Override
    public boolean columnExists(String name) {
        int index = colIndexByName(name);
        if (index < 0)
            return false;
        else
            return columnExists(index);
    }

    @Override
    public boolean valueExists(int index) {
        return (parser.getValues() != null) && (index >= 0) && (index < parser.getValues().length) && parser
                .getValues()[index].valueExists();
    }

    @Override
    public boolean valueExists(String name) {
        int index = colIndexByName(name);
        if (index < 0)
            return false;
        else
            return valueExists(index);
    }

    @Override
    public String getValue(int index) {
        CsvField csvField = getField(index);
        if (!csvField.valueExists())
            throw new IllegalArgumentException("Value for column #" + index + " does not exist");
        return csvField.getValue();
    }

    @Override
    public String getValue(String name) {
        int index = colIndexByName(name);
        if (index < 0)
            throw new IllegalArgumentException("Column named '" + name + "' does not exist");
        CsvField csvField = getField(index);
        if (!csvField.valueExists())
            throw new IllegalArgumentException("Value for column '" + name + "' does not exist");
        return csvField.getValue();
    }

    @Override
    public CsvField getField(int index) {
        if (parser.getValues() == null)
            throw new IllegalStateException("No current record");
        if ((index < 0) || (index > parser.getValues().length))
            throw new IllegalArgumentException("Column index out of range: " + index);
        return parser.getValues()[index];
    }

    @Override
    public CsvField getField(String name) {
        return getField(colIndexByName(name));
    }

    @Override
    public List<String> colNames() {
        if (parser.getNames() == null)
            throw new IllegalStateException("Column names not initialized");
        return Collections.unmodifiableList(Arrays.asList(parser.getNames()));
    }

    @Override
    public String colName(int index) {
        if (parser.getNames() == null)
            throw new IllegalStateException("Column names not initialized");
        if ((index < 0) || (index > parser.getNames().length))
            throw new IllegalArgumentException("Column index out of range: " + index);
        return parser.getNames()[index];
    }

    @Override
    public List<String> currentValues() {
        return parser.currentValues();
    }

    @Override
    public int rowNumber() {
        return parser.recordCount();
    }
}
