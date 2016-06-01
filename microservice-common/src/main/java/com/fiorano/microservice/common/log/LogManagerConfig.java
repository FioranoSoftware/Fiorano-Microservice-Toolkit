/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.microservice.common.log;

import java.util.logging.Formatter;
import java.util.logging.Level;

public class LogManagerConfig {

    private String m_logDir = "log";

    // character encoding
    private String m_strEncoding;

    // log level
    private int m_logLevel;

    // formatter object
    private DefaultFormatter m_formatter;

    // Size Limit for file Handler
    private int m_nFileSizeLimit = 1000000;

    // max number of log files that can be created
    private int m_nFileCount = 4;
    private String logType = "java.util.logging.FileHandler";
    private boolean includeTimeStamp = true;
    private String timeStampFormat = "MM/dd/yyyy HH:mm:ss";

    public String getLogDir() {
        return m_logDir;
    }

    public void setLogDir(String logDir) {
        m_logDir = logDir;
    }

    public String getEncoding() {
        return m_strEncoding;
    }

    public void setEncoding(String encoding) {
        m_strEncoding = encoding;
    }

    public int getLevel() {
        return m_logLevel;
    }

    public void setLevel(int logLevel) {
        m_logLevel = logLevel;
    }

    public Level getLogLevel() {
        if (m_logLevel <= -1)
            return Level.OFF;

        switch (m_logLevel) {
            case 0:
                return Level.SEVERE;
            case 1:
                return Level.WARNING;
            case 2:
                return Level.INFO;
            case 3:
                return Level.CONFIG;
            case 4:
                return Level.FINE;
            case 5:
                return Level.FINER;
            case 6:
                return Level.FINEST;
            default:
                return Level.CONFIG;
        }
    }

    public int getFileSizeLimit() {
        return m_nFileSizeLimit;
    }

    public void setFileSizeLimit(int fileSizeLimit) {
        m_nFileSizeLimit = fileSizeLimit;
    }

    public int getFileCount() {
        return m_nFileCount;
    }

    public void setFileCount(int fileCount) {
        m_nFileCount = fileCount;
    }

//    public String getFormatter() {
//        return m_formatter;
//    }
//
//    public void setFormatter(String formatter) {
//        m_formatter = formatter;
//    }

    public Formatter getFormatterObject() {
        if (m_formatter == null) {
            m_formatter = new DefaultFormatter();
            m_formatter.useDateFormat(timeStampFormat);
            m_formatter.useTimeStamp(includeTimeStamp);
        }

        return m_formatter;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

//    public boolean isIncludeTimeStamp() {
//        return includeTimeStamp;
//    }

    public void setIncludeTimeStamp(boolean includeTimeStamp) {
        this.includeTimeStamp = includeTimeStamp;
    }

//    public String getTimeStampFormat() {
//        return timeStampFormat;
//    }

    public void setTimeStampFormat(String timeStampFormat) {
        this.timeStampFormat = timeStampFormat;
    }
}
