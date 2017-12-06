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
import org.springframework.util.comparator.NullSafeComparator;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrivilegeProcessor extends AuthorizationProcessorBase {

    private DocumentContext documentContext;
    private JSONArray identifiers;
    private Comparator<Object> comparator;

    @Autowired
    public PrivilegeProcessor(
            @Value("${authorization.host}") String host,
            @Value("${privileges.path:/api/privileges}") String path,
            @Value("${authorization.token}") String token) throws URISyntaxException {
        super(host, path, token);
        comparator = (Comparator<Object>) NullSafeComparator.NULLS_HIGH;
        log.debug("Constructing {} with url={}{}", getClass().getSimpleName(), host, path);
    }

    public static String toJson(Map<String, String> items) {
        final String idKey = "id";
        if (items == null || items.size() == 0)
            throw new IllegalArgumentException("there are no items to build a Privilege JSON");
        String idValue = items.get(idKey);
        if (StringUtils.isEmpty(idValue))
            throw new IllegalArgumentException("Privilege '" + idKey + "' an not be null");
        items.remove(idKey);
        final List<String> json = new ArrayList<>();
        items.forEach((key, value) -> json.add(String.format("\"%s\":\"%s\"", key, value)));
        return "{\"" + idKey + "\":" + idValue + "," + String.join(",", json) + "}";
    }

    @Override
    public AuthorizationImportBase.ACTION selectAction(Map<String, String> items) {
        if ((items == null) || ((items.size() != 3) && (items.size() != 4)))
            throw new IllegalArgumentException("invalid items parameters");
        final String privilegeId = items.get(column(0));
        final String actionId = items.get(column(1));
        final String objectId = items.get(column(2));
        final String systemId = items.get(column(3));
        final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
        log.trace("selectAction: privilege={}, {}, {}, {}", privilegeId, actionId, objectId, systemId);
        if (getIdentifiers().contains(privilegeId)) {
            String filter = String.format("$[?(@.name=='%s')]", privilegeId);
            JSONArray resultArray = getDocumentContext().read(filter);
            if (resultArray != null && resultArray.size() == 1) {
                Map<String, Object> result = (Map<String, Object>) resultArray.get(0);
                log.info("Found result for '{}':{}", privilegeId, result);
                if ((comparator.compare(actionId, result.get(column(1))) == 0)
                        && (comparator.compare(objectId, result.get(column(2))) == 0)
                        && (comparator.compare(systemId, result.get(column(3))) == 0)) {
                    ret[0] = AuthorizationImportBase.ACTION.SKIP;
                } else {
                    items.put("id", result.get("id").toString());
                    ret[0] = AuthorizationImportBase.ACTION.UPDATE;
                }
            }
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
            identifiers = documentContext.read("$[*]['name']");
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
        if (index < 0 || index >= PrivilegeImportBean.COLUMNS.length)
            throw new IllegalArgumentException("column index out of range: " + index);
        return PrivilegeImportBean.COLUMNS[index];
    }
}
