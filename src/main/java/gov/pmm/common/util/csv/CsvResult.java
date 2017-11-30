/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util.csv;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by juan.haugaard on 12/19/2016.
 */
public class CsvResult {
    private List<CsvItemResult> items;
    private int errorCount;

    public CsvResult() {
        init();
    }

    private void init() {
        items = new LinkedList<CsvItemResult>();
        errorCount = 0;
    }

    public int getTotalCount() {
        return this.items.size();
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setItems(Collection<CsvItemResult> items) {
        init();
        this.items.addAll(items);
        errorCount = (int) this.items.stream().filter(item -> item.isError()).count();
    }

    public void addItem(CsvItemResult item) {
        this.items.add(item);
        if (item.isError())
            errorCount += 1;
    }

    public Collection<CsvItemResult> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("items=").append(items);
        sb.append(", errorCount=").append(errorCount);
        sb.append('}');
        return sb.toString();
    }
}
