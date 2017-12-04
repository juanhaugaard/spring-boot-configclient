/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.authorization;

import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrivilegeImportBean extends AuthorizationImportBase {

    public static final String[] COLUMNS = {"name", "action", "object", "system"};

    /**
     * PrivilegeImportBean Constructor
     */
    @Autowired
    public PrivilegeImportBean(@Qualifier("privilegeProcessor") AuthorizationProcessor processor) {
        super(processor);
        log.debug("{} constructed with {}",
                getClass().getSimpleName(),
                processor.getClass().getSimpleName());
    }

    public String[] getColumns() {
        return COLUMNS;
    }

    protected CsvItemResult processOneRecord(final CsvRow record) {
        CsvItemResult itemResult = new CsvItemResult();

        // here we process one CSV record to produce one Authorization entity
        try {
            final StringBuilder description = new StringBuilder();
            long missingCnt = 0;
            missingCnt += record.valueExists(getColumns()[0])?0:1;
            missingCnt += record.valueExists(getColumns()[1])?0:1;
            missingCnt += record.valueExists(getColumns()[2])?0:1;
            if (missingCnt > 0) {
                description.append("Failed on # ").append(record.rowNumber());
                description.append(record.valueExists(getColumns()[0])?"":", missing "+getColumns()[0]);
                description.append(record.valueExists(getColumns()[1])?"":", missing "+getColumns()[1]);
                description.append(record.valueExists(getColumns()[2])?"":", missing "+getColumns()[2]);
                itemResult.setStatus(CsvItemResult.STATUS.CLIENT_ERROR);
                itemResult.setDescription(description.toString());
            } else {
                final List<String> entity = new ArrayList<>();
                Arrays.stream(getColumns()).forEach(field -> {if (record.valueExists(field)) entity.add(record.getValue(field).trim());});
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
}

