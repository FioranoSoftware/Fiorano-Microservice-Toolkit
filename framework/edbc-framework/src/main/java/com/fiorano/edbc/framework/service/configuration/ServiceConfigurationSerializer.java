/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.internal.configuration.BeanXMLConfigurationSerializer;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.utils.BeanUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>ServiceConfigurationSerializer</code> is used to serialize or deserialize configuration of service
 * (should implement IServiceConfiguration).
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public final class ServiceConfigurationSerializer {

    private final static BeanXMLConfigurationSerializer SERIALIZER = new BeanXMLConfigurationSerializer();

    /**
     * Deserializes service configuration from either a String or byte array or an Input Stream to which the configuration
     * is serialized.
     *
     * @param serializedConfiguration configuration in serialized form (String, byte array or InputStream)
     * @return deserialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public static IServiceConfiguration deserialize(Object serializedConfiguration) throws ServiceConfigurationException {
        if (serializedConfiguration instanceof String) {
            return deserializeFromString((String) serializedConfiguration);
        } else if (serializedConfiguration instanceof byte[]) {
            return deserializeFromBytes((byte[]) serializedConfiguration);
        } else if (serializedConfiguration instanceof InputStream) {
            return deserializeFromStream((InputStream) serializedConfiguration);
        } else {
            Object message = RBUtil.getMessage(Bundle.class, Bundle.UNKNOWN_SERIALIZATION);
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.DESERIALIZATION_FAILED, new Object[]{message}),
                    ServiceErrorID.SERIALIZATION_ERROR);
        }
    }

    /**
     * Deserializes service configuration from a String to which the configuration is serialized.
     *
     * @param serializedConfiguration configuration in serialized form
     * @return deserialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public static IServiceConfiguration deserializeFromString(String serializedConfiguration) throws ServiceConfigurationException {
        return SERIALIZER.deserializeFromString(serializedConfiguration);
    }

    /**
     * Deserializes service configuration from an InputStream to which the configuration is serialized.
     *
     * @param serializedConfiguration configuration in serialized form
     * @return deserialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public static IServiceConfiguration deserializeFromStream(InputStream serializedConfiguration) throws ServiceConfigurationException {
        if (serializedConfiguration == null) {
            return null;
        }
        Object configuration;
        configuration = BeanUtils.deserialiseBean(serializedConfiguration);
        if (configuration instanceof IServiceConfiguration)
            ((IServiceConfiguration) configuration).decryptPasswords();
        return (IServiceConfiguration) (configuration instanceof IServiceConfiguration ? configuration : null);
    }

    /**
     * Deserializes service configuration from a bye array to which the configuration is serialized.
     *
     * @param serializedConfiguration configuration in serialized form
     * @return deserialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public static IServiceConfiguration deserializeFromBytes(byte[] serializedConfiguration) throws ServiceConfigurationException {
        if (serializedConfiguration == null) {
            return null;
        }
        return deserializeFromStream(new ByteArrayInputStream(serializedConfiguration));
    }

    /**
     * Serializes provided service configuration to a String.
     *
     * @param configuration configuration of service
     * @return String containing serialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public static String serializeAsString(IServiceConfiguration configuration) throws ServiceConfigurationException {
        return SERIALIZER.serializeToString(configuration);
    }

    /**
     * Serializes provided service configuration to a byte array.
     *
     * @param configuration configuration of service
     * @return byte array containing serialized configuration.
     * @throws ServiceConfigurationException if an exception occurs during deserialization.
     */
    public static byte[] serializeAsBytes(IServiceConfiguration configuration) throws ServiceConfigurationException {
        if (configuration == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializeToStream(configuration, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Serializes provided service configuration to an OutputStream.
     *
     * @param configuration configuration of service
     * @param outputStream  stream to which serialized service configuration should be written
     * @throws com.fiorano.edbc.framework.service.exception.ServiceConfigurationException if an exception occurs during deserialization.
     */
    public static void serializeToStream(IServiceConfiguration configuration, OutputStream outputStream) throws ServiceConfigurationException {
        if (configuration != null) {
            configuration.encryptPasswords();
            BeanUtils.serialiseBean(configuration, outputStream, Thread.currentThread().getContextClassLoader());
        }
    }

}
