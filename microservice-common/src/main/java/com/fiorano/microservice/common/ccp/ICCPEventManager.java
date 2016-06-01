/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

/**
 * The ICCPEventManager manages the life cycle of the objects created for handling the communication
 * with the peer server.
 * <p/>
 * Date: Feb 28, 2010
 * Time: 7:23:47 PM
 */
public interface ICCPEventManager {

    /**
     * Initiates the communication with the peer server.
     *
     * @throws Exception If an error occurs during initialization
     */
    void start() throws Exception;

    /**
     * Makes a graceful stop of the actions in progress and cleans up the resources being used.
     *
     * @throws Exception If an error occurs during cleanup
     */
    void stop() throws Exception;

    /**
     * Return the ICCPEventGenerator held by this object.
     *
     * @return ICCPEventGenerator used to send the Control Events to the peer server.
     */
    ICCPEventGenerator getCCPEventGenerator();

    /**
     * Sets the ICCPEventGenerator that will be used to send the events to the peer server.
     *
     * @param ccpEventGenerator used to send events to the peer server.
     */
    void setCCPEventGenerator(ICCPEventGenerator ccpEventGenerator);

    /**
     * Return the ICCPEventHandler held by this object.
     *
     * @return ICCPEventHandler used to handle the Control Events received from the peer server.
     */
    ICCPEventHandler getCCPEventHandler();

    /**
     * Sets the CCPEventHandler that will be used to handle the events received from the peer server.
     *
     * @param ccpEventHandler used to handle the events from peer server.
     */
    void setCCPEventHandler(ICCPEventHandler ccpEventHandler);

}
