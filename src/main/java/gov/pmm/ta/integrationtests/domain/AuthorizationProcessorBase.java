/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class AuthorizationProcessorBase implements AuthorizationImportBase.AuthorizationProcessor {
    private URI uri;
    private String token;
    private RestTemplate rest;

    protected AuthorizationProcessorBase(
            String host, String path, String token) throws URISyntaxException {
        this.token = token;
        this.rest = new RestTemplate();
        setURI(host + path);
    }

    public static String toJson(Map<String, String> items) {
        final List<String> json = new ArrayList<>();
        items.forEach((key, value) -> json.add(String.format("\"%s\":\"%s\"", key, value)));
        return "{" + String.join(",", json) + "}";
    }

    protected boolean performAdd(final URI uri, final String body) {
        log.trace("{}.performAdd({})", getClass().getSimpleName(), body);
        boolean ret = false;
        try {
            HttpEntity<String> entity = makeEntity(body);
            ResponseEntity<String> responseEntity = getRest().exchange(
                    uri,
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

    protected boolean performUpdate(final URI uri, final String body) {
        log.trace("{}.performUpdate({})", getClass().getSimpleName(), body);
        boolean ret = false;
        try {
            HttpEntity<String> entity = makeEntity(body);
            ResponseEntity<String> responseEntity = getRest().exchange(
                    uri,
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

    protected boolean performDelete(final String url, final Optional<String> body) {
        if ((body != null) && body.isPresent())
            log.trace("{}.performDelete({},{})", getClass().getSimpleName(), url, body.get());
        else
            log.trace("{}.performDelete({})", getClass().getSimpleName(), url);
        boolean ret = false;
        try {
            HttpEntity<String> entity;
            if ((body != null) && body.isPresent())
                entity = makeEntity(body.get());
            else
                entity = makeEntity();
            ResponseEntity<String> responseEntity = getRest().exchange(
                    url,
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

    protected HttpEntity<String> makeEntity(String body) {
        return new HttpEntity<>(body, getHeadersWithClientAuthToken());
    }

    protected HttpEntity<String> makeEntity() {
        return new HttpEntity<>(getHeadersWithClientAuthToken());
    }

    private HttpHeaders getHeadersWithClientAuthToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, token);

        return headers;
    }

    protected void logRestClientException(RestClientException exception) {
        if (exception instanceof HttpClientErrorException) {
            log.error("RestTemplate exception: {} -- {}", exception.getMessage(), ((HttpClientErrorException) exception).getResponseBodyAsString());
        } else {
            log.error("RestTemplate exception: {}", exception.getMessage(), exception);
        }
    }

    protected URI getURI() {
        return uri;
    }

    protected AuthorizationProcessorBase setURI(String uri) throws URISyntaxException {
        this.uri = new URI(uri);
        return this;
    }

    protected String getToken() {
        return token;
    }

    protected AuthorizationProcessorBase setToken(String token) {
        this.token = token;
        return this;
    }

    protected AuthorizationProcessorBase setRest(RestTemplate rest) {
        this.rest = rest;
        return this;
    }

    protected RestTemplate getRest() {
        return rest;
    }

    protected String getAll(final URI uri) {
        String ret = null;
        log.trace("getAll: uri={}", uri);
        try {
            HttpEntity<String> entity = makeEntity();
            ResponseEntity<String> responseEntity = getRest().exchange(
                    uri,
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

}
