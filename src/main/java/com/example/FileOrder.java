/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package com.example;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FileOrder {
    public static final List<String> FILE_ORDER = Arrays.asList(
            "subjects", "objects", "actions",
            "scopetypes", "scopes", "scopegroups",
            "privileges", "roles",
            "assignments", "delegations"
    );

    public static Integer apply(final String filename) {
        if (StringUtils.isEmpty(filename)) return -1;
        return IntStream.range(0, FILE_ORDER.size())
                .filter(i -> filename.toLowerCase()
                        .startsWith(FILE_ORDER.get(i)))
                .findFirst()
                .orElse(-1);
    }

//    public static void main(String[] args) {
//        String[] data = {
//                "Subjects-test.csv",
//                "Delegations-test.csv",
//                "Objects-test.csv",
//                "Assignments-test.csv",
//                "ScopeGroups-test.csv",
//                "Scopes-test.csv",
//                "ScopeTypes-test.csv",
//                "Privileges-test.csv",
//                "Roles-test.csv",
//                "Actions-test.csv",
//                "Subjects-test.other",
//                "Delegations-test.other",
//                "ScopeGroups-test.other",
//                "Objects-test.other",
//                "Assignments-test.other",
//                "Scopes-test.other",
//                "ScopeTypes-test.other",
//                "Privileges-test.other",
//                "Roles-test.other",
//                "Actions-test.other",
//                "Errors-test.csv"
//        };
//        System.out.println("=== Calclulate rankings ===");
//        Arrays.stream(data)
//                .forEach(it -> System.out.println(it + ":\t" + apply(it)));
//        System.out.println("=== Sort alphabetical ===");
//        Arrays.stream(data)
//                .sorted()
//                .forEach(it -> System.out.println(it + ":\t" + apply(it)));
//        System.out.println("=== Sort by rankings ===");
//        Arrays.stream(data)
//                .sorted(new FileOrder())
//                .forEach(it -> System.out.println(it + ":\t" + apply(it)));
//        System.out.println("=== Only valid Sort by rankings ===");
//        Arrays.stream(data)
//                .filter(it -> it.toLowerCase().endsWith(".csv"))
//                .filter(it -> apply(it) >= 0)
//                .sorted(new FileOrder())
//                .forEach(it -> System.out.println(it + ":\t" + apply(it)));
//    }

    public static int compare(String o1, String o2) {
        if ((o1 == null) && (o2 == null)) {
            return 0;
        }
        if ((o1 == null) && (o2 != null)) {
            return -1;
        }
        if ((o1 != null) && (o2 == null)) {
            return 1;
        }
        return apply(o1) - apply(o2);
    }

    public static List<String> sortAndFilter(final String[] array) {
        return sortAndFilter(Arrays.asList(array));
    }

    public static List<String> sortAndFilter(final List<String> list) {
        return sortAndFilter(list.stream()).collect(Collectors.toList());
    }

    public static Stream<String> sortAndFilter(final Stream<String> stream) {
        return stream
                .filter(it -> it
                        .toLowerCase()
                        .endsWith(".csv"))
                .filter(it -> apply(it) >= 0)
                .sorted(FileOrder::compare);
    }
}
