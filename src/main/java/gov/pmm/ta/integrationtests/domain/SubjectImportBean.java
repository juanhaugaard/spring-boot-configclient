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

/**
 * This class imports Subjects from a CSV file  * <p>
 * Created by juan.haugaard on 11/30/2017.
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubjectImportBean extends AuthorizationImportBase {

    public static final String[] COLUMNS = {"identifier", "type"};
    public static final String[] OPTIONAL_COLS = {};

    /**
     * SubjectImportBean Constructor
     */
    @Autowired
    public SubjectImportBean(@Qualifier("subjectProcessor") AuthorizationProcessor processor) {
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

