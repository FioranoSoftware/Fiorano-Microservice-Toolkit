/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.swing;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.edbc.cache.configuration.FieldDefinitions;
import com.fiorano.edbc.cache.configuration.XSDGenerator;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.cps.JMXPropertySheet;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.esb.wrapper.InputPortInstanceAdapter;
import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import com.fiorano.esb.wrapper.ServiceInstanceAdapter;
import com.fiorano.services.cache.CacheConstants;
import com.fiorano.services.cache.cps.swing.editors.panels.Bundle;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.record.ESBRecordDefinition;

import java.util.logging.Level;


public class CachePropertySheet extends JMXPropertySheet {
    @Override
    protected IServiceConfiguration getDefaultConfiguration() {
        return new CachePM();
    }

    @Override
    protected void onClose(boolean finished, Object configuration, CPSESBUtil cpsesbUtil) {
        if (finished) {
            CachePM cachePM = (CachePM) configuration;
            ServiceInstanceAdapter serviceInstanceAdapter = cpsesbUtil.getServiceInstanceAdapter();
            FieldDefinitions fieldDefinitions = cachePM.getFieldDefinitions();
            InputPortInstanceAdapter addPortInstance = serviceInstanceAdapter.getInputPortInstance(CacheConstants.ADD_PORT);
            InputPortInstanceAdapter deletePortInstance = serviceInstanceAdapter.getInputPortInstance(CacheConstants.DEL_PORT);
            OutputPortInstanceAdapter outputPortInstance = serviceInstanceAdapter.getOutputPortInstance(CacheConstants.OUT_PORT);
            try {
                ESBRecordDefinition schema = XSDGenerator.getSchema(fieldDefinitions, CacheConstants.ADD_PORT);
                addPortInstance.setSchema(schema);
            } catch (Exception e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_SET_PORT_XSD, new String[]{CacheConstants.ADD_PORT, e.getMessage()}), e);
            }
            try {
                ESBRecordDefinition schema = XSDGenerator.getSchema(fieldDefinitions, CacheConstants.DEL_PORT);
                deletePortInstance.setSchema(schema);
            } catch (Exception e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_SET_PORT_XSD, new String[]{CacheConstants.DEL_PORT, e.getMessage()}), e);
            }
            try {
                ESBRecordDefinition schema = XSDGenerator.getSchema(fieldDefinitions, CacheConstants.OUT_PORT);
                outputPortInstance.setSchema(schema);
            } catch (Exception e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_SET_PORT_XSD, new String[]{CacheConstants.OUT_PORT, e.getMessage()}), e);
            }
        }
    }
}
