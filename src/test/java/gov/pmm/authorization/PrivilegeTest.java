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
import org.junit.Test;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.event.Level.INFO;
import static org.slf4j.event.Level.TRACE;
import static org.slf4j.event.Level.WARN;

@Slf4j
public class PrivilegeTest {

    private static DocumentContext documentContext;
    private static JSONArray privileges;
    private static String token = "GS-Token eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJSb290IEFkbWluIn0.Z6VG9koCMFf-YfmghV9Se4tCpoVwrXyx5imKXC5o0TYWf4DNmO9rxc1EWgw-85_-Y_9tJqGdQePE2nJ728Mv86wtxtITWGa50pfGwlQAcMWP4k1hyl1CDv6agM5rhIkCIOndTegLjTpJDS5yssH1gB8qjKkdjZbsGVKlkbNSHPZuzdmapQlf1rAfMQFh4iDsRAR5qmlNfZrJCbFyzLlXn770j-fbqAsnSMYO7MMd97kVyvVaApxQuQFLhhKUIToG8I7o9F0Gg2ACBa0nWV2VFGxjaeaQd_bxEfrOeQl8N4H46HodMipxN394GK2h6TMIL01Rhx_YQ_I0XAFgu2iFtw";

    @BeforeClass
    public static void setup() throws IOException {
        Logging.setLogLevel("com.jayway.jsonpath", WARN);
        Logging.setLogLevel("gov.pmm.authorization.PrivilegeTest", TRACE);
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
        assertEquals("wrong total count", 30, csvResult.getTotalCount());
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

    private AuthorizationImportBase.AuthorizationProcessor makePrivilegeProcessor1() {
        return new AuthorizationImportBase.AuthorizationProcessor() {

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
