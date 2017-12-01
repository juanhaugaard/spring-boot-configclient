/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package com.example.entities;

import gov.pmm.common.util.csv.CsvImportBean;
import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ScopeGroupImportBean extends CsvImportBean {
    @Override
    protected CsvItemResult processOneRecord(final CsvRow record) throws Exception {
        return new CsvItemResult(CsvItemResult.STATUS.SYSTEM_ERROR, "Not Implemented");
    }
}
