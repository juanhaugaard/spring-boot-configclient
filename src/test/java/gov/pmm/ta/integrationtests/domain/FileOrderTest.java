/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.ta.integrationtests.domain;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileOrderTest {
    private String[] data = {
            "Subjects-test.csv",
            "Delegations-test.csv",
            "Objects-test.csv",
            "Assignments-test.csv",
            "ScopeGroups-test.csv",
            "Scopes-test.csv",
            "ScopeTypes-test.csv",
            "Privileges-test.csv",
            "Roles-test.csv",
            "Actions-test.csv",
            "Subjects-test.other",
            "Delegations-test.other",
            "ScopeGroups-test.other",
            "Objects-test.other",
            "Assignments-test.other",
            "Scopes-test.other",
            "ScopeTypes-test.other",
            "Privileges-test.other",
            "Roles-test.other",
            "Actions-test.other",
            "Errors-test.csv"
    };

    @Test
    public void testApply() {
        assertEquals(Integer.valueOf(4), FileOrder.apply(data[5]));
        assertEquals(Integer.valueOf(-1), FileOrder.apply(data[data.length - 1]));
    }

    @Test
    public void testCompare() {
        assertTrue(FileOrder.compare(data[4], data[5]) > 0);
        assertTrue(FileOrder.compare(data[5], data[5]) == 0);
        assertTrue(FileOrder.compare(data[2], data[7]) < 0);
    }

    @Test
    public void testSortAndFilter() {
        String[] expected = {
                data[0], data[2],
                data[9], data[6],
                data[5], data[4],
                data[7], data[8],
                data[3], data[1]
        };
        List<String> result = FileOrder.sortAndFilter(data);
        assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.get(i));
        }
    }
}
