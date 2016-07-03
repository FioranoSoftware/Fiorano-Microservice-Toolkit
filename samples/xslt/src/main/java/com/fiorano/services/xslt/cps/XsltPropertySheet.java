/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt.cps;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.cps.JMXPropertySheet;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.services.xslt.configuration.XsltConfigurationSerializer;
import com.fiorano.services.xslt.configuration.XsltPM;
import fiorano.esb.record.ESBRecordDefinition;

/**
 * Execution class for Xslt.
 * This class contains the main method which launches the component when started as an external process.
 * It creates the JMSHandler and new configuration object if needed.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class XsltPropertySheet extends JMXPropertySheet {

    protected IServiceConfiguration getDefaultConfiguration() {
        return new XsltPM();
    }

    protected void onClose(boolean finished, Object configuration, CPSESBUtil cpsesbUtil) {
        super.onClose(finished, configuration, cpsesbUtil);
        if (finished) {
            XsltPM config = (XsltPM) configuration;
            ESBRecordDefinition outSchemaDef = XsltPM.CONTEXT.equals(config.getOutputStructure())
                    ? config.getEsbDefInPort() : config.getEsbDefOutPort();
            cpsesbUtil.getServiceInstanceAdapter().getInputPortInstance("IN_PORT").setSchema(config.getEsbDefInPort());
            cpsesbUtil.getServiceInstanceAdapter().getOutputPortInstance("OUT_PORT").setSchema(outSchemaDef);
        }
    }

    @Override
    protected IConfigurationSerializer createConfigurationSerializer() {
        return new XsltConfigurationSerializer();
    }
}
