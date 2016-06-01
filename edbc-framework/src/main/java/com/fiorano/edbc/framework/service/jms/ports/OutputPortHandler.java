/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.jms.ports;

import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import fiorano.esb.util.LoggerUtil;

import javax.jms.Destination;
import java.util.logging.Logger;

/**
 * Class that holds refernce to output port object
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @created Thu, 22 Mar 2007
 */
public class OutputPortHandler {
    /**
     * Logger used for logging.
     */
    private Logger logger;

    /**
     * The send destination object.
     */
    private Destination destination;

    /**
     * Outputport instance adapter.
     */
    private OutputPortInstanceAdapter outputPortInstanceAdapter;

    /**
     * Creates an instance of OutputPortHandler.
     *
     * @param destination send destination
     */
    public OutputPortHandler(Destination destination, OutputPortInstanceAdapter outputPortInstanceAdapter) {
        this.destination = destination;
        this.outputPortInstanceAdapter = outputPortInstanceAdapter;
        this.logger = Logger.getLogger(this.getClass().getName());
        LoggerUtil.addFioranoConsoleHandler(this.logger);
    }

    /**
     * Gets the logger.
     *
     * @return logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Sets the logger object.
     */
    public void setLogger(Logger logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    /**
     * Gets the send destination.
     *
     * @return destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Gets the Outputport instance adapter.
     *
     * @return outputPortInstanceAdapter
     */
    public OutputPortInstanceAdapter getOutputPortInstanceAdapter() {
        return outputPortInstanceAdapter;
    }
}
