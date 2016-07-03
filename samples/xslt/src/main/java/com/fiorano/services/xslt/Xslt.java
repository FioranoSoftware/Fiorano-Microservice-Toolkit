/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt;

import com.fiorano.edbc.framework.service.AbstractInmemoryService;
import com.fiorano.edbc.framework.service.Bundle;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.jms.IJMSObjects;
import com.fiorano.services.common.configuration.NamedConfigConstants;
import com.fiorano.services.common.service.ManageablePropertyUtil;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.xslt.configuration.XsltConfigurationSerializer;
import com.fiorano.services.xslt.configuration.XsltPM;

import javax.naming.NamingException;

/**
 * Execution class for Xslt.
 * This class contains the main method which launches the component when started as an external process.
 * It creates the JMSHandler and new configuration object if needed.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class Xslt extends AbstractInmemoryService {

    /**
     * Main method starts the service in external process.
     *
     * @param args commandline arguments
     */
    public static void main(String args[]) {
        try {
            Xslt service = new Xslt();
            service.start(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a JMS Handler
     *
     * @return JMSObjects class which handles the jmsObjects.
     */
    protected IJMSObjects _createJMSObjects() {
        return new JMSObjects(this);
    }

    /**
     * Specifies whether the configuration is mandaory or not.
     *
     * @return whether the configuration is mandatory.
     */
    protected boolean isConfigurationMandatory() {
        return true;
    }

    /**
     * Creates a new Property Model object
     */
    protected void createDefaultServiceConfiguration() {
        configuration = new XsltPM();
    }

    protected void fetchConfiguration() throws ServiceExecutionException {

        try {
            String serializedConfiguration = (String) lookupHelper.lookupSerializedConfiguration();
            XsltConfigurationSerializer configurationSerializer = new XsltConfigurationSerializer();
            configuration = configurationSerializer.deserializeFromString(serializedConfiguration);
        } catch (NamingException e) {
            if (isConfigurationMandatory()) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class,
                        Bundle.CONFIGURATION_LOOKUP_FAILED),
                        e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            } else {
                logger.warning(RBUtil.getMessage(Bundle.class,
                        Bundle.CONFIGURATION_LOOKUP_FAILED)
                        + RBUtil.getMessage(Bundle.class,
                        Bundle.CREATING_NEW_CONFIGURATION));
                createDefaultServiceConfiguration();
            }
        } catch (ServiceConfigurationException e) {
            if (isConfigurationMandatory()) {
                throw new ServiceExecutionException(e.getMessage(), e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
            } else {
                logger.warning(e.getMessage() + RBUtil.getMessage(Bundle.class,
                        Bundle.CREATING_NEW_CONFIGURATION));
                createDefaultServiceConfiguration();
            }
        }
        try {
            ManageablePropertyUtil.setManageableProperties(configuration, lookupHelper.lookupManageableProperties());
        } catch (NamingException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.MANAGEABLE_PROPERTIES_LOOKUP_FAILED),
                    e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }

        try {
            updateNamedConfigurations();
        } catch (Exception e) {
            throw new ServiceExecutionException(
                    RBUtil.getMessage(Bundle.class, Bundle.NAMED_CONFIG_LOOKUP_FAILED, new Object[]{e.getMessage()}),
                    e, ServiceErrorID.SERVICE_LAUNCH_ERROR);
        }

        ServiceDetails details = new ServiceDetails("FPS", commandLineParams.getApplicationName(),
                commandLineParams.getServiceGUID(), commandLineParams.getServiceInstanceName(), String.valueOf(commandLineParams.getApplicationVersion()));
        ((XsltPM) configuration).updateServiceDetails(details);
    }

    @Override
    protected void updateNamedConfigurations() throws Exception {

        XsltPM config = (XsltPM) getConfiguration();

        if (config == null) {
            return;
        }

        updateNamedConfiguration(config.getThreadPoolConfiguration(), NamedConfigConstants.RESOURCE_CONFIG_TYPE);
    }
}
