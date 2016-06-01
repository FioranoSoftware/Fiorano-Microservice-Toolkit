/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt;

import com.fiorano.edbc.framework.service.jms.AbstractJMSObjects;
import com.fiorano.edbc.framework.service.jms.ports.AbstractInputPortHandler;
import com.fiorano.esb.wrapper.InputPortInstanceAdapter;
import com.fiorano.services.xslt.ports.InputPortHandler;
import fiorano.esb.util.EventGenerator;

import javax.jms.Destination;
import javax.jms.Session;
import java.util.Collection;
import java.util.Iterator;

/**
 * JMSObjects handles the creation of connection parameters, does the lookup and creates the JMSHandler.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class JMSObjects extends AbstractJMSObjects {

    /**
     * Creates an instance of JMSObjects.
     *
     * @param service service class object.
     */
    public JMSObjects(Xslt service) {
        super(service);
    }

    /**
     * @param destination              receive destination.
     * @param inputPortInstanceAdapter InputPortInstanceAdapter is an adapter (wrapper class) for InputPortInstance.
     * @param outputPortHandlers       output port handlers collection.
     * @param eventGenerator           used in raising events.
     * @param eventSession             session object.
     * @return input port handler object.
     */
    protected AbstractInputPortHandler createInputPortHandler(Destination destination,
                                                              InputPortInstanceAdapter inputPortInstanceAdapter,
                                                              Collection outputPortHandlers,
                                                              EventGenerator eventGenerator, Session eventSession) {
        return new InputPortHandler(destination, inputPortInstanceAdapter, outputPortHandlers, eventGenerator, eventSession);
    }

    public void destroy() {
        Collection inputPortHandlers = getInputPortHandlers();
        if (inputPortHandlers != null) {
            for (Iterator iterator = inputPortHandlers.iterator(); iterator.hasNext(); ) {
                InputPortHandler inputPortHandler = (InputPortHandler) iterator.next();
                inputPortHandler.stop();
            }
        }
        super.destroy();
    }
}

