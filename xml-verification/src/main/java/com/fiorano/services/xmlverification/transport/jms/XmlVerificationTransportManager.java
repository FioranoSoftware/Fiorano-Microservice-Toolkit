/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xmlverification.transport.jms;

import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.engine.IEngine;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.internal.transport.jms.IJMSRequestListener;
import com.fiorano.services.xmlverification.engine.XmlVerificationRequestProcessor;

import javax.jms.Message;


public class XmlVerificationTransportManager extends AbstractSyncIOTransportManager<IEngine> {
    public XmlVerificationTransportManager(IService parent) {
        super(parent);
    }

    @Override
    public IJMSRequestListener createRequestListener(String name, TransportAssociation transportAssociation) {
        XmlVerificationRequestProcessor xmlVerificationRequestProcessor = (XmlVerificationRequestProcessor) requestProcessFactory.createRequestProcessor(this, null);
        ErrorHandler<Message> errorHandler = new ErrorHandler<Message>(this, getParent(), transportAssociation.getErrorTransport());
        return new XmlVerificationRequestListener(transportAssociation, xmlVerificationRequestProcessor, errorHandler, logger);
    }
}
