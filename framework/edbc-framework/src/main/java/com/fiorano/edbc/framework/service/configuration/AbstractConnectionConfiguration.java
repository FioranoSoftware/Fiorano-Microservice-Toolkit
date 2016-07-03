/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.util.ErrorListener;
import fiorano.esb.common.ESBException;
import fiorano.esb.utils.BeanUtils;

/**
 * <code>AbstractConnectionConfiguration</code> should be extended by classes which holds configuration
 * details (metadata) required to establish a connection to EIS system
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @fiorano.xmbean
 */
public abstract class AbstractConnectionConfiguration implements IServiceConfigurationDetail {

    /**
     * @jmx.managed-operation description="Validates Configuration Properties"
     * @jmx.managed-parameter name="Listener" type="com.fiorano.util.ErrorListener" description="Listens for errors occured during validation"
     */
    public abstract void validate(ErrorListener Listener) throws ServiceConfigurationException;

    /**
     * Tests the connection to EIS using connection details provided
     *
     * @jmx.managed-operation description="Tests the connection"
     */
    public abstract void test() throws ServiceException;

    public Object clone() throws CloneNotSupportedException {
        try {
            String xml = BeanUtils.serialiseBean(this);
            return BeanUtils.deserialiseBean(xml);
        } catch (ESBException e) {
            Throwable linkedException = e.getLinkedException();
            if (linkedException == null) {
                linkedException = e;
            }
            throw new CloneNotSupportedException(linkedException.getMessage());
        }
    }
}
