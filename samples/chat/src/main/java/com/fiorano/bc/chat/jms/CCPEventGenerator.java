/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat.jms;

import com.fiorano.bc.chat.ChatService;
import com.fiorano.microservice.common.ccp.AbstractCCPEventGenerator;

import javax.jms.MessageProducer;
import javax.jms.Session;
import java.util.logging.Logger;

/**
 * The CCPEventGenerator converts the events to messages and publish them onto
 * the CCP_PEER_TO_COMPONENT_TRANSPORT topic.
 * <p/>
 * Date: Mar 01, 2010
 * Time: 7:23:47 PM
 */
public class CCPEventGenerator extends AbstractCCPEventGenerator {

    private ChatService service;

    public CCPEventGenerator(MessageProducer producer, Session session, ChatService service, Logger logger) {
        super(producer, session, logger);
        this.service = service;
    }

    @Override
    protected String getComponentID() {
        return service.getServiceLookupName();

    }
}
