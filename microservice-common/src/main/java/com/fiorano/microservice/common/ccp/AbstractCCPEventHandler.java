/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

import com.fiorano.openesb.microservice.ccp.event.CCPEventType;
import com.fiorano.openesb.microservice.ccp.event.ControlEvent;
import com.fiorano.openesb.microservice.ccp.event.EventFactory;
import com.fiorano.openesb.microservice.ccp.event.common.DataEvent;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.Data;
import com.fiorano.openesb.microservice.ccp.event.common.data.MemoryUsage;
import com.fiorano.openesb.microservice.ccp.event.common.data.ProcessID;
import com.fiorano.openesb.microservice.ccp.event.component.HandShakeAckEvent;
import com.fiorano.openesb.microservice.ccp.event.component.StatusEvent;
import com.fiorano.openesb.microservice.ccp.event.peer.CommandEvent;
import com.fiorano.openesb.microservice.ccp.event.peer.HandShakeEvent;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.JavaUtil;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Runtime.getRuntime;

public abstract class AbstractCCPEventHandler implements MessageListener, ICCPEventHandler {

    public static final double CCP_VERSION = 1.0;

    protected Timer timer;
    protected Logger logger;
    protected ICCPEventGenerator ccpEventGenerator;

    public AbstractCCPEventHandler(ICCPEventGenerator ccpEventGenerator, Logger logger) {
        this.ccpEventGenerator = ccpEventGenerator;
        this.logger = logger;
    }

    /**
     * Acknowledges the messages from the peer server. Converts them to Events and calls handleEvent.
     */
    public void onMessage(Message message) {
        try {
            ((BytesMessage) message).reset();
            message.acknowledge();
            CCPEventType eventType = CCPEventType.valueOf(message.getStringProperty(ControlEvent.EVENT_TYPE_HEADER));
            ControlEvent event = EventFactory.getEvent(eventType);
            event.fromMessage((BytesMessage) message);
            handleEvent(event);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_HANDLING_CCP_MESSAGE, new String[]{e.getMessage()}), e);
        }
    }

    /**
     * Stops the actions that are being carried out by the event handler. Any resources that are being consumed.
     * are cleaned up.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Handles the event sent from the peer server to component.
     *
     * @param event Event sent from the peer server.
     */
    public void handleEvent(ControlEvent event) {
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RECEIVED_EVENT, new Object[]{event}));
        switch (event.getEventType()) {
            case HANDSHAKE_INITIATE:
                performHandshake((HandShakeEvent) event);
                break;
            case COMMAND:
                handleCommand((CommandEvent) event);
                break;
            case DATA_REQUEST:
                handleDataRequest((DataRequestEvent) event);
                break;
        }
    }

    protected void performHandshake(HandShakeEvent handShakeEvent) {
        HandShakeAckEvent ackEvent = new HandShakeAckEvent();
        ackEvent.setReplyNeeded(false);
        ackEvent.setCorrelationID(handShakeEvent.getEventId());
        ackEvent.setMaxVersionSupported(CCP_VERSION);
        ackEvent.setMinVersionSupported(CCP_VERSION);
        ackEvent.setCcpSupported(CCP_VERSION >= handShakeEvent.getMinVersionSupported() && CCP_VERSION <= handShakeEvent.getMaxVersionSupported());
        ackEvent.setComment("Handshake Acknowledgement");
        if (ccpEventGenerator != null) {
            ccpEventGenerator.sendEvent(ackEvent);
        }
    }

    protected void handleDataRequest(final DataRequestEvent dataRequestEvent) {
        DataTimerTask timerTask = new DataTimerTask(dataRequestEvent);
        if (timer == null) {
            timer = new Timer("CCP-Data handler-" + getComponentID());
        }
        if (dataRequestEvent.getInterval() <= 0) {
            timer.schedule(timerTask, 0);
        } else {
            timer.schedule(timerTask, 0, dataRequestEvent.getInterval());
        }
    }

    protected abstract String getComponentID();

    protected abstract void handleLogLevelEvent(DataRequestEvent event, DataEvent dataEvent);

    protected void handleCommand(CommandEvent event) {

        switch (event.getCommand()) {
            case REPORT_STATE:
                StatusEvent serviceStatus = new StatusEvent();
                serviceStatus.setCorrelationID(event.getCorrelationID());
                if (ccpEventGenerator != null) {
                    ccpEventGenerator.sendEvent(serviceStatus);
                }
                serviceStatus.setCorrelationID(0);
                break;
        }
    }

    private class DataTimerTask extends TimerTask {
        private DataRequestEvent event;
        private int counter = 0;

        private DataTimerTask(DataRequestEvent event) {
            this.event = event;
        }

        public void run() {
            long count = event.getRepetitionCount();
            if (count >= 0 && counter++ == count) {
                cancel();
                return;
            }
            DataEvent dataEvent = (DataEvent) EventFactory.getEvent(CCPEventType.DATA);
            dataEvent.setCorrelationID(event.getEventId());
            for (DataRequestEvent.DataIdentifier dataIdentifier : event.getDataIdentifiers()) {
                switch (dataIdentifier) {
                    case MEMORY_USAGE:
                        MemoryUsage data = (MemoryUsage) Data.getDataObject(Data.DataType.COMPONENT_MEMORY_USAGE);
                        data.setHeapMemoryAllocated(getRuntime().maxMemory());
                        data.setHeapMemoryUsed(getRuntime().totalMemory() - getRuntime().freeMemory());
                        long nonHeapUsedMemory = (ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed());
                        long nonHeapMaxMemory = (ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getMax());
                        data.setNonHeapMemoryAllocated(nonHeapMaxMemory - nonHeapUsedMemory);
                        data.setNonHeapMemoryUsed(nonHeapUsedMemory);
                        dataEvent.getData().put(dataIdentifier, data);
                        break;
                    case LOG_LEVELS:
                        handleLogLevelEvent(event, dataEvent);
                        break;
                    case PID:
                        ProcessID processID = (ProcessID) Data.getDataObject(Data.DataType.PID);
                        try {
                            processID.setValue(JavaUtil.getPID());
                        } catch (Throwable e) {
                            processID.setValue("Error in getting PID");
                            logger.log(Level.WARNING, "Error in getting PID");
                        }
                        dataEvent.getData().put(dataIdentifier, processID);
                        break;
                }
            }
            if (ccpEventGenerator != null) {
                ccpEventGenerator.sendEvent(dataEvent);
            }
        }
    }
}
