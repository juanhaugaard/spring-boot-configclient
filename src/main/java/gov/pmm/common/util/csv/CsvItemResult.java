/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by juan.haugaard on 12/19/2016.
 */
public class CsvItemResult {
    private STATUS status;

    private String description;

    private String csvRecord;

    public CsvItemResult() {
        status = STATUS.OK;
        description = "";
    }

    public CsvItemResult(STATUS status) {
        this.status = status;
        this.csvRecord = this.description = "";
    }

    public CsvItemResult(STATUS status, String description) {
        this.status = status;
        this.description = description;
        this.csvRecord = "";
    }

    public CsvItemResult(STATUS status, String description, String csvRecord) {
        this.status = status;
        this.description = description;
        this.csvRecord = csvRecord;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getDescription() {
        return (description != null) ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCsvRecord() {
        return (csvRecord != null) ? csvRecord : "";
    }

    public void setCsvRecord(String csvRecord) {
        this.csvRecord = csvRecord;
    }

    @JsonIgnore
    public String getCsvRecordSummary() {
        if (csvRecord != null)
            if (csvRecord.length() <= 40)
                return csvRecord;
            else
                return csvRecord.substring(0, 37) + "...";
        else
            return "";
    }

    @JsonIgnore
    public boolean isError() {
        return status != CsvItemResult.STATUS.OK;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(String.format("\"status\": \"%s\"", getStatus()));
        sb.append(String.format(", \"description\":\"%s\"", getDescription()));
        sb.append(String.format(", \"csvRecord\":\"%s\"", getCsvRecord()));
        sb.append('}');
        return sb.toString();
    }

    public enum STATUS {
        UNRECOGNIZED(-1), OK(0), CLIENT_ERROR(1), SYSTEM_ERROR(2);
        private final int value;

        STATUS(int value) {
            this.value = value;
        }
    }
}
