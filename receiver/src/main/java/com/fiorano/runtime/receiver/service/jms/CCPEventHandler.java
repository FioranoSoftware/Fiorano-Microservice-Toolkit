/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.runtime.receiver.service.jms;


import com.fiorano.microservice.common.ccp.AbstractCCPEventHandler;
import com.fiorano.microservice.common.ccp.Bundle;
import com.fiorano.microservice.common.ccp.ICCPEventGenerator;
import com.fiorano.openesb.microservice.ccp.event.ControlEvent;
import com.fiorano.openesb.microservice.ccp.event.common.DataEvent;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.LogLevelRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.Data;
import com.fiorano.openesb.microservice.ccp.event.common.data.LogLevel;
import com.fiorano.openesb.microservice.ccp.event.peer.CommandEvent;


import com.fiorano.runtime.receiver.service.ReceiverCommandLineParams;
import com.fiorano.runtime.receiver.service.ReceiverService;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.microservice.common.log.LoggerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The CCPEventHandler listens to the messages that are published on the CCP_PEER_TO_COMPONENT_TRANSPORT,
 * converts them into control events and responds to the events.
 * <p/>
 * Date: Feb 28, 2010
 * Time: 7:23:47 PM
 */
public class CCPEventHandler extends AbstractCCPEventHandler {

    private final ReceiverService service;

    public CCPEventHandler(ReceiverService service, ICCPEventGenerator ccpEventGenerator, Logger logger) {
        super(ccpEventGenerator, logger);
        this.service = service;
       // service.getServiceState().addPropertyChangeListener(new ServiceStateListener(ccpEventGenerator, logger));
    }

    /**
     * Handles the event sent from the peer server to component.
     *
     * @param event Event sent from the peer server.
     */
    public void handleEvent(ControlEvent event) {
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RECEIVED_EVENT, new Object[]{event}));
        switch (event.getEventType()) {
            case DATA:
                Map<DataRequestEvent.DataIdentifier, Data> dataMap = ((DataEvent) event).getData();
              //  service.updateConfiguration(dataMap);
                synchronized (service) {
                    service.notifyAll();
                }
                break;
            default:
                super.handleEvent(event);
                break;
        }
    }

    protected void handleCommand(CommandEvent event) {
        switch (event.getCommand()) {
            case INITIATE_SHUTDOWN:
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RECEIVED_SHUTDOWN_COMMAND,
                        new Object[]{event.getCorrelationID()}));
                service.stop();
                break;
            case SET_LOGLEVEL:
                for (Map.Entry<String, String> logLevelEntry : event.getArguments().entrySet()) {
                    ReceiverCommandLineParams receiverCommandLineParams = service.getReceiverParams();
                    Logger logger = LoggerUtil.getServiceLogger(logLevelEntry.getKey(),receiverCommandLineParams.getConnectionFactory(),
                            receiverCommandLineParams.getServiceGUID());
                    LoggerUtil.setLevel(logger, Level.parse(logLevelEntry.getValue()));
                }
                break;
            case CLEAR_OUT_LOGS:
                service.clearOutLogs();
                break;
            case CLEAR_ERR_LOGS:
                service.clearErrLogs();
                break;
            default:
                super.handleCommand(event);
                break;
        }
    }

    @Override
    protected String getComponentID() {
        return service.getServiceLookupName();
    }

    @Override
    protected void handleLogLevelEvent(DataRequestEvent event, DataEvent dataEvent) {

        LogLevel level = (LogLevel) Data.getDataObject(Data.DataType.LOG_LEVEL);
        HashMap<String, Level> logLevels = new HashMap<>();
        if (event instanceof LogLevelRequestEvent) {
            for (String loggerName : ((LogLevelRequestEvent) event).getLoggerNames()) {
                ReceiverCommandLineParams receiverCommandLineParams = service.getReceiverParams();
                Logger logger = LoggerUtil.getServiceLogger(loggerName, receiverCommandLineParams.getConnectionFactory(),
                        receiverCommandLineParams.getServiceGUID());
                logLevels.put(loggerName, LoggerUtil.getLevel(logger));
            }
        } else {
            logLevels.put(service.getLogger().getName(), LoggerUtil.getLevel(service.getLogger()));
        }
        level.setLoggerLevels(logLevels);
        dataEvent.getData().put(DataRequestEvent.DataIdentifier.LOG_LEVELS, level);
    }
}
