/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.transport.jms;

import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.engine.IEngine;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.IJMSRequestListener;
import com.fiorano.services.display.engine.DisplayRequestProcessor;
import com.fiorano.services.display.engine.DisplayEngine;

import javax.jms.Message;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 9 Nov, 2010
 * Time: 6:38:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayTransportManager extends AbstractSyncIOTransportManager<DisplayEngine> {
    public DisplayTransportManager(IService parent) {
        super(parent);
    }


    public IJMSRequestListener createRequestListener(String name, TransportAssociation transportAssociation) {
        ErrorHandler<Message> errorHandler = new ErrorHandler<Message>(this, getParent(), transportAssociation.getErrorTransport());
        return new DisplayMessageListener(transportAssociation, (DisplayRequestProcessor) requestProcessFactory.createRequestProcessor(this,null),errorHandler,logger);
    }
}
