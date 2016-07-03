/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.microservice.common.log;

import fiorano.jms.boot.IBootConstants;
import fiorano.jms.common.FioranoException;
import fiorano.jms.log2.ILogConstants;
import fiorano.jms.log2.ILogIterator;
import fiorano.jms.log2.def.DefaultFormatter;
import fiorano.jms.log2.def.FioranoErrorHandler;
import fiorano.jms.log2.def.FioranoLogHandler;
import fiorano.jms.log2.def.FioranoOutHandler;
import fiorano.jms.util.Util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.*;

/**
 * Defines APIs to create, view and clear logs
 *
 * @author FSIPL
 * @version 1.0
 * @created June 22, 2005
 */
public class LogManager {

    private static HashMap<String, Handler[]> fileHandlerMap = new HashMap<String, Handler[]>();   // this map is added for handling the special case of InMemory component handlers
    private HashMap<String, Logger> componentLoggerMap = new HashMap<>();
    // config object
    private LogManagerConfig logManagerConfig;
    //  Map stores the uniqueId vs Handler array pair . Handler array has outFileHanlder at 0th index
    // and errFileHandler at 1st index
    // sync object
    private Object m_syncObject = new Object();

    /**
     * <p>Get the errLogs for a given uniqueId</p>
     *
     * @param uniqueId
     * @return
     */
    public ILogIterator getErrLogs(String uniqueId) {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        String dirName = getLogDirName(uniqueId);

        // create file filter
        String[] posExt = new String[]{".err"};
        String[] negExt = new String[]{".lck"};

        FilenameFilter filter = new LogFileNameFilter(posExt, negExt);

        return new DefaultLogIterator(this, dirName, filter, logManagerConfig.getEncoding());
    }

    /**
     * <p>Get the outLogs for a given uniqueId</p>
     *
     * @param uniqueId
     * @return
     */
    public ILogIterator getOutLogs(String uniqueId) {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        String dirName = getLogDirName(uniqueId);

        // create file filter
        String[] posExt = new String[]{".out"};
        String[] negExt = new String[]{".lck"};

        FilenameFilter filter = new LogFileNameFilter(posExt, negExt);

        return new DefaultLogIterator(this, dirName, filter, logManagerConfig.getEncoding());
    }

