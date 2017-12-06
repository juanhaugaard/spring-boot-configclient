/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests.domain;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.internal.JsonContext;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubjectProcessor extends AuthorizationProcessorBase {

    private DocumentContext documentContext;
    private JSONArray identifiers;

    @Autowired
    public SubjectProcessor(
            @Value("${authorization.host}") String host,
            @Value("${subjects.path:/api/subjects}") String path,
            @Value("${authorization.token}") String token) throws URISyntaxException {
        super(host, path, token);
        log.debug("Constructing {} with url={}{}", getClass().getSimpleName(), host, path);
    }

    @Override
    public AuthorizationImportBase.ACTION selectAction(Map<String, String> items) {
        if ((items == null) || (items.size() != 2))
            throw new IllegalArgumentException("invalid items parameter");
        final String subjectId = items.get(column(0));
        final String subjectType = items.get(column(1));
        final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
        log.trace("selectAction: subject={}, {}", subjectId, subjectType);
        if (getIdentifiers().contains(subjectId)) {
            String filter = String.format("$[?(@.identifier=='%s')]", subjectId);
            JSONArray result = getDocumentContext().read(filter);
            result.stream()
                    .map(o -> (Map<String, String>) o)
                    .filter(map -> subjectId.equals(map.get("identifier")))
                    .forEach(map -> {
                        if (subjectType.equals(map.get("type"))) {
                            ret[0] = AuthorizationImportBase.ACTION.SKIP;
                        } else {
                            ret[0] = AuthorizationImportBase.ACTION.UPDATE;
                        }
                    });
        }
        return ret[0];
    }

    @Override
    public boolean performAdd(Map<String, String> items) {
        return performAdd(getURI(), toJson(items));
    }

    @Override
    public boolean performUpdate(Map<String, String> items) {
        return performUpdate(getURI(), toJson(items));
    }

    @Override
    public boolean performDelete(Map<String, String> items) {
        StringBuilder url = new StringBuilder(getURI().getScheme());
        url.append("://").append(getURI().getHost());
        url.append(":").append(getURI().getPort()).append(getURI().getPath());
        url.append("/").append(items.get(column(0)));
        return performDelete(url.toString(), Optional.empty());
    }

    private void setup() {
        log.trace("setup");
        String json = getAll(getURI());
        if (!StringUtils.isEmpty(json)) {
            JsonContext jsonContext = new JsonContext();
            documentContext = jsonContext.parse(json);
            identifiers = documentContext.read("$[*]['identifier']");
        } else {
            log.error("All Subjects JSON is empty");
        }
    }

    private DocumentContext getDocumentContext() {
        if (documentContext == null)
            setup();
        return documentContext;
    }

    private JSONArray getIdentifiers() {
        if (identifiers == null)
            setup();
        return identifiers;
    }

    private String column(int index) {
        if (index < 0 || index >= SubjectImportBean.COLUMNS.length)
            throw new IllegalArgumentException("column index out of range: " + index);
        return SubjectImportBean.COLUMNS[index];
    }
}
