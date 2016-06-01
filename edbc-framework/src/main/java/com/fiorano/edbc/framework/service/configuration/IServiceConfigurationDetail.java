/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.util.ErrorListener;

/**
 * <code>IServiceConfigurationDetail</code> is extended by classes which form a part <code>IServiceConfiguration</code>.
 * This is just a convention but not mandatory.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface IServiceConfigurationDetail extends Cloneable {

    /**
     * Validate this configuration
     *
     * @param Listener Listener which should be notified of errors during configuration
     * @throws ServiceConfigurationException Exception thrown when validation fails.
     */
    void validate(ErrorListener Listener) throws ServiceConfigurationException;

    /**
     * Returns formatted string that represents the object, this can be used while logging.
     *
     * @return formatted string representing object
     */
    String getAsFormattedString();

    /**
     * Creates copy of object
     *
     * @return copy of object
     * @throws CloneNotSupportedException if clone is not supported
     */
    Object clone() throws CloneNotSupportedException;
}
