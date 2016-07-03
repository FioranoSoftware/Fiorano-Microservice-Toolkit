/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.configuration;

import com.fiorano.edbc.framework.service.configuration.Bundle;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.common.ESBException;
import fiorano.esb.utils.BeanUtils;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 21-Jan-2011
 * Time: 18:43:37
 * To change this template use File | Settings | File Templates.
 */
public class BeanXMLConfigurationSerializer<C extends IServiceConfiguration> implements IConfigurationSerializer<C> {

    /**
     * Deserializes service configuration from a String to which the configuration is serialized.
     *
     * @param stringRepresentation configuration in serialized form
     * @return deserialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public C deserializeFromString(String stringRepresentation) throws ServiceConfigurationException {
        return (C) internalDeserializeFromString(stringRepresentation);
    }

    protected Object internalDeserializeFromString(String stringRepresentation) throws ServiceConfigurationException {
        if (stringRepresentation == null) {
            return null;
        }
        Object configuration;
        try {
            configuration = BeanUtils.deserialiseBean(stringRepresentation);
            /*if(configuration instanceof IServiceConfiguration)
                ((IServiceConfiguration)configuration).decryptPasswords();*/
        } catch (ESBException e) {
            String message = e.getMessage();
            if (message == null) {
                message = e.getLinkedException() != null ? e.getLinkedException().getMessage() : "";
            }
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.DESERIALIZATION_FAILED, new Object[]{message}), e,
                    ServiceErrorID.SERIALIZATION_ERROR);
        }
        return configuration;
    }

    /**
     * Serializes provided service configuration to a String.
     *
     * @param configuration configuration of service
     * @return String containing serialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public String serializeToString(C configuration) throws ServiceConfigurationException {
        if (configuration == null) {
            return null;
        }
        String serializedConfiguration;
        try {
//            configuration.encryptPasswords();
            serializedConfiguration = BeanUtils.serialiseBean(configuration);
        } catch (ESBException e) {
            String message = e.getMessage();
            if (message == null) {
                message = e.getLinkedException() != null ? e.getLinkedException().getMessage() : "";
            }
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.DESERIALIZATION_FAILED, new Object[]{message}), e,
                    ServiceErrorID.SERIALIZATION_ERROR);
        }
        return serializedConfiguration;
    }

}