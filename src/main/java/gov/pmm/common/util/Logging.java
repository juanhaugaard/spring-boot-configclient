/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


public class Logging {
    public static void setLogLevel(final Class clazz, final Level level) {
        setLogLevel(clazz.getName(), level);
    }

    public static void setLogLevel(final String className, final Level level) {
        Logger logger = LoggerFactory.getLogger(className);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger l = (ch.qos.logback.classic.Logger) logger;
            l.setLevel(translateLevel(level));
        }
    }

    public static ch.qos.logback.classic.Level translateLevel(final Level level) {
        ch.qos.logback.classic.Level ret;
        switch (level) {
            case INFO:
                ret = ch.qos.logback.classic.Level.INFO;
                break;
            case WARN:
                ret = ch.qos.logback.classic.Level.WARN;
                break;
            case ERROR:
                ret = ch.qos.logback.classic.Level.ERROR;
                break;
            case DEBUG:
                ret = ch.qos.logback.classic.Level.DEBUG;
                break;
            case TRACE:
                ret = ch.qos.logback.classic.Level.TRACE;
                break;
            default:
                ret = ch.qos.logback.classic.Level.INFO;
        }
        return ret;
    }
}