    /**
     * Get a filter property.
     *
     * @param props
     * @param name
     * @param defaultValue
     * @return
     */
    public Filter getFilterProperty(Properties props, String name,
                                    Filter defaultValue) {
        if (props == null)
            return defaultValue;

        String val = props.getProperty(name);

        try {
            if (val != null) {
                Class clz = ClassLoader.getSystemClassLoader().loadClass(val);

                return (Filter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return defaultValue;
    }

    /**
     * Configure Stub Manager
     *
     * @param config Config
     * @throws FioranoException
     */
    public void configure(LogManagerConfig config) throws FioranoException {
        logManagerConfig = config;
    }

    /**
     * Create a Handler object with configuration specified
     * in 'properties' parameter. It recognizes properties
     * defined in standard jdk logging.
     * <p/>
     * The created handler is associated with parameter uniqueId.
     *
     * @param uniqueId
     * @param properties
     * @return
     * @throws Exception
     */
    public synchronized Handler createHandler(String uniqueId, Properties properties)
            throws Exception {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        int type = ILogConstants.UNKNOWN_HANDLER_TYPE;

        String handler = properties.getProperty(ILogConstants.LOG_HANDLER, ILogConstants.LOG_HANDLER_DEF);

        if (handler.equalsIgnoreCase(FileHandler.class.getName()))
            type = ILogConstants.FILE_HANDLER_TYPE;

        else if (handler.equalsIgnoreCase(ConsoleHandler.class.
                getName()))
            type = ILogConstants.CONSOLE_HANDLER_TYPE;


        return createHandler(type, uniqueId, properties);
    }

    /**
     * Create a Handler object with type (Console, file .. )
     * and configuration specified in 'properties' parameter. It
     * recognizes properties defined in standard jdk logging.
     * <p/>
     * The created handler is associated with parameter uniqueId.
     *
     * @param type
     * @param uniqueId
     * @param properties
     * @return
     * @throws Exception
     */
    public Handler createHandler(int type, String uniqueId,
                                 Properties properties)
            throws Exception {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        // In case handler for this uniqueId exists,
        // then return the already created one.

        // This occurs when application tries to create
        // unified connections. In that case fmq rtl creates
        // one queueConnection and one topic connection. As each connection
        // object has separate handler, then two handlers were getting created
        // at server side.
        //
        // - Aseem Bansal
        //
        FioranoLogHandler logHandler = null;
        if (properties.getProperty("IsInMemComponentLogger") == null) {  // The property IsInMemoryLogger specifies the special case of creating handler for InMemory component
            logHandler = getHandler(type, uniqueId);                     //  If the property is null , it will proceed normally as in all other cases else the case will be treated differently
            if (logHandler != null)
                return logHandler;
        }

        synchronized (m_syncObject) {
            if (properties.getProperty("IsInMemComponentLogger") == null) {
                logHandler = getHandler(type, uniqueId);

                if (logHandler != null)
                    return logHandler;
            }

            logHandler = _createHandler(type, properties, uniqueId);
        }

        if (logHandler == null)
            return null;

        Logger logger = getLogger(uniqueId);

        logger.setUseParentHandlers(false);

        if (properties.getProperty("IsInMemComponentLogger") == null || getHandler(type, uniqueId) == null)
            logger.addHandler(logHandler);

        return logHandler;
    }

    /**
     * Destroy the parameter handler.
     *
     * @param uniqueId
     * @param handler
     */
    public void destroyHandler(String uniqueId, Handler handler) {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        Logger logger = getLogger(uniqueId);

        // Remove handler before flush and close so that no more
        // logs are passed to this handler object
        //
        logger.removeHandler(handler);

        handler.flush();
        handler.close();
    }

    /**
     * Destroy all handlers associated with the given id.
     *
     * @param uniqueId
     */
    public void destroyHandlers(String uniqueId) {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        for(String loggerName : componentLoggerMap.keySet()){
            if(loggerName.toUpperCase().contains(uniqueId)){
                Logger logger = componentLoggerMap.get(loggerName);
                Handler[] handlers = logger.getHandlers();

                for (int i = 0; i < handlers.length; i++){
                    handlers[i].flush();
                    handlers[i].close();
                    logger.removeHandler(handlers[i]);
                }
            }
        }

    }

    /**
     * Clear error logs stored for a given Id
     *
     * @param uniqueId
     * @return
     */
    public boolean clearErrLogs(String uniqueId, Logger logger) {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        // Algo:
        //
        // 1. In case fileHandler is not plugged in for this id, then
        //    return
        //
        //  Otherwise,
        //
        // 2.  Close and remove the fileHandler from logger table
        //    (This will give write access to all log files)
        //
        // 3. Clear the log files
        //
        // 4. Add the handlers
        //
        // Note: - It might be possible that somebody has the reference
        //         of FioranoHandler, so in re-creation, we only
        //         change the internal handlers.
        //

        // 1. In case fileHandler is not plugged in for this id, then
        //    return
        File[] logFiles = getErrLogFiles(uniqueId, true);

        if ((logFiles == null) || (logFiles.length == 0))
            return true;

        return _clearLogs(uniqueId, logFiles, logger);
    }

    /**
     * Clear Out logs stored for a given Id
     *
     * @param uniqueId
     * @return
     */
    public boolean clearOutLogs(String uniqueId, Logger logger) {
        // Fixed N_3656
        // convert all ids to upper case as FMQ's
        // connection factory is in upper case.
        uniqueId = toUpperCase(uniqueId);

        // Check out the algo in clearErrLogs
        //

        File[] logFiles = getOutLogFiles(uniqueId, true);

        if ((logFiles == null) || (logFiles.length == 0))
            return true;

        return _clearLogs(uniqueId, logFiles, logger);
    }

    private FioranoLogHandler getHandler(int type, String uniqueId) {
        Logger logger = getLogger(uniqueId);

        Handler[] handlers = logger.getHandlers();

        for (int i = 0; i < handlers.length; i++) {
            if (!(handlers[i] instanceof FioranoLogHandler))
                continue;

            FioranoLogHandler logHandler = (FioranoLogHandler) handlers[i];

            if (type == logHandler.getLogType())
                return logHandler;
        }
        return null;
    }

    private Logger getLogger(String uniqueId) {
        uniqueId = uniqueId.toUpperCase();

        Logger parent = Logger.getLogger(ILogConstants.PARENT_LOGGER);
        Logger logger = parent.getLogger(uniqueId);

        return logger;
    }

    private File[] getErrLogFiles(String uniqueId, boolean inclueLckFiles) {
        // create file filter
        String[] posExt = new String[]{".err"};
        String[] negExt = null;

        if (!inclueLckFiles)
            negExt = new String[]{".lck"};

        FilenameFilter filter = new LogFileNameFilter(posExt, negExt);

        String dirName = getLogDirName(uniqueId);

        File dir = new File(dirName);

        if (!dir.exists())
            return null;

        return dir.listFiles(filter);
    }

    private File[] getOutLogFiles(String uniqueId, boolean inclueLckFiles) {
        // create file filter
        String[] posExt = new String[]{".out"};
        String[] negExt = null;

        if (!inclueLckFiles)
            negExt = new String[]{".lck"};

        FilenameFilter filter = new LogFileNameFilter(posExt, negExt);

        String dirName = getLogDirName(uniqueId);

        File dir = new File(dirName);

        if (!dir.exists())
            return null;

        return dir.listFiles(filter);
    }

    private String getOutFile(String uniqueId, Properties props) {
        return getLogFileName(uniqueId, props, ".out");
    }

    private String getErrFile(String uniqueId, Properties props) {
        return getLogFileName(uniqueId, props, ".err");
    }

    private String getLogFileName(String uniqueId, Properties props, String suffix) {
        String logDirName = getLogDirName(uniqueId, props);

        File logDir = new File(logDirName);

        if (!logDir.exists())
            logDir.mkdirs();

        String outFileName = normalize(uniqueId) + suffix;

        return logDirName + File.separator + outFileName;
    }

    private String getLogDirName(String uniqueId) {
        return getLogDirName(logManagerConfig.getLogDir(), uniqueId);
    }

    private String getLogDirName(String uniqueId, Properties props) {
        String propName = FileHandler.class.getName() + ".dir";
        String baseLogdir = props.getProperty(propName);

        // Default logs dir
        if ((baseLogdir == null) || (baseLogdir.trim().equalsIgnoreCase("")))
            baseLogdir = logManagerConfig.getLogDir();
        else if (!(Util.isAbsolutePath(baseLogdir))) {
            String runDir = System.getProperty(IBootConstants.FMQ_DB_PATH);

            if (runDir == null) {
                runDir = ".";
            }

            baseLogdir = runDir + "/" + baseLogdir;
        }

        return getLogDirName(baseLogdir, uniqueId);
    }

    private String getLogDirName(String baseLogdir, String uniqueId) {
        String handlerDir = uniqueId;

        if (handlerDir.indexOf("__") != -1)
            handlerDir = handlerDir.replaceAll("__", "/");

        String logDirName = baseLogdir + File.separator + handlerDir;

        return logDirName;
    }

    private String toUpperCase(String id) {
        if (id == null)
            return null;

        return id.toUpperCase();
    }

    private boolean _clearLogs(String uniqueId, File[] logFiles, Logger logger)
            throws SecurityException {
        // 2.  Close and remove the fileHandler from logger table
        //    (This will give write access to all log files)
        //
        ArrayList removedHandlers = _removeFioranoLoggers(uniqueId, logger);

        // 3. Clear the log files
        //
        boolean cleared = _clearLogs(logFiles);

        _addHandlers(uniqueId, removedHandlers, logger);

        return cleared;
    }

    /* This method updates the handler of InMemory component loggers*/
//    private void updateInMemComponentHandler(String uniqueId, FioranoOutHandler newOutHandler, FioranoErrorHandler newErrHandler)
//            throws SecurityException {
//        for (String loggerName : componentLoggerMap.get(uniqueId)) {
//            Logger logger = getLogger(loggerName);
//            FioranoLogHandler oldHandler = (FioranoLogHandler) logger.getHandlers()[0];
//            oldHandler.setOutLogHandler(newOutHandler);
//            oldHandler.setErrLogHandler(newErrHandler);
//        }
//        // update entries in fileHandlerMap
//        fileHandlerMap.get(uniqueId)[0] = newOutHandler;
//        fileHandlerMap.get(uniqueId)[1] = newErrHandler;
//    }

    private void _addHandlers(String uniqueId, ArrayList removedHandlers, Logger logger)
            throws SecurityException {
        Iterator iter = removedHandlers.iterator();

        while (iter.hasNext()) {
            FioranoLogHandler oldHandler = (FioranoLogHandler) iter.next();

            int type = oldHandler.getLogType();
            Level level = oldHandler.getLevel();
            Properties props = oldHandler.getProperties();

            try {
                FioranoLogHandler newHandler = _createHandler(type, props,
                        uniqueId);
                FioranoOutHandler newOutHandler = newHandler.getOutLogHandler();
                FioranoErrorHandler newErrHandler = newHandler.getErrLogHandler();

                oldHandler.setOutLogHandler(newOutHandler);
                oldHandler.setErrLogHandler(newErrHandler);
                oldHandler.setLevel(level);

//                if (componentLoggerMap.get(uniqueId) != null)        // If this uniqueId is present in componentLoggerMap , then its the case of InMemory Component Handler, so update the component's logger's handler
//                    updateInMemComponentHandler(uniqueId, newOutHandler, newErrHandler);

                if (logger == null) {
                    logger = getLogger(uniqueId);
                }

                logger.addHandler(oldHandler);
            } catch (IOException ex) {
                // Log and continue the good work
                ex.printStackTrace();
            }
        }
    }

    private ArrayList _removeFioranoLoggers(String uniqueId, Logger logger)
            throws SecurityException {
        ArrayList list = new ArrayList();
        if (logger == null) {
            logger = getLogger(uniqueId);
        }

        Handler[] handlers = logger.getHandlers();

        for (int i = 0; i < handlers.length; i++) {
            if (!(handlers[i] instanceof FioranoLogHandler))
                continue;

            logger.removeHandler(handlers[i]);
            handlers[i].flush();
            handlers[i].close();

            list.add(handlers[i]);
        }

        return list;
    }

    private boolean _clearLogs(File[] logFiles) {
        if ((logFiles == null) || logFiles.length == 0)
            return true;

        boolean deleted = true;

        for (int i = 0; i < logFiles.length; i++) {
            boolean result = logFiles[i].delete();
            deleted = deleted && result;
        }

        return deleted;
    }

    private FioranoLogHandler _createHandler(int type, Properties props, String uniqueId)
            throws IOException {
        FioranoLogHandler handler = null;
        String cname = null;
        boolean deleteOnExit = true;
        if(props.get("deleteOnExit") != null){
            deleteOnExit = "true".equalsIgnoreCase(props.getProperty("deleteOnExit"));
        }

        switch (type) {
            case ILogConstants.FILE_HANDLER_TYPE: {
                String outFile = getOutFile(uniqueId, props);
                String errorFile = getErrFile(uniqueId, props);

                cname = FileHandler.class.getName();

                Handler outFileHandler = null;
                Handler errFileHandler = null;
                FioranoOutHandler outHandler = null;
                FioranoErrorHandler errorHandler = null;

                try {
                    if (props.getProperty("IsInMemComponentLogger") == null) {
                        outFileHandler = _createFileHandler(outFile, deleteOnExit);
                        errFileHandler = _createFileHandler(errorFile, deleteOnExit);
                        outHandler = new FioranoOutHandler(outFileHandler, props);
                        errorHandler = new FioranoErrorHandler(errFileHandler, props);
                    } else {

                        boolean dirty = false;

                        if (fileHandlerMap.get(uniqueId) != null && fileHandlerMap.get(uniqueId)[0] != null) // if fileHandlerMap has outHandler then fetch it from there else create new outHandler
                        {
                            outHandler = (FioranoOutHandler) fileHandlerMap.get(uniqueId)[0];
                        } else {
                            outFileHandler = _createFileHandler(outFile, deleteOnExit);
                            outHandler = new FioranoOutHandler(outFileHandler, props);
                            dirty = true;
                        }
                        if (fileHandlerMap.get(uniqueId) != null && fileHandlerMap.get(uniqueId)[1] != null) // if fileHandlerMap has errorHandler then fetch it from there else create new errorHandler
                            errorHandler = (FioranoErrorHandler) fileHandlerMap.get(uniqueId)[1];
                        else {
                            errFileHandler = _createFileHandler(errorFile, deleteOnExit);
                            errorHandler = new FioranoErrorHandler(errFileHandler, props);
                            dirty = true;
                        }
                        if (dirty) {       // if new outHandler or errorHandler is created then replace the entry in fileHandlerMap
                            Handler[] fileHandlers = new Handler[2];
                            fileHandlers[0] = outHandler;
                            fileHandlers[1] = errorHandler;
                            fileHandlerMap.put(uniqueId, fileHandlers);
                        }

//                        if (props.getProperty("LoggerName") != null) // update the componentLoggerMap if LoggerName property is not null
//                        {
//                            if (componentLoggerMap.get(uniqueId) == null) {
//                                ArrayList<String> loggerList = new ArrayList<String>();
//                                loggerList.add(props.getProperty("LoggerName"));
//                                componentLoggerMap.put(uniqueId, loggerList);
//                            } else {
//                                if (!componentLoggerMap.get(uniqueId).contains(props.getProperty("LoggerName")))
//                                    componentLoggerMap.get(uniqueId).add(props.getProperty("LoggerName"));
//                            }
//                        }

                    }


                    handler = new FioranoLogHandler(outHandler, errorHandler);
                } catch (IOException exp) {
                    // Fixed N_5443.
                    //
                    // In case exception is thrown while creating file handler
                    // then close half created file handlers and throw back the exception
                    //
                    if (outFileHandler != null)
                        outFileHandler.close();

                    if (errFileHandler != null)
                        errFileHandler.close();

                    handler = null;

                    throw exp;
                }

                break;
            }
            case ILogConstants.CONSOLE_HANDLER_TYPE: {
                cname = ConsoleHandler.class.getName();

                Handler outConsoleHandler = _createConsoleHandler(props);
                Handler errConsoleHandler = _createConsoleHandler(props);

                FioranoOutHandler outHandler = new FioranoOutHandler(
                        outConsoleHandler, props);
                FioranoErrorHandler errHandler = new FioranoErrorHandler(
                        errConsoleHandler, props);

                handler = new FioranoLogHandler(outHandler, errHandler);

                break;
            }
            default: {
                String handlerClass = props.getProperty(ILogConstants.LOG_HANDLER, ILogConstants.LOG_HANDLER_DEF);
                Handler customHandler = null;
                try {
                    customHandler = (Handler) Class.forName(handlerClass).newInstance();
                } catch (InstantiationException e) {
                    return null;
                } catch (IllegalAccessException e) {
                    return null;
                } catch (ClassNotFoundException e) {
                    return null;
                }

                FioranoOutHandler outHandler = new FioranoOutHandler(customHandler, props);
                FioranoErrorHandler errHandler = new FioranoErrorHandler(customHandler, props);

                handler = new FioranoLogHandler(outHandler, errHandler);


            }
        }

        initialize(handler, props);
        return handler;
    }

    /**
     * Initialize the handler object
     *
     * @param handler
     * @param props
     */
    public void initialize(Handler handler, Properties props) {
        if (handler == null)
            return;

        Level level = logManagerConfig.getLogLevel();
        Formatter formatter = logManagerConfig.getFormatterObject();

        if (formatter instanceof DefaultFormatter)
            ((DefaultFormatter) formatter).configure(props);

        String encoding = logManagerConfig.getEncoding();

        handler.setLevel(level);
        handler.setFormatter(formatter);

        try {
            handler.setEncoding(encoding);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Create File Handler
     *
     * @param defaultFileName
     * @return
     * @throws SecurityException
     * @throws IOException
     */
    private Handler _createFileHandler(String defaultFileName, boolean deleteLockOnExit) throws SecurityException, IOException {

        int limit = logManagerConfig.getFileSizeLimit();
        int count = logManagerConfig.getFileCount();

        Handler handler = new FileHandler(defaultFileName, limit, count, true);
        if(deleteLockOnExit){
            deleteLckOnExist(defaultFileName, count);
        }else{
            if(defaultFileName != null){
                for (int i = 0; i < count; i++) {
                    try {
                        File file = new File(defaultFileName + "." + i + ".lck");

                    } catch (Throwable ex) {
                        // Ignore the exception
                    }
                }
            }
        }
        return handler;
    }

    private void deleteLckOnExist(String actualPattern, int count) {
        if (actualPattern == null)
            return;

        deleteOnExist(actualPattern + ".lck");

        for (int i = 0; i < count; i++) {
            deleteOnExist(actualPattern + "." + i + ".lck");
        }
    }

    private void deleteOnExist(String fileName) {
        try {
            File file = new File(fileName);

            file.deleteOnExit();
        } catch (Throwable ex) {
            // Ignore the exception
        }
    }

    /**
     * Create console Handler
     *
     * @param props
     * @return
     * @throws SecurityException
     * @throws IOException
     */
    private Handler _createConsoleHandler(Properties props)
            throws SecurityException, IOException {
        Handler handler = new ConsoleHandler();

        return handler;
    }

    private String normalize(String uniqueId) {
        uniqueId = uniqueId.replace('/', '_');
        uniqueId = uniqueId.replace('\\', '_');
        uniqueId = uniqueId.replace('.', '_');

        return uniqueId;
    }

    /**
     * <p><strong> </strong> represents </p>
     *
     * @author FSIPL
     * @version 1.0
     * @created June 22, 2005
     */
    class LogFileNameFilter
            implements FilenameFilter {
        private String[] m_positveExts;
        private String[] m_negativeExts;

        /**
         * @param positiveExt
         * @param negativeExt
         */
        public LogFileNameFilter(String[] positiveExt, String[] negativeExt) {
            m_positveExts = positiveExt;
            m_negativeExts = negativeExt;
        }

        /**
         * Tests if a specified file should be included in a file list.
         *
         * @param dir  the directory in which the file was found.
         * @param name the name of the file.
         * @return <code>true</code> if and only if the name should be
         * included in the file list; <code>false</code> otherwise.
         */
        public boolean accept(File dir, String name) {
            if (!matchPositive(name))
                return false;

            if (matchNegative(name))
                return false;

            return true;
        }

        private boolean matchNegative(String name) {
            if ((m_negativeExts == null) || m_negativeExts.length == 0)
                return false;

            for (int i = 0; i < m_negativeExts.length; i++) {
                int index = name.indexOf(m_negativeExts[i]);

                if (index != -1)
                    return true;
            }

            return false;
        }

        private boolean matchPositive(String name) {
            if ((m_positveExts == null) || m_positveExts.length == 0)
                return true;

            for (int i = 0; i < m_positveExts.length; i++) {
                int index = name.indexOf(m_positveExts[i]);

                if (index != -1)
                    return true;
            }

            return false;
        }
    }

    public void addLogger(String loggerName, Logger logger) {
        componentLoggerMap.put(loggerName, logger);
    }
}
