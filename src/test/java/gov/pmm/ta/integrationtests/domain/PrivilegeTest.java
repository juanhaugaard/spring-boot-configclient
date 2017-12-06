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
import org.springframework.util.comparator.NullSafeComparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.event.Level.TRACE;
import static org.slf4j.event.Level.WARN;

@Slf4j
public class PrivilegeTest {

    private static DocumentContext documentContext;
    private static JSONArray privileges;
    private static String token = "GS-Token eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJSb290IEFkbWluIn0.Z6VG9koCMFf-YfmghV9Se4tCpoVwrXyx5imKXC5o0TYWf4DNmO9rxc1EWgw-85_-Y_9tJqGdQePE2nJ728Mv86wtxtITWGa50pfGwlQAcMWP4k1hyl1CDv6agM5rhIkCIOndTegLjTpJDS5yssH1gB8qjKkdjZbsGVKlkbNSHPZuzdmapQlf1rAfMQFh4iDsRAR5qmlNfZrJCbFyzLlXn770j-fbqAsnSMYO7MMd97kVyvVaApxQuQFLhhKUIToG8I7o9F0Gg2ACBa0nWV2VFGxjaeaQd_bxEfrOeQl8N4H46HodMipxN394GK2h6TMIL01Rhx_YQ_I0XAFgu2iFtw";

    @BeforeClass
    public static void setup() throws IOException {
        Logging.setLogLevel(com.jayway.jsonpath.DocumentContext.class.getPackage().getName(), WARN);
        Logging.setLogLevel(PrivilegeTest.class, TRACE);
        JsonContext jsonContext = new JsonContext();
        documentContext = jsonContext.parse(new File("data2/Privileges-test.json"));
        privileges = documentContext.read("$.*.name");
    }

    @Test
    public void testPrivileges() {
        JSONArray privileges = documentContext.read("$.*");
        assertNotNull("Failed to parse privileges", PrivilegeTest.privileges);
        log.info("privileges count: {}", privileges.size());
        assertEquals("Wrong number of privileges parsed", 30, privileges.size());
        privileges.stream().forEach(it -> log.trace("privilege: {}", it));
    }

    @Test
    public void testPrivilege() {
        String id = "Create Award";
        assertNotNull("Failed to parse privileges", privileges);
        assertEquals("Wrong number of privileges parsed", 30, privileges.size());
        assertTrue("Parsed privileges should include: " + id, privileges.contains(id));
        log.info("Filtered count: {}", privileges.stream().filter(it -> id.equals(it.toString())).count());
        privileges.stream().filter(it -> id.equals(it.toString())).forEach(it -> log.info("privileges: {}", it.toString()));
        Optional<?> objectId = privileges.stream().filter(it -> id.equals(it.toString())).findFirst();
        assertTrue("Did find match", objectId.isPresent());
        assertEquals("Did not match object: " + id, id, objectId.get().toString());
    }

    @Test
    public void testPrivilegeImporter() {
        String[] expected = {
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Added, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Skipped, ",
                "Updated, ",
                "Updated, ",
                "Skipped, ",
                "Skipped, "
        };
        CsvResult csvResult = null;
        PrivilegeImportBean importer = new PrivilegeImportBean(makePrivilegeProcessor1());
        try (FileInputStream fis = new FileInputStream("data2/Privileges-test.csv")) {
            csvResult = importer.readInputStream(fis);
        } catch (IOException e) {
            log.error(e.getMessage());
            csvResult = null;
        }
        assertNotNull("csvResult should not be null", csvResult);
        log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
        csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
        assertEquals("wrong total count", 31, csvResult.getTotalCount());
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
    public void testLivePrivilegeImporter() throws URISyntaxException {
        String host = "http://172.31.2.135:8080";
        String path = "/api/privileges";
        CsvResult csvResult = null;
        AuthorizationImportBase.AuthorizationProcessor processor =new PrivilegeProcessor(host, path, token);
        PrivilegeImportBean importer = new PrivilegeImportBean(processor);
        try (FileInputStream fis = new FileInputStream("data2/Privileges-test.csv")) {
            csvResult = importer.readInputStream(fis);
        } catch (IOException e) {
            log.error(e.getMessage());
            csvResult = null;
        }
        assertNotNull("csvResult should not be null", csvResult);
        log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
        csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
        assertEquals("wrong total count", 31, csvResult.getTotalCount());
        assertEquals("wrong error count", 0, csvResult.getErrorCount());
    }

    private JSONArray getIdentifiers() {
        return privileges;
    }

    private DocumentContext getDocumentContext() {
        return documentContext;
    }

    private AuthorizationImportBase.AuthorizationProcessor makePrivilegeProcessor1() {
        return new AuthorizationImportBase.AuthorizationProcessor() {
            Comparator<Object> comparator = NullSafeComparator.NULLS_HIGH;

            @Override
            public AuthorizationImportBase.ACTION selectAction(Map<String, String> items) {
                if ((items == null) || !((items.size() != 3) || (items.size() != 4)))
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

            public boolean performAdd(final Map<String, String> items) {
                // do nothing
                return true;
            }

            public boolean performUpdate(final Map<String, String> items) {
                // do nothing
                return true;
            }

            private String column(int index) {
                if (index < 0 || index >= PrivilegeImportBean.COLUMNS.length)
                    throw new IllegalArgumentException("column index out of range: " + index);
                return PrivilegeImportBean.COLUMNS[index];
            }
        };

    }
}
