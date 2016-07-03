/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display;


import com.fiorano.bc.display.model.ConfigurationPM;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.internal.configuration.BeanXMLConfigurationSerializer;
import com.fiorano.services.display.util.ConfigurationConverter;

/**
 * Created by IntelliJ IDEA.
 * User: Spurthy
 * Date: Feb 3, 2011
 * Time: 12:02:59 PM
 * To change this template use File | Settings | File Templates.
 */

public class DisplayConfigurationSerializer extends BeanXMLConfigurationSerializer<ConfigurationPM> {
    @Override
    public ConfigurationPM deserializeFromString(String stringRepresentation) throws ServiceConfigurationException {
        Object deserializedObject = internalDeserializeFromString(stringRepresentation);
        if (deserializedObject instanceof ConfigurationPM) {
            return (ConfigurationPM) deserializedObject;
        } else {
            return ConfigurationConverter.convert(deserializedObject);
        }
    }
}