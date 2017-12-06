/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests.domain;

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
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.event.Level.WARN;

@Slf4j
public class ObjectTest {

    private static DocumentContext documentContext;
    private static JSONArray objects;
    private static String token = "GS-Token eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJSb290IEFkbWluIn0.Z6VG9koCMFf-YfmghV9Se4tCpoVwrXyx5imKXC5o0TYWf4DNmO9rxc1EWgw-85_-Y_9tJqGdQePE2nJ728Mv86wtxtITWGa50pfGwlQAcMWP4k1hyl1CDv6agM5rhIkCIOndTegLjTpJDS5yssH1gB8qjKkdjZbsGVKlkbNSHPZuzdmapQlf1rAfMQFh4iDsRAR5qmlNfZrJCbFyzLlXn770j-fbqAsnSMYO7MMd97kVyvVaApxQuQFLhhKUIToG8I7o9F0Gg2ACBa0nWV2VFGxjaeaQd_bxEfrOeQl8N4H46HodMipxN394GK2h6TMIL01Rhx_YQ_I0XAFgu2iFtw";

    @BeforeClass
    public static void setup() throws IOException {
        Logging.setLogLevel(com.jayway.jsonpath.DocumentContext.class.getPackage().getName(), WARN);
        JsonContext jsonContext = new JsonContext();
        documentContext = jsonContext.parse(new File("data2/Objects-test.json"));
        objects = documentContext.read("$[*]");
    }

    @Test
    public void testObjects() {
        JSONArray objects = documentContext.read("$[*]");
        assertNotNull("Failed to parse objects", objects);
        log.info("objects count: {}", objects.size());
        assertEquals("Wrong number of objects parsed", 18, objects.size());
        objects.stream().forEach(it -> log.trace("object: {}", it));
    }

    @Test
    public void testObject() {
        String id = "Award";
        assertNotNull("Failed to parse objects", objects);
        assertEquals("Wrong number of objects parsed", 18, objects.size());
        assertTrue("Parsed objects should include: " + id, objects.contains(id));
        log.info("Filtered count: {}", objects.stream().filter(it -> id.equals(it.toString())).count());
        objects.stream().filter(it -> id.equals(it.toString())).forEach(it -> log.info("object: {}", it.toString()));
        Optional<?> objectId = objects.stream().filter(it -> id.equals(it.toString())).findFirst();
        assertTrue("Did find match", objectId.isPresent());
        assertEquals("Did not match object: " + id, id, objectId.get().toString());
    }

    @Test
    public void testObjectImporter() {
        String[] expected = {
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Added, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Added, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, "
        };
        CsvResult csvResult = null;
        ObjectImportBean importer = new ObjectImportBean(makeObjectProcessor1());
        try (FileInputStream fis = new FileInputStream("data2/Objects-test.csv")) {
            csvResult = importer.readInputStream(fis);
        } catch (IOException e) {
            log.error(e.getMessage());
            csvResult = null;
        }
        assertNotNull("csvResult should not be null", csvResult);
        log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
        csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
        assertEquals("wrong total count", 14, csvResult.getTotalCount());
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
    public void testLiveObjectImporter() throws URISyntaxException {
        AuthorizationImportBase.AuthorizationProcessor processor;
        String host = "http://172.31.2.135:8080";
        String path = "/api/objects";
        processor = new ObjectProcessor(host, path, token);
        ObjectImportBean importer = new ObjectImportBean(processor);
        CsvResult csvResult = null;
        try (FileInputStream fis = new FileInputStream("data2/Objects-test.csv")) {
            csvResult = importer.readInputStream(fis);
            log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
            csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
            assertEquals("wrong total count", 14, csvResult.getTotalCount());
            assertEquals("wrong error count", 0, csvResult.getErrorCount());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private AuthorizationImportBase.AuthorizationProcessor makeObjectProcessor1() {
        return new AuthorizationImportBase.AuthorizationProcessor() {

            @Override
            public AuthorizationImportBase.ACTION selectAction(Map<String, String> items) {
                final String objectId = items.get(ObjectImportBean.COLUMNS[0]);
                final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
                if (objects.contains(objectId)) {
                    ret[0] = AuthorizationImportBase.ACTION.SKIP;
                }
                return ret[0];
            }

            @Override
            public boolean performAdd(Map<String, String> items) {
                return true;
            }

            @Override
            public boolean performUpdate(Map<String, String> items) {
                return true;
            }

            @Override
            public boolean performDelete(Map<String, String> items) {
                return true;
            }

            @Override
            public boolean performSkip(Map<String, String> items) {
                return true;
            }
        };

    }
}
