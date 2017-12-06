/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DelegationImportBean extends AuthorizationImportBase {

    public static final String[] COLUMNS = {
            "delegatee", "privileges",
            "scopes", "scopegroups",
            "effdate", "expdate"
    };

    public static final String[] OPTIONAL_COLS = {COLUMNS[2], COLUMNS[3], COLUMNS[4], COLUMNS[5]};

    /**
     * DelegationImportBean Constructor
     */
    @Autowired
    public DelegationImportBean(@Qualifier("delegationProcessor") AuthorizationProcessor processor) {
        super(processor);
        log.debug("{} constructed with {}",
                getClass().getSimpleName(),
                processor.getClass().getSimpleName());
    }

    public String[] getColumns() {
        return COLUMNS;
    }

    public String[] getOptionalColumns() {
        return OPTIONAL_COLS;
    }
}

