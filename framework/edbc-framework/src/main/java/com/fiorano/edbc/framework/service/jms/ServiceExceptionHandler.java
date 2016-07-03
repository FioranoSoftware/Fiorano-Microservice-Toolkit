/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.AbstractService;
import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.configuration.AbstractErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.engine.AbstractServiceEngine;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.RetryAction;
import com.fiorano.edbc.framework.service.exception.RetryConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.esb.common.service.ServiceState;
import com.fiorano.fw.error.FrameWorkException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.ExceptionUtil;
import com.fiorano.util.StringUtil;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Date: Mar 9, 2007
 * Time: 2:49:14 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ServiceExceptionHandler implements ExceptionListener {
    protected AbstractErrorHandlingConfiguration errorHandlingConfiguration;
    protected AbstractService service;
    protected EventGenerator eventGenerator;
    protected Session eventSession;
    protected Logger logger;

    public ServiceExceptionHandler(AbstractService service, fiorano.esb.util.EventGenerator eventGenerator, Session eventSession) {
        if (service.getConfiguration() != null) {
            this.errorHandlingConfiguration = service.getConfiguration().getErrorHandlingConfiguration();
        }
        this.logger = service.getLogger();
        this.eventGenerator = new EventGenerator(eventGenerator.getProducer(), eventGenerator.getErrorDestination(), eventGenerator.getEventDestination());
        this.eventSession = eventSession;
        this.service = service;
    }

    public boolean handleException(ServiceExecutionException exception, Message requestMessage, IMessageHandler messageHandler) {
        if (errorHandlingConfiguration != null) {
            Collection errorHandlingActions = errorHandlingConfiguration.getActions(exception.getErrorID());
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_OCCURRED, new String[]{exception.getLocalizedMessage()}));
            return performErrorHandlingActions(errorHandlingActions, messageHandler, requestMessage, exception);
        }
        return false;
    }

    protected boolean performErrorHandlingActions(Collection errorHandlingActions, IMessageHandler messageHandler, Message requestMessage,
                                                  ServiceExecutionException exception) {

        if (service.getServiceState().getState() == ServiceState.State.STOPPING || service.getServiceState().getState() == ServiceState.State.STOPPED) {
            return true;
        }

        if (errorHandlingActions != null) {
            for (Iterator iterator = errorHandlingActions.iterator(); iterator.hasNext(); ) {
                ErrorHandlingAction action = (ErrorHandlingAction) iterator.next();
                if (action.isEnabled()) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PERFORMING_ERROR_HANDLING, new String[]{action.getName()}));
                    switch (action.getId()) {
                        case ErrorHandlingAction.RETRY_EXECUTION:
                            if (action instanceof RetryAction) {
                                if (retry(((RetryAction) action).getConfiguration(), messageHandler, requestMessage, false)) {
                                    return true;
                                }
                            }
                            break;
                        case ErrorHandlingAction.RECONNECT:
                            if (action instanceof RetryAction) {
                                if (retry(((RetryAction) action).getConfiguration(), messageHandler, requestMessage, true)) {
                                    return true;
                                }
                            }
                            break;
                        case ErrorHandlingAction.THROW_ERROR_ON_WARNING:
                            break;
                        case ErrorHandlingAction.STOP_SERVICE:
                            stopService();
                            break;
                        case ErrorHandlingAction.PROCESS_INVALID_REQUEST:
                            //do nothing
                            break;
                        case ErrorHandlingAction.LOG:
                            if (logger != null) {
                                logger.log(Level.SEVERE, exception.getMessage(), exception);
                            }
                            break;
                        case ErrorHandlingAction.DISCARD_CONNECTION:
                            discardConnection();
                            break;
                        case ErrorHandlingAction.SEND_TO_ERROR_PORT:
                        default:
                            if (noRetryActions(errorHandlingActions)) {
                                sendError(exception, requestMessage);
                            }
                    }
                }
