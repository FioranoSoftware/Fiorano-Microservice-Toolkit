/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

import com.fiorano.services.common.Exceptions;

/**
 * <code>ServiceConfigurationException</code> is thrown to indicate service configuration details
 * are invalid. It can contain multiple exceptions indication all possible invalid configuration
 * details
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ServiceConfigurationException extends ServiceException {

    private Exceptions exceptions;

    /**
     * Creates <code>ServiceConfigurationException</code> with given <code>errorID</code>. <code>errorID</code> should be one of
     * {@link ServiceErrorID#INVALID_CONFIGURATION_ERROR} or {@link ServiceErrorID#SERIALIZATION_ERROR}
     *
     * @param errorID errorID indicating the type of exception occured
     */
    public ServiceConfigurationException(ServiceErrorID errorID) {
        super(errorID);
    }

    /**
     * Creates <code>ServiceConfigurationException</code> with given <code>message</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#INVALID_CONFIGURATION_ERROR} or {@link ServiceErrorID#SERIALIZATION_ERROR}
     *
     * @param message description of exception occured
     * @param errorID errorID indicating type of exception occured
     */
    public ServiceConfigurationException(String message, ServiceErrorID errorID) {
        super(message, errorID);
    }

    /**
     * Creates <code>ServiceConfigurationException</code> with given <code>cause</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#INVALID_CONFIGURATION_ERROR} or {@link ServiceErrorID#SERIALIZATION_ERROR}
     *
     * @param cause   exception occured which raised this exception
     * @param errorID errorID indicating type of exception occured
     */
    public ServiceConfigurationException(Throwable cause, ServiceErrorID errorID) {
        super(cause, errorID);
    }

    /**
     * Creates <code>ServiceConfigurationException</code> with given <code>message</code>, <code>cause</code> and <code>errorID</code>. <code>errorID</code>
     * should be one of {@link ServiceErrorID#INVALID_CONFIGURATION_ERROR} or {@link ServiceErrorID#SERIALIZATION_ERROR}
     *
     * @param message description of exception occured
     * @param cause   exception occured which raised this exception
     * @param errorID errorID indicating type of exception occured
     */
    public ServiceConfigurationException(String message, Throwable cause, ServiceErrorID errorID) {
        super(message, cause, errorID);
    }

    /**
     * Indicates whether this exception has multiple exceptions.
     *
     * @return <code>true</code> if there are multiple exceptions; <code>false</code> otherwise
     */
    public boolean hasMultipleExceptions() {
        return getErrorID() == ServiceErrorID.INVALID_CONFIGURATION_ERROR && exceptions != null && !exceptions.isEmpty();
    }

    /**
     * Returns a collection of exceptions each indicating a invalid configuration detail.
     *
     * @return Collection of exceptions
     */
    public Exceptions getExceptions() {
        return exceptions;
    }

    /**
     * Sets a collection of exceptions each indicating a invalid configuration detail.
     *
     * @param exceptions Collection of exceptions
     */
    public void setExceptions(Exceptions exceptions) {
        this.exceptions = exceptions;
    }
}
