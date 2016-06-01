/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.error;

import com.fiorano.edbc.framework.service.configuration.AbstractErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.connection.IConnection;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.RetryAction;
import com.fiorano.edbc.framework.service.exception.RetryConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.connection.IConnectionManager;
import com.fiorano.edbc.framework.service.internal.transport.IErrorTransport;
import com.fiorano.edbc.framework.service.internal.transport.IRequestListener;
import com.fiorano.services.common.util.RBUtil;

import java.util.*;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Aug 4, 2009
 * Time: 12:40:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class ErrorHandler<R> extends Module implements IErrorHandler<R> {

    private AbstractErrorHandlingConfiguration errorHandlingConfiguration;
    private IErrorTransport errorTransport;
    private IConnectionManager connectionManager;
    private IService service;

    public ErrorHandler(IModule parent, IService service, IErrorTransport errorTransport) {
        super(parent);
        this.service = service;
        this.errorTransport = errorTransport;
        this.logger = service.getLogger();
    }

    //---------------------------------------------[IModule API]----------------------------------------------------
    public void internalCreate() throws ServiceExecutionException {
        super.internalCreate();
        this.errorHandlingConfiguration = service.getConfiguration().getErrorHandlingConfiguration();
        if (service.getEngine() != null) {
            this.connectionManager = service.getEngine().getConnectionManager();
        }
    }

    public void internalDestroy() throws ServiceExecutionException {
        this.errorHandlingConfiguration = null;
        this.connectionManager = null;
        super.internalDestroy();
    }

    public void handleException(ServiceExecutionException exception, R requestMessage, IRequestListener<R> requestListener, IConnection connection) {
        if (errorHandlingConfiguration != null) {
            Collection errorHandlingActions = errorHandlingConfiguration.getActions(exception.getErrorID());
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_OCCURRED, new String[]{exception.getLocalizedMessage()}));
            performErrorHandlingActions(errorHandlingActions, requestListener, connection, requestMessage, exception);
        }
    }

    private void performErrorHandlingActions(Collection errorHandlingActions, IRequestListener<R> requestListener, IConnection connection, R request,
                                             ServiceExecutionException exception) {

        if (isStopping()) {
            return;
        }

        if (errorHandlingActions != null) {
            for (Object errorHandlingAction : errorHandlingActions) {
                ErrorHandlingAction action = (ErrorHandlingAction) errorHandlingAction;
                if (action.isEnabled()) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PERFORMING_ERROR_HANDLING, new String[]{action.getName()}));
                    switch (action.getId()) {
                        case ErrorHandlingAction.RETRY_EXECUTION:
                            if (action instanceof RetryAction) {
                                if (retry(((RetryAction) action).getConfiguration(), requestListener, connection, request, false)) {
                                    return;
                                }
                            }
                            break;
                        case ErrorHandlingAction.RECONNECT:
                            if (action instanceof RetryAction) {
                                if (retry(((RetryAction) action).getConfiguration(), requestListener, connection, request, true)) {
                                    return;
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
                            discardConnection(connection);
                            break;
                        case ErrorHandlingAction.SEND_TO_ERROR_PORT:
                        default:
                            if (noRetryActions(errorHandlingActions)) {
                                sendError(exception, request);
                            }
                    }
                }
//               else {
//                   if (action.getId() == ErrorHandlingAction.PROCESS_INVALID_REQUEST) {
//                       throw new RuntimeException(exception.getMessage(), exception);
//                   }
//               }
            }
        }
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

    private void discardConnection(IConnection connection) {
        try {
            if (connectionManager != null) {
                connectionManager.errorOccured(connection);
                connectionManager.destroyConnection(connection);
            } else {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ACTION_NOT_SUPPORTED,
                        new String[]{ErrorHandlingAction.getName(ErrorHandlingAction.DISCARD_CONNECTION)}));
            }
        } catch (ServiceExecutionException ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.DISCARD_CONNECTION_FAILED));
        }
    }

    private void sendError(ServiceExecutionException exception, R request) {
        try {
            errorTransport.sendError(exception, request);
        } catch (ServiceExecutionException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_ERROR_FAILED, new Object[]{e.getMessage()}), e);
        }
    }

    private Map populateOtherActionsByCount(RetryConfiguration retryConfiguration) {
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

    private boolean retry(RetryConfiguration retryConfiguration, IRequestListener<R> requestListener, IConnection connection, R request, boolean reconnect) {

//        if (reconnect && connectionManager == null) {
//            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ACTION_NOT_SUPPORTED,
//                                                        new String[] {ErrorHandlingAction.getName(ErrorHandlingAction.RECONNECT)}));
//        }
        Map otherActionsByCount = populateOtherActionsByCount(retryConfiguration);

        for (int index = 0; retryConfiguration.getRetryCount() == -1 || index < retryConfiguration.getRetryCount(); ) {

            if (isStopping()) {
                return true;
            }

            index++;
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RETRY_COUNT, new String[]{String.valueOf(index)}));
            if (State.STOPPED.equals(service.getState())) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.STOPPING_ERROR_HANDLING));
                break;
            }
            try {
                if (isInMemory()) {
                    long sleepTime = 0;
                    while (sleepTime < retryConfiguration.getRetryInterval() && !isStopping()) {
                        long time = (retryConfiguration.getRetryInterval() - sleepTime) > 5000 ? 5000l : (retryConfiguration.getRetryInterval() - sleepTime);
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
                if (reconnect && connectionManager != null) {
                    connectionManager.errorOccured(connection);
                    connectionManager.destroyConnection(connection);
                }
                requestListener.onRequest(request);
                return true;
            } catch (ServiceExecutionException e) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_OCCURRED, new String[]{e.getLocalizedMessage()}));
                Collection<ErrorHandlingAction> actions = new ArrayList<ErrorHandlingAction>();
                Set<Map.Entry<Integer, List<ErrorHandlingAction>>> entrySet = otherActionsByCount.entrySet();
                for (Map.Entry<Integer, List<ErrorHandlingAction>> entry : entrySet) {
                    if (!(entry.getKey() == 0)) {
                        if (index % entry.getKey() == 0) {
                            actions.addAll(entry.getValue());
                        }
                    }
                }
                for (ErrorHandlingAction action : actions) {
                    action.setEnabled(true);
                }
                performErrorHandlingActions(actions, requestListener, connection, request, e);
            }
        }
        return false;
    }

    protected void stopService() {
        service.getLauncher().terminate();
    }

    public boolean isInMemory() {
        return service.getLaunchConfiguration().isInmemoryLaunchable();
    }

    private boolean isStopping() {
        return (service.getState() == State.STOPPING || service.getState() == State.STOPPED
                || service.getState() == State.DESTROYING || service.getState() == State.DESTROYED);
    }
}
