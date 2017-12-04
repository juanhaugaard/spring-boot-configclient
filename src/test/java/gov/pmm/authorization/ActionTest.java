/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.authorization;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.internal.JsonContext;
import gov.pmm.common.util.Logging;
import gov.pmm.common.util.csv.CsvItemResult;
import gov.pmm.common.util.csv.CsvResult;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.slf4j.event.Level.WARN;

@Slf4j
public class ActionTest {

    private static DocumentContext documentContext;
    private static JSONArray actions;
    private static String token = "GS-Token eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJSb290IEFkbWluIn0.Z6VG9koCMFf-YfmghV9Se4tCpoVwrXyx5imKXC5o0TYWf4DNmO9rxc1EWgw-85_-Y_9tJqGdQePE2nJ728Mv86wtxtITWGa50pfGwlQAcMWP4k1hyl1CDv6agM5rhIkCIOndTegLjTpJDS5yssH1gB8qjKkdjZbsGVKlkbNSHPZuzdmapQlf1rAfMQFh4iDsRAR5qmlNfZrJCbFyzLlXn770j-fbqAsnSMYO7MMd97kVyvVaApxQuQFLhhKUIToG8I7o9F0Gg2ACBa0nWV2VFGxjaeaQd_bxEfrOeQl8N4H46HodMipxN394GK2h6TMIL01Rhx_YQ_I0XAFgu2iFtw";

    @BeforeClass
    public static void setup() throws IOException {
        Logging.setLogLevel("com.jayway.jsonpath", WARN);
        JsonContext jsonContext = new JsonContext();
        documentContext = jsonContext.parse(new File("data2/Actions-test.json"));
        actions = documentContext.read("$[*]");
    }

    @Test
    public void testActions() {
        JSONArray objects = documentContext.read("$[*]");
        assertNotNull("Failed to parse actions", objects);
        log.info("actions count: {}", objects.size());
        assertEquals("Wrong number of actions parsed", 10, objects.size());
        objects.stream().forEach(it -> log.trace("object: {}", it));
    }

    @Test
    public void testAction() {
        String id = "Submit";
        assertNotNull("Failed to parse actions", actions);
        assertEquals("Wrong number of actions parsed", 10, actions.size());
        assertTrue("Parsed actions should include: " + id, actions.contains(id));
        log.info("Filtered count: {}", actions.stream().filter(it -> id.equals(it.toString())).count());
        actions.stream().filter(it -> id.equals(it.toString())).forEach(it -> log.info("object: {}", it.toString()));
        Optional<?> objectId = actions.stream().filter(it -> id.equals(it.toString())).findFirst();
        assertTrue("Did find match", objectId.isPresent());
        assertEquals("Did not match object: " + id, id, objectId.get().toString());
    }

    @Test
    public void testActionImporter() {
        String[] expected = {
                "Skipped -",
                "Added -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Added -",
                "Skipped -",
                "Skipped -",
                "Added -",
                "Skipped -",
                "Skipped -",
                "Skipped -"
        };
        CsvResult csvResult = null;
        ActionImportBean importer = new ActionImportBean(makeActionProcessor1());
        try (FileInputStream fis = new FileInputStream("data2/Actions-test.csv")) {
            csvResult = importer.readInputStream(fis);
        } catch (IOException e) {
            log.error(e.getMessage());
            csvResult = null;
        }
        assertNotNull("csvResult should not be null", csvResult);
        log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
        csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
        assertEquals("wrong total count", 13, csvResult.getTotalCount());
        assertEquals("wrong error count", 0, csvResult.getErrorCount());
        int i = 0;
        for (CsvItemResult item : csvResult.getItems()) {
            assertTrue("Unexpected CSV Item "
                            + (i + 1)
                            + " result, expected: "
                            + expected[i]
                            + ", actual: "
                            + item.getDescription().substring(0, expected[i].length()),
                    item.getDescription().startsWith(expected[i++]));
        }
    }

    @Test
    @Ignore("Requires VPN to aws TEST")
    public void testLiveActionImporter() throws URISyntaxException {
        AuthorizationImportBase.AuthorizationProcessor processor;
        String host = "http://172.31.2.135:8080";
        String path = "/api/actions";
        processor = new ActionProcessor(host, path, token);
        ActionImportBean importer = new ActionImportBean(processor);
        CsvResult csvResult = null;
        try (FileInputStream fis = new FileInputStream("data2/Actions-test.csv")) {
            csvResult = importer.readInputStream(fis);
            log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
            csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
            assertEquals("wrong total count", 13, csvResult.getTotalCount());
            assertEquals("wrong error count", 0, csvResult.getErrorCount());
        } catch (IOException e) {
            log.error(e.getMessage());
            csvResult = null;
        }
    }

    private AuthorizationImportBase.AuthorizationProcessor makeActionProcessor1() {
        return new AuthorizationImportBase.AuthorizationProcessor() {

            @Override
            public AuthorizationImportBase.ACTION selectAction(List<String> items) {
                final String objectId = items.get(0);
                final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
                if (actions.contains(objectId)) {
                    ret[0] = AuthorizationImportBase.ACTION.SKIP;
                }
                return ret[0];
            }

            @Override
            public boolean performAdd(List<String> items) {
                return true;
            }

            @Override
            public boolean performUpdate(List<String> items) {
                return true;
            }

            @Override
            public boolean performDelete(List<String> items) {
                return true;
            }

            @Override
            public boolean performSkip(List<String> items) {
                return true;
            }
        };

    }
}