//            else {
//                if (action.getId() == ErrorHandlingAction.PROCESS_INVALID_REQUEST) {
//                    throw new RuntimeException(exception.getMessage(), exception);
//                }
//            }
            }
        }
        return false;
    }

         /*
      * This method will return false, if no retry actions are present. So that error can be sent to error port, and
      * if returns true, error will be sent to error port.
      * Fix for Bug 24969 - Desc : Sends to error port irrespective of retry actions configured.
     */

    private boolean noRetryActions(Collection errorHandlingActions) {
        if (errorHandlingActions != null) {
            for (Object errorHandlingAction : errorHandlingActions) {
                ErrorHandlingAction action = (ErrorHandlingAction) errorHandlingAction;
                if (action.isEnabled()) {
                    switch (action.getId()) {
                        case ErrorHandlingAction.RETRY_EXECUTION:
                            if (action instanceof RetryAction) {
                                return false;
                            }
                            break;
                        case ErrorHandlingAction.RECONNECT:
                            if (action instanceof RetryAction) {
                                return false;
                            }
                            break;
                    }
                }
            }
        }
        return true;
    }


    private void discardConnection() {

        try {
            if (service != null && service.getEngine() != null && service.getEngine().getConnection() != null) {
                service.getEngine().getConnection().destroy();
            }
        } catch (ServiceExecutionException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.DISCARD_CONNECTION_FAILED));
        }
    }

    public void sendError(Exception exception, Message msg) {
        try {
            if (msg != null && !StringUtil.isEmpty(msg.getStringProperty(Constants.COMPONENT_IN_TIME))) {
                long inTime = Long.parseLong(msg.getStringProperty(Constants.COMPONENT_IN_TIME));
                msg.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, String.valueOf(System.currentTimeMillis() - inTime));
            }
            String message;
            Throwable throwable = ExceptionUtil.getCause(exception);
            if (throwable == null) {
                message = exception.getMessage();
            } else {
                message = throwable.getMessage();
            }
            if (exception instanceof ServiceExecutionException) {
                ServiceExecutionException serviceExecutionException = (ServiceExecutionException) exception;
                eventGenerator.sendError(serviceExecutionException.getErrorID().getName(),
                        message, exception, msg, eventSession.createTextMessage());
            } else {
                eventGenerator.sendError(message, exception, msg, eventSession.createTextMessage());
            }
        } catch (FrameWorkException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_ERROR_FAILED, new Object[]{e.getMessage()}), e);
        } catch (JMSException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_ERROR_FAILED, new Object[]{e.getMessage()}), e);
        }
    }

    protected Map populateOtherActionsByCount(RetryConfiguration retryConfiguration) {
        Map otherActionsByCount = new Hashtable();
        if (retryConfiguration == null) {
            return otherActionsByCount;
        }
        Map otherActionsMap = retryConfiguration.getOtherActions();
        if (otherActionsMap == null || otherActionsMap.isEmpty()) {
            return otherActionsByCount;
        }
        for (Object o : otherActionsMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Collection actions = (Collection) otherActionsByCount.get(entry.getValue());
            if (actions == null) {
                actions = new ArrayList();
            }
            actions.add(entry.getKey());
            otherActionsByCount.put(entry.getValue(), actions);
        }
        return otherActionsByCount;
    }

    protected boolean retry(RetryConfiguration retryConfiguration, IMessageHandler messageHandler, Message message, boolean reconnect) {
        if (service != null) {
            Map otherActionsByCount = populateOtherActionsByCount(retryConfiguration);

            for (int index = 0; retryConfiguration.getRetryCount() == -1 || index < retryConfiguration.getRetryCount(); ) {

                if (service.getServiceState().getState() == ServiceState.State.STOPPING || service.getServiceState().getState() == ServiceState.State.STOPPED) {
                    return true;
                }

                index++;
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RETRY_COUNT, new String[]{String.valueOf(index)}));
                if (!service.isStarted()) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.STOPPING_ERROR_HANDLING));
                    break;
                }
                try {
                    if (service.isInMemory()) {
                        long sleepTime = 0;
                        while (sleepTime < retryConfiguration.getRetryInterval()
                                && !(service.getServiceState().getState() == ServiceState.State.STOPPING
                                || service.getServiceState().getState() == ServiceState.State.STOPPED)) {
                            long time = (retryConfiguration.getRetryInterval() - sleepTime) > 5000 ? 5000L : (retryConfiguration.getRetryInterval() - sleepTime);
                            if (time > 0)
                                Thread.sleep(time);
                            sleepTime = sleepTime + time;
                        }
                    } else {
                        Thread.sleep(retryConfiguration.getRetryInterval());
                    }
                } catch (InterruptedException e) {
                    //do nothing
                }
                try {
                    if (reconnect && service.getEngine() != null) {
                        AbstractServiceEngine engine = service.getEngine();
                        engine.setConnection(engine.createConnection());
                    }
                    messageHandler.handleMessage(message);
                    return true;
                } catch (ServiceExecutionException e) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_OCCURRED, new String[]{e.getLocalizedMessage()}));
                    Collection<ErrorHandlingAction> actions = new ArrayList<>();
                    Set<Map.Entry<Integer, List<ErrorHandlingAction>>> entrySet = otherActionsByCount.entrySet();
                    for (Map.Entry<Integer, List<ErrorHandlingAction>> entry : entrySet) {
                        if (!(entry.getKey() == 0)) {
                            if (index % entry.getKey() == 0) {
                                actions.addAll(entry.getValue());
                            }
                        }
                    }
                    performErrorHandlingActions(actions, messageHandler, message, e);
                }
            }
        }
        return false;
    }

    protected void stopService() {
        service.stop();
    }

    public void onException(JMSException jmsException) {
        stopService();
    }

    public boolean isInMemory() {
        return service.getCommandLineParams().isInmemoryLaunchable();
    }
}