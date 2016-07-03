/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

/**
 * <code>ServiceException</code> is base class for exceptions thrown by a service. It
 * is recommmended that all exceptions thrown by service classes are instances of
 * <code>ServiceException</code> but not mandatory.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class ServiceException extends Exception {

    private ServiceErrorID errorID;

    /**
     * Creates <code>ServiceException</code> with given <code>errorID</code>. <code>errorID</code> should be one of
     * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#INVALID_CONFIGURATION_ERROR}, {@link ServiceErrorID#SERIALIZATION_ERROR},
     * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link ServiceErrorID#TRANSPORT_ERROR} or {@link ServiceErrorID#CONNECTION_ERROR}.
     * Inherited classes might allow only a subset of error id values mentioned
     *
     * @param errorID errorID indicating the type of exception occured
     */
    protected ServiceException(ServiceErrorID errorID) {
        super();
        this.errorID = errorID;
    }

    /**
     * Creates <code>ServiceException</code> with given <code>message</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#INVALID_CONFIGURATION_ERROR}, {@link ServiceErrorID#SERIALIZATION_ERROR},
     * {@link ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link ServiceErrorID#TRANSPORT_ERROR} or {@link ServiceErrorID#CONNECTION_ERROR}.
     * Inherited classes might allow only a subset of error id values mentioned
     *
     * @param message description of exception occured
     * @param errorID errorID indicating type of exception occured
     */
    protected ServiceException(String message, ServiceErrorID errorID) {
        super(message);
        this.errorID = errorID;
    }

    /**
     * Creates <code>ServiceException</code> with given <code>cause</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#INVALID_CONFIGURATION_ERROR}, {@link ServiceErrorID#SERIALIZATION_ERROR},
     * {@link ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link ServiceErrorID#TRANSPORT_ERROR} or {@link ServiceErrorID#CONNECTION_ERROR}.
     * Inherited classes might allow only a subset of error id values mentioned
     *
     * @param cause   exception occured which raised this exception
     * @param errorID errorID indicating type of exception occured
     */
    protected ServiceException(Throwable cause, ServiceErrorID errorID) {
        super(cause);
        this.errorID = errorID;
    }

    /**
     * Creates <code>ServiceException</code> with given <code>message</code>, <code>cause</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#INVALID_CONFIGURATION_ERROR}, {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#SERIALIZATION_ERROR},
     * {@link ServiceErrorID#SERVICE_LAUNCH_ERROR}, {@link ServiceErrorID#INVALID_REQUEST_ERROR},
     * {@link ServiceErrorID#REQUEST_EXECUTION_ERROR}, {@link ServiceErrorID#RESPONSE_GENERATION_ERROR},
     * {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#TRANSPORT_ERROR} or {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#CONNECTION_ERROR}.
     * Inherited classes might allow only a subset of error id values mentioned
     *
     * @param message description of exception occured
     * @param cause   exception occured which raised this exception
     * @param errorID errorID indicating type of exception occured
     */
    protected ServiceException(String message, Throwable cause, ServiceErrorID errorID) {
        super(message, cause);
        this.errorID = errorID;
    }

    /**
     * Returns the error id indicating the type of error occured.
     *
     * @return error ID
     */
    public ServiceErrorID getErrorID() {
        return errorID;
    }
}
