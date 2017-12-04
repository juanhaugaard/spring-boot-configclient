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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.slf4j.event.Level.WARN;

@Slf4j
public class SubjectTest {

    private static DocumentContext documentContext;
    private static JSONArray identifiers;
    private static String token = "GS-Token eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJSb290IEFkbWluIn0.Z6VG9koCMFf-YfmghV9Se4tCpoVwrXyx5imKXC5o0TYWf4DNmO9rxc1EWgw-85_-Y_9tJqGdQePE2nJ728Mv86wtxtITWGa50pfGwlQAcMWP4k1hyl1CDv6agM5rhIkCIOndTegLjTpJDS5yssH1gB8qjKkdjZbsGVKlkbNSHPZuzdmapQlf1rAfMQFh4iDsRAR5qmlNfZrJCbFyzLlXn770j-fbqAsnSMYO7MMd97kVyvVaApxQuQFLhhKUIToG8I7o9F0Gg2ACBa0nWV2VFGxjaeaQd_bxEfrOeQl8N4H46HodMipxN394GK2h6TMIL01Rhx_YQ_I0XAFgu2iFtw";

    @BeforeClass
    public static void setup() throws IOException {
        Logging.setLogLevel("com.jayway.jsonpath", WARN);
        JsonContext jsonContext = new JsonContext();
        documentContext = jsonContext.parse(new File("data2/Subjects-Test.json"));
        identifiers = documentContext.read("$[*]['identifier']");
    }

    @Test
    public void testIdentifiers() {
        assertNotNull("Failed to parse identifiers", identifiers);
        assertTrue("Parsed zero identifiers", identifiers.size() > 0);
        log.info("identifier count: {}", identifiers.size());
        identifiers.stream().forEach(it -> log.trace("id: {}", it));
    }

    @Test
    public void testSubjects() {
        JSONArray subjects = documentContext.read("$[*]");
        assertNotNull("Failed to parse subjects", subjects);
        assertTrue("Parsed zero subjects", subjects.size() > 0);
        log.info("subjects count: {}", subjects.size());
        subjects.stream().forEach(it -> log.trace("subject: {}", it));
    }

    @Test
    public void testSubject() {
        String id = "JHaugaard";
        String filter = String.format("$[?(@.identifier=='%s')]", id);
        JSONArray subject = documentContext.read(filter);
        assertNotNull("Failed to parse subject", subject);
        assertEquals("Should parsed one subject", 1, subject.size());
        log.info("subjects count: {}", subject.size());
        Map<String, String> theSubject = (Map<String, String>) subject.get(0);
        String userId = theSubject.get("identifier");
        String userType = theSubject.get("type");
        log.debug("subject: {}, {}", userId, userType);
        assertEquals(id, userId);
        assertEquals("User", userType);
    }

    @Test
    public void testSubjectImporter() {
        String[] expected = {
                "Skipped -",
                "Failed on # 2",
                "Added -",
                "Added -",
                "Added -",
                "Failed on # 6",
                "Added -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Skipped -",
                "Updated -",
                "Skipped -",
                "Skipped -",
                "Updated -"
        };
        CsvResult csvResult = null;
        SubjectImportBean importer = new SubjectImportBean(makeSubjectProcessor1());
        try (FileInputStream fis = new FileInputStream("data2/Subjects-test.csv")) {
            csvResult = importer.readInputStream(fis);
        } catch (IOException e) {
            log.error(e.getMessage());
            csvResult = null;
        }
        assertNotNull("csvResult should not be null", csvResult);
        log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
        csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
        assertEquals("wrong total count", 19, csvResult.getTotalCount());
        assertEquals("wrong error count", 2, csvResult.getErrorCount());
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
    public void testLiveSubjectImporter() throws URISyntaxException {
        AuthorizationImportBase.AuthorizationProcessor processor;
        String host = "http://172.31.2.135:8080";
        String path = "/api/subjects";
        processor = new SubjectProcessor(host, path, token);
        SubjectImportBean importer = new SubjectImportBean(processor);
        CsvResult csvResult = null;
        try (FileInputStream fis = new FileInputStream("data2/Subjects-test.csv")) {
            csvResult = importer.readInputStream(fis);
            log.info("Result total:{} errors:{}", csvResult.getTotalCount(), csvResult.getErrorCount());
            csvResult.getItems().stream().forEach(it -> log.info("{} {}", it.getStatus(), it.getDescription()));
            assertEquals("wrong total count", 19, csvResult.getTotalCount());
            assertEquals("wrong error count", 3, csvResult.getErrorCount());
        } catch (IOException e) {
            log.error(e.getMessage());
            csvResult = null;
        }
    }

    @Test
    @Ignore("Requires VPN to aws TEST")
    public void testLiveSubjectProcessorDelete() throws URISyntaxException {
        AuthorizationImportBase.AuthorizationProcessor processor;
        String host = "http://172.31.2.135:8080";
        String path = "/api/subjects";
        processor = new SubjectProcessor(host, path, token);
        List<String> items = new ArrayList<>();
        items.add("John Doe");
        items.add("User");
        assertFalse(processor.performDelete(items));
    }

    private AuthorizationImportBase.AuthorizationProcessor makeSubjectProcessor1() {
        return new AuthorizationImportBase.AuthorizationProcessor() {

            @Override
            public AuthorizationImportBase.ACTION selectAction(List<String> items) {
                final String subjectId = items.get(0);
                final AuthorizationImportBase.ACTION[] ret = {AuthorizationImportBase.ACTION.ADD};
                if (identifiers.contains(subjectId)) {
                    String filter = String.format("$[?(@.identifier=='%s')]", subjectId);
                    JSONArray result = documentContext.read(filter);
                    assertNotNull("JsonReadFilteredFor(" + subjectId + ") returned null", result);
                    assertTrue("JsonReadFilteredFor(" + subjectId + ") should be zero or one", result.size() < 2);
                    result.stream()
                            .map(o -> (Map<String, String>) o)
                            .filter(item -> items.get(0).equals(item.get("identifier")))
                            .forEach(item -> {
                                if (items.get(1).equals(item.get("type"))) {
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
