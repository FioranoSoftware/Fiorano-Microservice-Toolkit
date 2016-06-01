/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.configuration;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 21-Jan-2011
 * Time: 16:17:46
 * To change this template use File | Settings | File Templates.
 */
public interface IConfigurationSerializer<C extends IServiceConfiguration> {

    /**
     * Deserializes service configuration from a String to which the configuration is serialized.
     *
     * @param stringRepresentation configuration in serialized form
     * @return deserialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    C deserializeFromString(String stringRepresentation) throws ServiceConfigurationException;

    /**
     * Serializes provided service configuration to a String.
     *
     * @param configuration configuration of service
     * @return String containing serialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    String serializeToString(C configuration) throws ServiceConfigurationException;
}
