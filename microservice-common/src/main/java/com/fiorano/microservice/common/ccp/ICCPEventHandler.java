/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

import com.fiorano.openesb.microservice.ccp.event.ControlEvent;

/**
 * The ICCPEventHandler handles the events that are received by the component from the peer server.
 * <p/>
 * Date: Feb 27, 2010
 * Time: 2:15:12 PM
 */
public interface ICCPEventHandler {

    /**
     * Handles the events that are sent from the peer server
     *
     * @param event The event received by the component from the peer server.
     */
    void handleEvent(ControlEvent event);

    /**
     * Makes a graceful stop of the actions in progress and cleans up the resources being used.
     *
     * @throws Exception
     */
    void stop() throws Exception;
}
