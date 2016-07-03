package com.fiorano.microservice.common.log;

import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.util.StringUtil;

import java.io.File;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {

    static String JNDI_CONSTANT = "__";
    static String LOG_NAME_SEPARATOR = ".";

    //loggers expected in the form of logger1=level1,logger2=level2...
    public static Map<String, String> getLogLevels(String loggerParam) {

        Map<String, String> logLevels = new HashMap<>();
        if (StringUtil.isEmpty(loggerParam)) {
            return logLevels;
        }

        StringTokenizer tokenizer = new StringTokenizer(loggerParam, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int index = token.indexOf("=");
            logLevels.put(token.substring(0, index), token.substring(index + 1, token.length()));
        }

        return logLevels;
    }

    //log manager details expected in the form of loggerType=FileHandler,directory=logs...
    public static LogManagerConfig getLogManagerConfig(String logManagerParam) {

        LogManagerConfig config = new LogManagerConfig();

        StringTokenizer tokenizer = new StringTokenizer(logManagerParam, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int index = token.indexOf("=");
            String key = token.substring(0, index);
            String value = token.substring(index + 1, token.length());
            switch (key) {
                case "loggerClass":
                    config.setLogType(value);
                    break;
                case "java.util.logging.FileHandler.dir":
                    config.setLogDir(value);
                    break;
                case "java.util.logging.FileHandler.limit":
                    config.setFileSizeLimit(Integer.valueOf(value));
                    break;
                case "java.util.logging.FileHandler.count":
                    config.setFileCount(Integer.valueOf(value));
                    break;
                case "fiorano.jms.log2.def.DefaultFormatter.includetimestamp":
                    config.setIncludeTimeStamp(Boolean.valueOf(value));
                    break;
                case "fiorano.jms.log2.def.DefaultFormatter.dateformat":
                    config.setTimeStampFormat(value);
                    break;
            }
        }

        return config;
    }

    public static String getLogPath(LogManagerConfig logManagerConfig) {
        return new StringBuilder().append(System.getProperty("FIORANO_HOME")).append(File.separator).append("data")
                .append(File.separator).append(logManagerConfig.getLogDir()).toString();
    }

    public static String getLogPath(String userHome, LogManagerConfig logManagerConfig) {
        return new StringBuilder().append((userHome == null ? System.getProperty("FIORANO_HOME") : userHome)).append(File.separator).append("data")
                .append(File.separator).append(logManagerConfig.getLogDir()).toString();
    }

    public static LogManager createLogHandlers(CommandLineParameters commandLineParameters) throws Exception {

        Map<String, String> logLevels = getLogLevels((String) commandLineParameters.getParameter("-loggers"));
        LogManagerConfig logManagerConfig = getLogManagerConfig((String) commandLineParameters.getParameter("-logManager"));
        String logPath = getLogPath(logManagerConfig);
        logManagerConfig.setLogDir(logPath);
        String logId = new StringBuilder(commandLineParameters.getApplicationName()).append("__")
                .append(commandLineParameters.getApplicationVersion()).append("__")
                .append(commandLineParameters.getServiceInstanceName()).toString();
        int logHandlerType = "java.util.logging.FileHandler".equals(logManagerConfig.getLogType()) ? 1 : 2;

        LogManager logManager = new LogManager();
        logManager.configure(logManagerConfig);

        Set<String> loggers = logLevels.keySet();
        Properties properties = new Properties();
        properties.put("deleteOnExit", !commandLineParameters.isInmemoryLaunchable() + "");
        Handler handler = logManager.createHandler(logHandlerType, logId, properties);
        handler.setLevel(Level.ALL);
        for (String loggerName : loggers) {
//            Logger logger = Logger.getLogger(loggerName.toUpperCase());
            String fullLoggerName = getServiceLoggerName(loggerName, commandLineParameters.getApplicationName(), String.valueOf(commandLineParameters.getApplicationVersion()), commandLineParameters.getServiceGUID(), commandLineParameters.getServiceInstanceName());
            Logger logger = Logger.getLogger(fullLoggerName);
            logger.addHandler(handler);
            logger.setLevel(Level.parse(logLevels.get(loggerName)));
//            fiorano.esb.util.LoggerUtil.addFioranoConsoleHandler(logger);
            logger.setUseParentHandlers(false);
            logManager.addLogger(fullLoggerName, logger);
        }

        return logManager;
    }



    public static LogManager createLogHandlers(String logeLevelsParam, String logManagerParam, String instanceId, String guid) throws Exception {
        return createLogHandlers(logeLevelsParam, logManagerParam, instanceId, guid, true, null);
    }

    public static LogManager createLogHandlers(String logeLevelsParam, String logManagerParam, String instanceId, String guid, boolean deleteOnExit) throws Exception {
        return createLogHandlers(logeLevelsParam, logManagerParam, instanceId, guid, deleteOnExit, null);
    }
    public static LogManager createLogHandlers(String logeLevelsParam, String logManagerParam, String instanceId, String guid, boolean deleteOnExit, String homeDir) throws Exception {

        Map<String, String> logLevels = getLogLevels(logeLevelsParam);
        LogManagerConfig logManagerConfig = getLogManagerConfig(logManagerParam);
        String logPath = getLogPath(homeDir, logManagerConfig);
        logManagerConfig.setLogDir(logPath);
        String appVersion = getAppVersionString(instanceId);
        if(appVersion != null){
            appVersion = appVersion.replace("_",".");
        }
        String logId = new StringBuilder(getApplicationGUID(instanceId)).append("__")
                .append(appVersion).append("__")
                .append(getServiceInstanceName(instanceId)).toString();
        int logHandlerType = "java.util.logging.FileHandler".equals(logManagerConfig.getLogType()) ? 1 : 2;

        LogManager logManager = new LogManager();
        logManager.configure(logManagerConfig);

        Set<String> loggers = logLevels.keySet();
        Properties properties = new Properties();
        properties.put("deleteOnExit", deleteOnExit + "");
        Handler handler = logManager.createHandler(logHandlerType, logId, properties);
        handler.setLevel(Level.ALL);
        for (String loggerName : loggers) {
//            Logger logger = Logger.getLogger(loggerName.toUpperCase());
            String fullLoggerName = getServiceLoggerName(loggerName, instanceId, guid);
            Logger logger = Logger.getLogger(fullLoggerName);
            logger.addHandler(handler);
            logger.setLevel(Level.parse(logLevels.get(loggerName)));
//            fiorano.esb.util.LoggerUtil.addFioranoConsoleHandler(logger);
            logger.setUseParentHandlers(false);
            logManager.addLogger(fullLoggerName, logger);
        }

        return logManager;
    }


    /**
     * returns logger created using instance based logger name for a given logger name
     *
     * @param loggerName - Logger name mentioned in the Service Descriptor
     * @param instanceId - ServiceInstance ID
     * @param serviceGUID - Service's GUID
     * @return instance based logger name for service if all parameters are not empty and connFactoryName contains __ else the logger name as it is
     */
    public static Logger getServiceLogger(String loggerName, String instanceId, String serviceGUID) {
        return Logger.getLogger(getServiceLoggerName(loggerName, instanceId, serviceGUID));
    }

    /**
     * returns instance based logger name for a given logger
     *
     * @param loggerName - Logger name mentioned in the Service Descriptor
     * @param instanceId - service instance ID
     * @param serviceGUID - Service's GUID
     * @return instance based logger name for service if all parameters are not empty and connFactoryName contains __ else the logger name as it is
     */
    public static String getServiceLoggerName(String loggerName, String instanceId, String serviceGUID) {
        String appGUID=getApplicationGUID(instanceId);
        String serviceInstanceName=getServiceInstanceName(instanceId);
        String appVersionString = getAppVersionString(instanceId);
        return getServiceLoggerName(loggerName, appGUID, appVersionString, serviceGUID, serviceInstanceName);
    }

    /**
     * Returns the Application Instance Name
     */
    private static String getApplicationGUID(String connFactoryName) {
        if (connFactoryName != null && connFactoryName.indexOf(JNDI_CONSTANT) != -1) {
            return getPrefix(connFactoryName, JNDI_CONSTANT);
        } else {
            return null;
        }
    }

    /**
     * Returns the Application Instance Name
     */
    private static String getAppVersionString(String connFactoryName) {
        if (connFactoryName != null && connFactoryName.indexOf(JNDI_CONSTANT) != -1) {
            String conFacWithOutAppGuid = getSuffix(connFactoryName, JNDI_CONSTANT);
            return getPrefix(conFacWithOutAppGuid, JNDI_CONSTANT);
        } else {
            return null;
        }
    }

    /**
     * Returns the Service Instance Name
     */
    private static String getServiceInstanceName(String connFactoryName) {
        if (connFactoryName != null && connFactoryName.indexOf(JNDI_CONSTANT) != -1) {
            String conFacWithOutAppGuid = getSuffix(connFactoryName, JNDI_CONSTANT);
            return getSuffix(conFacWithOutAppGuid, JNDI_CONSTANT);
        } else {
            return null;
        }
    }




    /**
     * returns logger created using instance based logger name for a given logger name
     *
     * @param loggerName - Logger name mentioned in the Service Descriptor
     * @param commandLineParameters - Command Line Parameters
     * @return instance based logger name for service if all the parameters are not empty else the logger name as it is
     */
    public static Logger getServiceLogger(String loggerName, CommandLineParameters commandLineParameters) {
        return Logger.getLogger(getServiceLoggerName(loggerName, commandLineParameters.getApplicationName(), String.valueOf(commandLineParameters.getApplicationVersion()), commandLineParameters.getServiceGUID(), commandLineParameters.getServiceInstanceName()));
    }

    /**
     * returns logger created using instance based logger name for a given logger name
     *
     * @param loggerName - Logger name mentioned in the Service Descriptor
     * @param appGUID - Application's GUID
     * @param serviceGUID - Service's GUID
     * @param serviceInstanceName - Service's instance name
     * @return instance based logger name for service if all the parameters are not empty else the logger name as it is
     */
    public static Logger getServiceLogger(String loggerName, String appGUID, String appVersion, String serviceGUID,
                                          String serviceInstanceName) {
        return Logger.getLogger(getServiceLoggerName(loggerName, appGUID, appVersion, serviceGUID, serviceInstanceName));
    }

    /**
     * returns instance based logger name for a given logger name
     *
     * @param loggerName - Logger name mentioned in the Service Descriptor
     * @param appGUID - Application's GUID
     * @param serviceGUID - Service's GUID
     * @param serviceInstanceName - Service's instance name
     * @return instance based logger name for service if all the parameters are not empty else the logger name as it is
     */
    public static String getServiceLoggerName(String loggerName, String appGUID, String appVersion, String serviceGUID,
                                              String serviceInstanceName) {
        if (StringUtil.isEmpty(loggerName) || StringUtil.isEmpty(appGUID) || StringUtil.isEmpty(serviceGUID)
                || StringUtil.isEmpty(serviceInstanceName) || StringUtil.isEmpty(appVersion)) {
            return loggerName;
        }
        String logName = appGUID + JNDI_CONSTANT + appVersion.replace(".", "_") + LOG_NAME_SEPARATOR + serviceGUID + LOG_NAME_SEPARATOR +
                         serviceInstanceName + LOG_NAME_SEPARATOR + loggerName;
        return logName.toUpperCase();

    }


    /**
     * Setting the log level on handlers. In Fiorano, there will be only one handler at all times usually. Do not the
     * level on the logger. When CCP is not used, the log level changes are handled in the MQ layer and the level is set
     * on the handler. So if log level on logger is set to a value for lesser logging, those logs will not be seen even when
     * logging is increased.
     * @param logger
     * @param level
     */
    public static void setLevel(Logger logger, Level level) {
        logger.setLevel(level);
        for(Handler handler : logger.getHandlers()) {
            handler.setLevel(level);
        }
    }

    /**
     * Just returning the level of lone handler used by components.
     * See #setLevel(Logger,Level) for explanation.
     *
     * @param logger
     * @return
     */
    public static Level getLevel(Logger logger) {

        Handler[] handlers = logger.getHandlers();
        return handlers.length != 0 ? handlers[0].getLevel() : logger.getLevel();
    }

    private static String getSuffix(String givenString, String searchString) {
        return givenString.substring(givenString.indexOf(searchString) + searchString.length());
    }

    private static String getPrefix(String givenString, String searchString) {
        return givenString.substring(0, givenString.indexOf(searchString));
    }

}
