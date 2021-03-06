/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests.domain;

import gov.pmm.common.util.Pair;
import gov.pmm.common.util.csv.CsvImportBean;
import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvRow;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class imports Subjects from a CSV file  * <p>
 * Created by juan.haugaard on 11/30/2017.
 */
@Slf4j
public abstract class AuthorizationImportBase extends CsvImportBean {

    private final AuthorizationProcessor processor;

    public AuthorizationImportBase(AuthorizationProcessor processor) {
        this.processor = processor;
    }

    public abstract String[] getColumns();

    public abstract String[] getOptionalColumns();

    public AuthorizationProcessor getProcessor() {
        return processor;
    }

    protected List<String> optionalColumns() {
        return Arrays.asList(getOptionalColumns());
    }

    protected boolean validateOneRecord(final CsvRow record, final CsvItemResult itemResult) {
        boolean ret = true;
        final StringBuilder description = new StringBuilder();
        long missingCnt = Arrays.stream(getColumns())
                .filter(column -> !optionalColumns().contains(column))
                .filter(column -> !record.valueExists(column))
                .count();
        if (missingCnt > 0) {
            description.append("Failed on # ").append(record.rowNumber());
            Arrays.stream(getColumns())
                    .filter(column -> !optionalColumns().contains(column))
                    .filter(column -> !record.valueExists(column))
                    .forEach(column -> description.append(", missing ").append(column));
            itemResult.setStatus(CsvItemResult.STATUS.CLIENT_ERROR);
            itemResult.setDescription(description.toString());
            ret = false;
        }
        return ret;
    }

    protected CsvItemResult processOneRecord(final CsvRow record) {
        CsvItemResult itemResult = new CsvItemResult();

        // here we process one CSV record to produce one Authorization entity
        try {
            if (validateOneRecord(record, itemResult)) {
                final StringBuilder description = new StringBuilder();
                final Map<String, String> entity = new HashMap<>();
                Arrays.stream(getColumns())
                        .filter(record::valueExists)
                        .map(column -> Pair.of(column, record.getValue(column).trim()))
                        .forEach(pair -> entity.put(pair.getFirst(), pair.getSecond()));
                switch (getProcessor().selectAction(entity)) {
                    case ADD: // item not found, insert it
                        if (getProcessor().performAdd(entity)) {
                            description.append("Added");
                        } else {
                            description.append("Failed to add");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                    case UPDATE: // item found, update it
                        if (getProcessor().performUpdate(entity)) {
                            description.append("Updated");
                        } else {
                            description.append("Failed to update");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                    case DELETE: // item found, delete it
                        if (getProcessor().performDelete(entity)) {
                            description.append("Deleted");
                        } else {
                            description.append("Failed to delete");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                    case SKIP: // item identical, skip it
                        if (getProcessor().performSkip(entity)) {
                            description.append("Skipped");
                        } else {
                            description.append("Failed to skip");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                }
                entity.forEach((key, value) -> description.append(", ").append(key).append("=").append(value));
                itemResult.setDescription(description.toString());
            }
            itemResult.setCsvRecord(String.join(",", record.currentValues()));
        } catch (Exception e) {
            String description = e.getMessage();
            log.error("---> FAILURE record #{}: {}", record.rowNumber(), description);
            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
            itemResult.setDescription("Failure for record #" + record.rowNumber());
        }
        return itemResult;
    }

    public enum ACTION {ADD, UPDATE, DELETE, SKIP}

    public interface AuthorizationProcessor {
        ACTION selectAction(final Map<String, String> items);

        default boolean performAdd(final Map<String, String> items) {
            // do nothing
            return false;
        }

        default boolean performUpdate(final Map<String, String> items) {
            // do nothing
            return false;
        }

        default boolean performDelete(final Map<String, String> items) {
            // do nothing
            return false;
        }

        default boolean performSkip(final Map<String, String> items) {
            // do nothing
            log.trace("{}.performSkip({})", getClass().getSimpleName(), String.join(",", items.values()));
            return true;
        }
    }
}
