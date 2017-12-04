/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.authorization;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrivilegeProcessor extends AuthorizationProcessorBase {

    private DocumentContext documentContext;
    private JSONArray identifiers;

    @Autowired
    public PrivilegeProcessor(
            @Value("${authorization.host}") String host,
            @Value("${privileges.path:/api/privileges}") String path,
            @Value("${authorization.token}") String token) throws URISyntaxException {
        super(host,path,token);
        log.debug("Constructing {} with url={}{}", getClass().getSimpleName(), host, path);
    }

    @Override
    public AuthorizationImportBase.ACTION selectAction(List<String> items) {
        if ((items == null) || !((items.size() != 3)||(items.size() != 4)))
            throw new IllegalArgumentException("invalid items parameters");
        final String privilegeId = items.get(0);
        final String actionId = items.get(1);
        final String objectId = items.get(2);
        final String systemId = (items.size()==4)?items.get(3):null;
        final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
        log.trace("selectAction: privilege={}, {}, {}, {}", privilegeId, actionId, objectId, systemId);
//        if (getIdentifiers().contains(privilegeId)) {
//            String filter = String.format("$[?(@.identifier=='%s')]", subjectId);
//            JSONArray result = getDocumentContext().read(filter);
//            result.stream()
//                    .map(o -> (Map<String, String>) o)
//                    .filter(map -> subjectId.equals(map.get("identifier")))
//                    .forEach(map -> {
//                        if (subjectType.equals(map.get("type"))) {
//                            ret[0] = AuthorizationImportBase.ACTION.SKIP;
//                        } else {
//                            ret[0] = AuthorizationImportBase.ACTION.UPDATE;
//                        }
//                    });
//        }
        return ret[0];
    }

    @Override
    public boolean performAdd(List<String> items) {
        String body = String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}",
                SubjectImportBean.COLUMNS[0], items.get(0),
                SubjectImportBean.COLUMNS[1], items.get(1));
        return performAdd(getURI(), body);
    }

    @Override
    public boolean performUpdate(List<String> items) {
        String body = String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}",
                SubjectImportBean.COLUMNS[0], items.get(0),
                SubjectImportBean.COLUMNS[1], items.get(1));
        return performUpdate(getURI(), body);
    }

    @Override
    public boolean performDelete(List<String> items) {
        StringBuilder url = new StringBuilder(getURI().getScheme());
        url.append("://").append(getURI().getHost());
        url.append(":").append(getURI().getPort()).append(getURI().getPath());
        url.append("/").append(items.get(0));
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
}
