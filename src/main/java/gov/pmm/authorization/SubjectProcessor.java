package gov.pmm.authorization;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.internal.JsonContext;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SubjectProcessor implements AuthorizationImportBase.AuthorizationProcessor {

    private RestTemplate rest;
    private URI uri;
    private String token;
    private DocumentContext documentContext;
    private JSONArray identifiers;

    @Autowired
    public SubjectProcessor(
            @Value("${authorization.host}") String host,
            @Value("${subjects.path:/api/subjects}") String path,
            @Value("${authorization.token}") String token) throws URISyntaxException {
        log.debug("Constructing {} with url={}{}", getClass().getSimpleName(), host, path);
        this.rest = new RestTemplate();
        this.token = token;
        setURI(host + path);
    }

    @Override
    public AuthorizationImportBase.ACTION selectAction(List<String> items) {
        if ((items == null) || (items.size() != 2))
            throw new IllegalArgumentException("invalid items parameter");
        final String subjectId = items.get(0);
        final String subjectType = items.get(1);
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
    public boolean performAdd(List<String> items) {
        log.trace("{}.performAdd({},{})", getClass().getSimpleName(), items.get(0), items.get(1));
        boolean ret = false;
        try {
            String body = String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}",
                    SubjectImportBean.COLUMNS[0], items.get(0),
                    SubjectImportBean.COLUMNS[1], items.get(1));
            HttpEntity<String> entity = makeEntity(body);
            ResponseEntity<String> responseEntity = rest.exchange(
                    getURI(),
                    HttpMethod.POST,
                    entity,
                    String.class);
            if (responseEntity != null) {
                if ((responseEntity.getStatusCode() == HttpStatus.OK) || (responseEntity.getStatusCode() == HttpStatus.CREATED)) {
                    ret = true;
                } else {
                    log.error("POST: {} -- {}", getURI(), responseEntity.getStatusCode());
                }
            } else {
                log.error("POST: {} response is null", getURI());
            }
        } catch (RestClientException e) {
            logRestClientException(e);
        }
        return ret;
    }

    @Override
    public boolean performUpdate(List<String> items) {
        log.trace("{}.performUpdate({},{})", getClass().getSimpleName(), items.get(0), items.get(1));
        boolean ret = false;
        try {
            String body = String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}",
                    SubjectImportBean.COLUMNS[0], items.get(0),
                    SubjectImportBean.COLUMNS[1], items.get(1));
            HttpEntity<String> entity = makeEntity(body);
            ResponseEntity<String> responseEntity = rest.exchange(
                    getURI(),
                    HttpMethod.PUT,
                    entity,
                    String.class);
            if (responseEntity != null) {
                if ((responseEntity.getStatusCode() == HttpStatus.OK) || (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT)) {
                    ret = true;
                } else {
                    log.error("PUT: {} -- {}", getURI(), responseEntity.getStatusCode());
                }
            } else {
                log.error("PUT: {} response is null", getURI());
            }
        } catch (RestClientException e) {
            logRestClientException(e);
        }
        return ret;
    }

    @Override
    public boolean performDelete(List<String> items) {
        log.trace("{}.performDelete({})", getClass().getSimpleName(), items.get(0));
        boolean ret = false;
        try {
            StringBuilder url = new StringBuilder(getURI().getScheme());
            url.append("://").append(getURI().getHost());
            url.append(":").append(getURI().getPort()).append(getURI().getPath());
            url.append("/").append(items.get(0));
            HttpEntity<String> entity = makeEntity();
            ResponseEntity<String> responseEntity = rest.exchange(
                    url.toString(),
                    HttpMethod.DELETE,
                    entity,
                    String.class);
            if (responseEntity != null) {
                if ((responseEntity.getStatusCode() == HttpStatus.OK) || (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT)) {
                    ret = true;
                } else {
                    log.error("DELETE: {} -- {}", url, responseEntity.getStatusCode());
                }
            } else {
                log.error("DELETE: {} response is null", url);
            }
        } catch (RestClientException e) {
            logRestClientException(e);
        }
        return ret;
    }

    @Override
    public boolean performSkip(List<String> items) {
        log.trace("{}.performSkip({},{})", getClass().getSimpleName(), items.get(0), items.get(1));
        return true;
    }

    @PostConstruct
    public void setup() {
        log.trace("setup");
        String json = getAllSubjects();
        if (!StringUtils.isEmpty(json)) {
            JsonContext jsonContext = new JsonContext();
            documentContext = jsonContext.parse(json);
            identifiers = documentContext.read("$[*]['identifier']");
        } else {
            log.error("All Subjects JSON is empty");
        }
    }

    private JSONArray getIdentifiers() {
        if (identifiers == null)
            setup();
        return identifiers;
    }

    private DocumentContext getDocumentContext() {
        if (documentContext == null)
            setup();
        return documentContext;
    }

    private String getAllSubjects() {
        String ret = null;
        log.trace("getAllSubjects: uri={}", getURI());
        try {
            HttpEntity<String> entity = makeEntity();
            ResponseEntity<String> responseEntity = rest.exchange(
                    getURI(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            if (responseEntity != null) {
                if ((responseEntity.getStatusCode() == HttpStatus.OK) || (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT)) {
                    ret = responseEntity.getBody();
                } else {
                    log.error("GET: {} -- {}", getURI(), responseEntity.getStatusCode());
                }
            } else {
                log.error("GET: {} response is null", getURI());
            }
        } catch (RestClientException e) {
            logRestClientException(e);
        }
        return ret;
    }

    private HttpEntity<String> makeEntity(String body) {
        return new HttpEntity<>(body, getHeadersWithClientAuthToken());
    }

    private HttpEntity<String> makeEntity() {
        return new HttpEntity<>(getHeadersWithClientAuthToken());
    }

    private HttpHeaders getHeadersWithClientAuthToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, token);

        return headers;
    }

    public URI getURI() {
        return uri;
    }

    public void setURI(String uri) throws URISyntaxException {
        this.uri = new URI(uri);
    }

    private void logRestClientException(RestClientException exception) {
        if (exception instanceof HttpClientErrorException) {
            log.error("RestTemplate exception: {} -- {}", exception.getMessage(), ((HttpClientErrorException) exception).getResponseBodyAsString());
        } else {
            log.error("RestTemplate exception: {}", exception.getMessage(), exception);
        }
    }

}
