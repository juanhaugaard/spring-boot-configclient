/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public abstract class CsvImportBean {

    public CsvResult readInputStream(InputStream stream) throws IOException {
        if (stream == null) throw new IllegalArgumentException("InputStream should not be null");
        CsvParser parser = new CsvParser(stream);
        return readFile(parser);
    }

    public CsvResult readFile(String csvText) throws IOException {
        if (csvText == null) throw new IllegalArgumentException("csvText should not be null");
        InputStream stream = new ByteArrayInputStream(csvText.getBytes());
        return readInputStream(stream);
    }

    public void readInputStream(InputStream stream, CsvItemResultListener listener) {
        if (stream == null) throw new IllegalArgumentException("InputStream should not be null");
        CsvParser parser = new CsvParser(stream);
        readStream(parser, listener);
    }

    public void readStream(CsvParser parser, CsvItemResultListener listener) {
        if (parser == null) throw new IllegalArgumentException("CsvParser should not be null");
        if (parser == listener) throw new IllegalArgumentException("CsvItemResultListener should not be null");
        log.trace("{}.readStream invoked", this.getClass().getSimpleName());
        try {
            for (CsvRow row : parser) {
                CsvItemResult itemResult = processOneRecord(row);
                if (listener != null)
                    listener.onNext(itemResult);
            }
            if (listener != null)
                listener.onCompleted();
        } catch (Exception e) {
            if (listener != null)
                listener.onError(e);
        }
    }

    public CsvResult readFile(CsvParser parser) throws IOException {
        if (parser == null) throw new IllegalArgumentException("CsvParser should not be null");
        log.trace("{}.readFile invoked", this.getClass().getSimpleName());
        CsvResult ret = new CsvResult();
        try {
            for (CsvRow row : parser) {
                CsvItemResult itemResult = processOneRecord(parser.currentRow());
                ret.addItem(itemResult);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        return ret;
    }

    protected abstract CsvItemResult processOneRecord(CsvRow record) throws Exception;
}
