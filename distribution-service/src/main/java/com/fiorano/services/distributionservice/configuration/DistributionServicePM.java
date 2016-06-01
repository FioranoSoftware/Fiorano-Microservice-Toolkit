/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.configuration;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.distributionservice.Bundle;
import com.fiorano.util.ErrorListener;
import com.fiorano.util.lang.ClassUtil;

import java.util.Map;
import java.util.Set;

/**
 * Configuration for Distribution service
 *
 * @author FSIPL
 * @version 1.0
 * @created March 25, 2006
 * @fiorano.xmbean
 * @jmx.mbean
 * @jboss.xmbean
 */
public class DistributionServicePM extends ConnectionlessServiceConfiguration {
    private int[] weightsOfPorts = {1, 1, 1, 1, 1};
    private int portCount = 5;
    private boolean isPropagateSchema;

    /**
     * Constructor for the DistributionServicePropertyModel object
     */
    public DistributionServicePM() {
        for (Object o : errorHandlingConfiguration.getErrorActionsMap().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Set actions = (Set) entry.getValue();
            boolean isJMSError = ServiceErrorID.TRANSPORT_ERROR.equals(entry.getKey());
            for (Object action : actions) {
                ErrorHandlingAction errorHandlingAction = (ErrorHandlingAction) action;
                if (errorHandlingAction.getId() == ErrorHandlingAction.LOG || errorHandlingAction.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                    errorHandlingAction.setEnabled(true);
                }
                if (isJMSError && errorHandlingAction.getId() == ErrorHandlingAction.STOP_SERVICE) {
                    errorHandlingAction.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void test() throws ServiceException {
    }


    /**
     * fetches the weights on output ports as an array of ints
     *
     * @return an int array containing wight on each output port at the respective index
     * @jmx.managed-attribute description="It specifies the weight associated with each of the output ports.
     * This is the number of messages to be sent from a particular output port before sending the message on the next output port."
     * @jmx.descriptor name="displayName" value="Port Weights"
     */
    public int[] getPortWeights() {
        return weightsOfPorts;
    }

    /**
     * sets the weights on output ports as an array of ints
     * object
     *
     * @param weights - weights of output ports
     */
    public void setPortWeights(int[] weights) {
        weightsOfPorts = weights;
    }

    /**
     * The number of output ports specified in the configuration
     *
     * @return number of output ports
     * @jmx.managed-attribute description="It specifies the number of output ports to be added for the service."
     * @jmx.descriptor name="displayName" value="Number of Ports"
     */
    public int getPortCount() {
        return portCount;
    }

    /**
     * The number of output ports specified in the configuration
     *
     * @param count - number of output ports to be created
     */
    public void setPortCount(int count) {
        portCount = count;
    }

    public boolean isPropagateSchema() {
        return isPropagateSchema;
    }

    /**
     * sets propagateSchema option
     *
     * @param propagateSchema
     */
    public void setPropagateSchema(boolean propagateSchema) {
        isPropagateSchema = propagateSchema;
    }

    public String getAsFormattedString() {
        return null;
    }

    /**
     * @return name of help set.
     */
    public String getHelpSetName() {
        return ClassUtil.getShortClassName(this.getClass()) + ".hs";
    }

    /**
     * @jmx.managed-operation description="Validates Configuration Properties"
     * @jmx.managed-parameter name="listener" type="com.fiorano.util.ErrorListener" description="Listens for errors occured during validation"
     */
    @Override
    public void validate(ErrorListener listener) throws ServiceConfigurationException {

        super.validate(listener);

        if (getPortCount() < 1) {
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_NO_OF_PORTS),
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }
}
