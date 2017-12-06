/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Readers {
    public static String readFileOrResource(
            final String name,
            final String filePath,
            final String resourcePath) throws IOException {

        String fullFileName = null;
        String fullResourceName = null;
        if (StringUtils.isEmpty(filePath)) {
            fullFileName = name;
        } else {
            fullFileName = filePath + File.separator + name;
        }
        try {
            return readFile(fullFileName);
        } catch (IOException ioe) {
            try {
                if (StringUtils.isEmpty(resourcePath)) {
                    fullResourceName = '/' + name;
                } else {
                    fullResourceName = resourcePath + '/' + name;
                }
                return readResource(fullResourceName);
            } catch (IOException ioe2) {
                throw new IOException("Could not read neither file " + fullFileName + " or resource " + fullResourceName);
            }
        }
    }

    public static String readFile(final String filename) throws IOException {
        String ret = null;
        log.trace("Reading file {}", filename);
        try (InputStream stream = new FileInputStream(filename)) {
            ret = streamToString(stream);
        }
        return ret;
    }

    public static String readResource(final String resourcename) throws IOException {
        String ret = null;
        log.trace("Reading resource {}", resourcename);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream(resourcename)) {
            ret = streamToString(stream);
        }
        return ret;
    }

    public static String streamToString(final InputStream stream) throws IOException {
        return FileCopyUtils.copyToString(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
