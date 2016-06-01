/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.xslt.configuration;

import com.fiorano.edbc.framework.service.configuration.Bundle;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.internal.configuration.BeanXMLConfigurationSerializer;
import com.fiorano.services.common.util.RBUtil;

import javax.xml.stream.XMLStreamException;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 22-Jan-2011
 * Time: 14:06:36
 * To change this template use File | Settings | File Templates.
 */
public class XsltConfigurationSerializer extends BeanXMLConfigurationSerializer<XsltPM> {
    @Override
    public XsltPM deserializeFromString(String stringRepresentation) throws ServiceConfigurationException {
        XsltPM configuration;
        ConfigurationConvertor convertor = new ConfigurationConvertor();
        try {
            if (!convertor.isOldConfiguration(stringRepresentation)) {
                configuration = super.deserializeFromString(stringRepresentation);
            } else {
                configuration = convertor.convert(stringRepresentation);
            }
        } catch (XMLStreamException e) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.DESERIALIZATION_FAILED, new Object[]{e.getMessage()});
            throw new ServiceConfigurationException(message, e, ServiceErrorID.SERIALIZATION_ERROR);
        }
        return configuration;
    }
}
