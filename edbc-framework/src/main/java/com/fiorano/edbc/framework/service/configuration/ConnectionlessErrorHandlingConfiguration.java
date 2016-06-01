/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;

/**
 * <code>ConnectionlessErrorHandlingConfiguration</code> defines <code>ErrorHandlingAction<code>s
 * that may be taken when an error / exception occurs in a service which is Connectionless.
 * Supported <code>ServiceErrorID</code>s are {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#INVALID_REQUEST_ERROR},
 * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#RESPONSE_GENERATION_ERROR}
 * and {@link ServiceErrorID#TRANSPORT_ERROR}
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @fiorano.xmbean
 * @see ErrorHandlingConfiguration
 */
public class ConnectionlessErrorHandlingConfiguration extends AbstractErrorHandlingConfiguration {

    protected void loadErrorActions() {
        addError(ServiceErrorID.INVALID_REQUEST_ERROR, getActionsForInvalidRequest());
        addError(ServiceErrorID.REQUEST_EXECUTION_ERROR, getActionsForRequestExecutionError());
        addError(ServiceErrorID.RESPONSE_GENERATION_ERROR, getActionsForResponseGeneration());
        addError(ServiceErrorID.TRANSPORT_ERROR, getActionsForTransportError());
    }
}
