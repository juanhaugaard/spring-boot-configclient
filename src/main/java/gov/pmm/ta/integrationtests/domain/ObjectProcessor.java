/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests.domain;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.internal.JsonContext;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
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
public class ObjectProcessor extends AuthorizationProcessorBase {

    private DocumentContext documentContext;
    private JSONArray objects;

    public ObjectProcessor(
            @Value("${authorization.host}") String host,
            @Value("${objects.path:/api/objects}") String path,
            @Value("${authorization.token}") String token) throws URISyntaxException {
        super(host, path, token);
        log.debug("Constructing {} with url={}{}", getClass().getSimpleName(), host, path);
    }

    @Override
    public AuthorizationImportBase.ACTION selectAction(Map<String, String> items) {
        if ((items == null) || (items.size() != 1))
            throw new IllegalArgumentException("invalid items parameter");
        final String objectId = items.get(column(0));
        final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
        log.trace("selectAction: object={}", objectId);
        if (getIdentifiers().contains(objectId)) {
            ret[0] = AuthorizationImportBase.ACTION.SKIP;
        }
        return ret[0];
    }

    @Override
    public boolean performAdd(Map<String, String> items) {
        return performAdd(getURI(), toJson(items));
    }

    @Override
    public boolean performUpdate(Map<String, String> items) {
        log.debug("{}.performUpdate({})", getClass().getSimpleName(), toJson(items));
        log.error("Objects are not updatable");
        return false;
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
            objects = documentContext.read("$[*]");
        } else {
            log.error("All Objects JSON is empty");
        }
    }

    private DocumentContext getDocumentContext() {
        if (documentContext == null)
            setup();
        return documentContext;
    }

    private JSONArray getIdentifiers() {
        if (objects == null)
            setup();
        return objects;
    }

    private String column(int index) {
        if (index < 0 || index >= ObjectImportBean.COLUMNS.length)
            throw new IllegalArgumentException("column index out of range: " + index);
        return ObjectImportBean.COLUMNS[index];
    }
}
