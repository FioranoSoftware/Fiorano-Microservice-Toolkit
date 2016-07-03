/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

import com.fiorano.openesb.microservice.ccp.event.ControlEvent;

/**
 * The ICCPEventGenerator sends the events from the component to the peer server.
 * <p/>
 * Date: Mar 01, 2010
 * Time: 7:23:47 PM
 */
public interface ICCPEventGenerator {
    /**
     * Sends the control event to the peer server.
     *
     * @param event The event that has to be sent to the peer server.
     */
    void sendEvent(ControlEvent event);

}
