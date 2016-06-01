/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

import com.fiorano.util.ExceptionUtil;

/**
 * <code>ServiceExecutionException</code> is thrown to indicate an exception that occured
 * during execution of service. For framework to automatically handle exceptions occured
 * during execution of service, they should be wrapped in <code>ServiceExecutionException</code>
 * with appropriate <code>ServiceErrorID</code> mentioned.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ServiceExecutionException extends ServiceException {

    /**
     * Creates <code>ServiceExecutionException</code> with given <code>errorID</code>. <code>errorID</code> should be one of
     * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link ServiceErrorID#TRANSPORT_ERROR} or {@link ServiceErrorID#CONNECTION_ERROR}
     *
     * @param errorID errorID indicating the type of exception occured
     */
    public ServiceExecutionException(ServiceErrorID errorID) {
        super(errorID);
    }

    /**
     * Creates <code>ServiceExecutionException</code> with given <code>message</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#TRANSPORT_ERROR} or {@link ServiceErrorID#CONNECTION_ERROR}
     *
     * @param message description of exception occured
     * @param errorID errorID indicating type of exception occured
     */
    public ServiceExecutionException(String message, ServiceErrorID errorID) {
        super(message, errorID);
    }

    /**
     * Creates <code>ServiceExecutionException</code> with given <code>cause</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link ServiceErrorID#TRANSPORT_ERROR} or {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#CONNECTION_ERROR}
     *
     * @param cause   exception occured which raised this exception
     * @param errorID errorID indicating type of exception occured
     */
    public ServiceExecutionException(Throwable cause, ServiceErrorID errorID) {
        super(cause, errorID);
    }

    /**
     * Creates <code>ServiceExecutionException</code> with given <code>message</code>, <code>cause</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#TRANSPORT_ERROR} or {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#CONNECTION_ERROR}
     *
     * @param message description of exception occured
     * @param cause   exception occured which raised this exception
     * @param errorID errorID indicating type of exception occured
     */
    public ServiceExecutionException(String message, Throwable cause, ServiceErrorID errorID) {
        super(message, cause, errorID);
    }

    public String getErrorDetail()
            throws Exception {
        return ExceptionUtil.getStackTrace(this);
    }

    public boolean errorDetailAsCData() {
        return true;
    }
}
