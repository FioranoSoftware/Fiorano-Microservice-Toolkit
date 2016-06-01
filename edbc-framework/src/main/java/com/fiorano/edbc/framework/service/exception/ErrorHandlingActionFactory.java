/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

/**
 * Factory class for creating {@link com.fiorano.edbc.framework.service.exception.ErrorHandlingAction} objects.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public final class ErrorHandlingActionFactory {
    /**
     * Returns <code>ErrorHandlingAction</code> for given <code>id</code>.
     *
     * @param id value from {@link ErrorHandlingAction#LOG}, {@link com.fiorano.edbc.framework.service.exception.ErrorHandlingAction#SEND_TO_ERROR_PORT},
     *           {@link ErrorHandlingAction#RETRY_EXECUTION}, {@link com.fiorano.edbc.framework.service.exception.ErrorHandlingAction#RECONNECT},
     *           {@link ErrorHandlingAction#THROW_ERROR_ON_WARNING}, {@link ErrorHandlingAction#STOP_SERVICE},
     *           {@link com.fiorano.edbc.framework.service.exception.ErrorHandlingAction#PROCESS_INVALID_REQUEST}
     * @return <code>ErrorHandlingAction</code> object
     */
    public static ErrorHandlingAction createErrorHandlingAction(int id) {
        switch (id) {
            case ErrorHandlingAction.RETRY_EXECUTION:
                RetryAction retryAction = new RetryAction(id);
                RetryConfiguration configuration = new RetryConfiguration();
                ErrorHandlingAction errorHandlingAction = createErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT);
                configuration.addOtherAction(errorHandlingAction, 1);
                errorHandlingAction = createErrorHandlingAction(ErrorHandlingAction.LOG);
                configuration.addOtherAction(errorHandlingAction, 1);
                retryAction.setConfiguration(configuration);
                return retryAction;
            case ErrorHandlingAction.RECONNECT:
                RetryAction reConnect = new RetryAction(id);
                RetryConfiguration reConnectConfiguration = new RetryConfiguration();
                reConnectConfiguration.setRetryCount(10);
                reConnectConfiguration.setRetryInterval(300000);
                ErrorHandlingAction action = createErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT);
                reConnectConfiguration.addOtherAction(action, 2);
                action = createErrorHandlingAction(ErrorHandlingAction.LOG);
                reConnectConfiguration.addOtherAction(action, 1);
                reConnect.setConfiguration(reConnectConfiguration);
                reConnect.setEnabled(true);
                return reConnect;
            default:
                return new ErrorHandlingAction(id);
        }
    }
}
