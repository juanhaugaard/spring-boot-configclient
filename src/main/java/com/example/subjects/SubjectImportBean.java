/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package com.example.subjects;

import gov.pmm.common.util.csv.CsvImportBean;
import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * This class imports Subjects from a CSV file  * <p>
 * Created by juan.haugaard on 11/30/2017.
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubjectImportBean extends CsvImportBean {
    public static final String COL1="identifier";
    public static final String COL2="type";
    /**
     * SubjectImportBean Constructor
     */
    @Autowired
    public SubjectImportBean() {
        log.debug("{} constructed", getClass().getSimpleName());
    }

    protected CsvItemResult processOneRecord(CsvRow record) {
        Timestamp s = new Timestamp(System.currentTimeMillis());
        CsvItemResult itemResult = new CsvItemResult();

        // here we process one CSV record to produce one Subject
        try {
            if (!record.valueExists(COL1) || !record.valueExists(COL2)) {
                String description = "Failed on # " + record.rowNumber();
                itemResult.setStatus(CsvItemResult.STATUS.CLIENT_ERROR);
                if (!record.valueExists(COL1))
                    description += ", missing "+COL1;
                if (!record.valueExists(COL2))
                    description += ", missing "+COL2;
                itemResult.setDescription(description);
            } else {
                Subject user = new Subject(record.getValue(COL1), record.getValue(COL2));
                log.trace("---> Adding: {}", user);
                if (restCallToAuthorization(user)) {
                    itemResult.setDescription("Processed " + user.getType() + ": " + user.getIdentifier());
                } else {
                    itemResult.setStatus(CsvItemResult.STATUS.CLIENT_ERROR);
                    itemResult.setDescription("Failed for " + user.getType() + ": " + user.getIdentifier());
                }
            }
            itemResult.setCsvRecord(String.join(",", record.currentValues()));
        } catch (Exception e) {
            String description = e.getMessage();
            log.error("---> FAILURE: {}", description);
            itemResult.setStatus(CsvItemResult.STATUS.SYSTEM_ERROR);
            itemResult.setDescription("Failure for record #" + record.rowNumber());
        }
        return itemResult;
    }

    private boolean restCallToAuthorization(Subject user) {
        // here we should use a RestTemplate to call Authorization for this user
        return true;
    }
}
