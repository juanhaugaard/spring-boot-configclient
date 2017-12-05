/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ObjectImportBean extends AuthorizationImportBase {

    public static final String[] COLUMNS = {"object"};

    public static final String[] OPTIONAL_COLS = {};

    /**
     * ObjectImportBean Constructor
     */
    @Autowired
    public ObjectImportBean(@Qualifier("objectProcessor") AuthorizationProcessor processor) {
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

