/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

/**
 * Created by juan.haugaard on 1/27/2017.
 */
class CsvFieldImpl implements CsvField {
    private String name;
    private String value;

    public CsvFieldImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean valueExists() {
        return (value != null) && (value.length() > 0);
    }

    @Override
    public String toString() {
        return "Field{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
    }
}
