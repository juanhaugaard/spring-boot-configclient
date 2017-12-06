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
public class RoleImportBean extends AuthorizationImportBase {

    public static final String[] COLUMNS = {"name", "privileges", "delegableprivileges"};

    public static final String[] OPTIONAL_COLS = {COLUMNS[1], COLUMNS[2]};

    /**
     * RoleImportBean Constructor
     */
    @Autowired
    public RoleImportBean(@Qualifier("roleProcessor") AuthorizationProcessor processor) {
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

