/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder;

import com.fiorano.bc.feeder.ConfigurationConverter;
import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.internal.configuration.BeanXMLConfigurationSerializer;


public class FeederConfigurationSerializer extends BeanXMLConfigurationSerializer<FeederPM> {
    @Override
    public FeederPM deserializeFromString(String stringRepresentation) throws ServiceConfigurationException {
        Object deserializedObject = internalDeserializeFromString(stringRepresentation);
        if (deserializedObject instanceof FeederPM) {
            return (FeederPM) deserializedObject;
        } else {
            return ConfigurationConverter.convert(deserializedObject);
        }
    }
}
