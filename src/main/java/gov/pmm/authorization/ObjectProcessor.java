package gov.pmm.authorization;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.internal.JsonContext;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ObjectProcessor extends AuthorizationProcessorBase {

    private RestTemplate rest;
    private URI uri;
    private String token;
    private DocumentContext documentContext;
    private JSONArray objects;

    public ObjectProcessor(
            @Value("${authorization.host}") String host,
            @Value("${objects.path:/api/objects}") String path,
            @Value("${authorization.token}") String token) throws URISyntaxException {
        super(host, path, token);
        log.debug("Constructing {} with url={}{}", getClass().getSimpleName(), host, path);
        this.rest = new RestTemplate();
        this.token = token;
        setURI(host + path);
    }

    @Override
    public AuthorizationImportBase.ACTION selectAction(List<String> items) {
        if ((items == null) || (items.size() != 1))
            throw new IllegalArgumentException("invalid items parameter");
        final String objectId = items.get(0);
        final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
        log.trace("selectAction: object={}", objectId);
        if (getIdentifiers().contains(objectId)) {
            ret[0] = AuthorizationImportBase.ACTION.SKIP;
        }
        return ret[0];
    }

    @Override
    public boolean performAdd(List<String> items) {
        String body = String.format("{\"%s\":\"%s\"}",
                ObjectImportBean.COLUMNS[0], items.get(0));
        return performAdd(getURI(), body);
    }

    @Override
    public boolean performUpdate(List<String> items) {
        log.debug("{}.performUpdate({})", getClass().getSimpleName(), String.join(",", items));
        log.error("Objects are not updatable");
        return false;
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
}