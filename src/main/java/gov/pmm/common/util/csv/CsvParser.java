/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by juan.haugaard on 1/23/2017.
 */
@Slf4j
public class CsvParser implements Iterable<CsvRow> {
    private Reader reader;
    private String[] names;
    private CsvField[] values;
    private int lineNumber;
    private boolean eof;
    private CsvRow currentRow;
    private CsvRowIterator rowIterator;

    public CsvParser(InputStream stream) {
        this(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public CsvParser(Reader reader) {
        init(reader);
    }

    private void init(Reader reader) {
        if (reader == null) throw new IllegalArgumentException("Reader should not be null");
        this.reader = reader;
        lineNumber = -1;
        eof = false;
        values = null;
        names = null;
        readNames();
        currentRow = new CsvRowImpl(this);
        rowIterator = new CsvRowIterator(this);
        log.trace("{} initialized", this.getClass().getSimpleName());
    }

    public boolean isEof() {
        return eof;
    }

    String[] getNames() {
        return names;
    }

    CsvField[] getValues() {
        return values;
    }

    public int columnCount() {
        if (names == null) {
            log.warn("Column names not initialized");
            return 0;
        }
        return names.length;
    }

    public int colIndexByName(String name) {
        if ((names != null) && (name != null)) {
            for (int i = 0; i < names.length; i++) {
                if (name.equals(names[i]))
                    return i;
            }
        } else {
            if (names == null)
                log.warn("Column names not initialized");
            if (name == null)
                log.warn("Column name can not be null");
        }
        return -1;
    }

    public boolean columnExists(int index) {
        return (names != null) && (index >= 0) && (index < names.length) && !StringUtils.isEmpty(names[index]);
    }

    public boolean columnExists(String name) {
        int index = colIndexByName(name);
        if (index < 0)
            return false;
        else
            return columnExists(index);
    }

    public CsvRow currentRow() {
        return currentRow;
    }

    List<CsvField> currentRecord() {
        if (values == null)
            throw new IllegalStateException("No current record");
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    List<String> currentValues() {
        List<CsvField> csvFields = currentRecord();
        List<String> ret = new ArrayList<String>(csvFields.size());
        for (CsvField csvField : csvFields)
            ret.add(csvField.getValue());
        return Collections.unmodifiableList(ret);
    }

    public int recordCount() {
        return lineNumber;
    }

    private void readNames() {
        log.trace("{}.readNames invoked", this.getClass().getSimpleName());
        try {
            List<String> first = parseLine(reader);
            if (first != null) {
                names = first.toArray(new String[first.size()]);
                log.trace("{}.readNames successfully read header, {} columns", getClass().getSimpleName(), names.length);
            } else {
                eof = true;
                log.error("{}.readNames failed to read header ", this.getClass().getSimpleName());
            }
        } catch (IOException e) {
            eof = true;
            log.error("Failed to read headers: {}", e.getMessage(), e);
        }
    }

    public List<CsvField> nextRecord() {
        log.trace("{}.nextRecord invoked", this.getClass().getSimpleName());
        try {
            List<String> items = null;
            do {
                if (eof || (items = parseLine(reader)) == null) {
                    // end of file reached
                    eof = true;
                    log.trace("{}.nextRecord EOF", this.getClass().getSimpleName());
                    return Collections.EMPTY_LIST;
                } else if (!isBlankRecord(items)) {
                    // valid record, create fields
                    values = new CsvField[Math.min(items.size(), names.length)];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = new CsvFieldImpl(names[i], items.get(i));
                    }
                    log.trace("{}.nextRecord Success", this.getClass().getSimpleName());
                    return currentRecord();
                } else {
                    // blank record, read next line
                    continue;
                }
            } while (true);
        } catch (IOException e) {
            log.trace("{}.nextRecord EOF, {}", this.getClass().getSimpleName(), e.getMessage());
            eof = true;
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * A blank record contains only one field and that field is an empty string
     *
     * @param items list of fields strings
     * @return true iff only one empty field
     */
    private boolean isBlankRecord(List<String> items) {
        if ((items == null) || (items.size() < 1))
            return true;
        String firstItem = items.get(0);
        if (firstItem == null)
            return true;
        if (columnCount() == 1)
            return false;
        if (columnCount() > 1)
            return (items.size() == 1) && (firstItem.length() == 0);
        return false;
    }

    /**
     * Returns a null when the input stream is empty
     */
    private List<String> parseLine(Reader r) throws IOException {
        log.trace("{}.parseLine invoked", this.getClass().getSimpleName());
        int ch = r.read();
        while ((ch == '\r') || (ch == '\n')) {
            ch = r.read();
        }
        if (ch < 0) {
            log.trace("{}.parseLine Stream EOF reached", this.getClass().getSimpleName());
            return null;
        }
        List<String> store = new LinkedList<>();
        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean started = false;
        while (ch >= 0) {
            if (inQuotes) {
                started = true;
                if (ch == '"') {
                    inQuotes = false;
                } else {
                    curVal.append((char) ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                    if (started) {
                        // if this is the second quote in a value, add a quote
                        // this is for the double quote in the middle of a value
                        curVal.append('"');
                    }
                } else if (ch == ',') {
                    store.add(curVal.toString());
                    curVal = new StringBuffer();
                    started = false;
                } else if (ch == '\r') {
                    //ignore LF characters
                } else if (ch == '\n') {
                    //end of a line, break out
                    break;
                } else {
                    curVal.append((char) ch);
                }
            }
            ch = r.read();
        }
        store.add(curVal.toString());
        lineNumber += 1;
        log.trace("{}.parseLine returning, line: {}", this.getClass().getSimpleName(), lineNumber);
        return store;
    }

    public Iterator<CsvRow> iterator() {
        return rowIterator;
    }

    private class CsvRowIterator implements Iterator<CsvRow> {
        private CsvParser parser;
        private Boolean hasNext;

        CsvRowIterator(CsvParser parser) {
            this.parser = parser;
            hasNext = null;
        }

        @Override
        public boolean hasNext() {
            if (hasNext == null) {
                if (parser.isEof())
                    hasNext = Boolean.FALSE;
                else {
                    parser.nextRecord();
                    hasNext = !parser.isEof();
                }
            }
            return hasNext.booleanValue();
        }

        @Override
        public CsvRow next() {
            if (!hasNext())
                throw new IllegalStateException("There is no next row");
            hasNext = null;
            return parser.currentRow();
        }
    }
}
