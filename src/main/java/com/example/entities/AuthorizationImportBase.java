package com.example.entities;

import gov.pmm.common.util.csv.CsvImportBean;
import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvRow;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    protected CsvItemResult processOneRecord(final CsvRow record) {
        CsvItemResult itemResult = new CsvItemResult();

        // here we process one CSV record to produce one Authorization entity
        try {
            final StringBuilder description = new StringBuilder();
            long missingCnt = Arrays.stream(getColumns()).filter(column -> !record.valueExists(column)).count();
            if (missingCnt > 0) {
                description.append("Failed on # ").append(record.rowNumber());
                Arrays.stream(getColumns())
                        .filter(column -> !record.valueExists(column))
                        .forEach(field -> description.append(", missing ").append(field));
                itemResult.setStatus(CsvItemResult.STATUS.CLIENT_ERROR);
                itemResult.setDescription(description.toString());
            } else {
                final List<String> entity = new ArrayList<>();
                Arrays.stream(getColumns()).forEach(field -> entity.add(record.getValue(field).trim()));
                switch (processor.selectAction(entity)) {
                    case ADD: // item not found, insert it
                        if (processor.performAdd(entity)) {
                            description.append("Added");
                        } else {
                            description.append("Failed to add");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                    case UPDATE: // item found, update it
                        if (processor.performUpdate(entity)) {
                            description.append("Updated");
                        } else {
                            description.append("Failed to update");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                    case DELETE: // item found, delete it
                        if (processor.performDelete(entity)) {
                            description.append("Deleted");
                        } else {
                            description.append("Failed to delete");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                    case SKIP: // item identical, skip it
                        if (processor.performSkip(entity)) {
                            description.append("Skipped");
                        } else {
                            description.append("Failed to skip");
                            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
                        }
                        break;
                }
                entity.stream().forEach(item -> description.append(" - ").append(item));
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
        ACTION selectAction(final List<String> items);

        default boolean performAdd(final List<String> items) {
            // do nothing
            return false;
        }

        default boolean performUpdate(final List<String> items) {
            // do nothing
            return false;
        }

        default boolean performDelete(final List<String> items) {
            // do nothing
            return false;
        }

        default boolean performSkip(final List<String> items) {
            // do nothing
            return true;
        }
    }
}
