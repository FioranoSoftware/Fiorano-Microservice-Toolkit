/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingActionFactory;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>ErrorHandlingConfiguration</code> defines <code>ErrorHandlingAction<code>s
 * that may be taken when an error / exception occurs in a service which is Connectionless.
 * Supported <code>ServiceErrorID</code>s are {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#INVALID_REQUEST_ERROR},
 * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#RESPONSE_GENERATION_ERROR}
 * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#TRANSPORT_ERROR} and {@link ServiceErrorID#CONNECTION_ERROR}
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @fiorano.xmbean
 * @see ErrorHandlingConfiguration
 */
public class ErrorHandlingConfiguration extends ConnectionlessErrorHandlingConfiguration {

    /**
     * Returns actions that may be taken when {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#CONNECTION_ERROR} occurs
     *
     * @return <code>ErrorHandlingAction</code>s for errors caused due to connection to EIS
     */
    protected Set<ErrorHandlingAction> getActionsForConnectionError() {
        Set<ErrorHandlingAction> actions = new HashSet<>();
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.LOG));
        ErrorHandlingAction discConn = ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.DISCARD_CONNECTION);
        discConn.setEnabled(true);
        actions.add(discConn);
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.RECONNECT));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.STOP_SERVICE));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.THROW_ERROR_ON_WARNING));
        return actions;
    }

    protected void loadErrorActions() {
        super.loadErrorActions();
        addError(ServiceErrorID.CONNECTION_ERROR, getActionsForConnectionError());
    }
}
