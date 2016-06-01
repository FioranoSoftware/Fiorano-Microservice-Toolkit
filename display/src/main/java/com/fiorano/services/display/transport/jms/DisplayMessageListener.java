/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.transport.jms;

import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOMessageListener;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.edbc.framework.service.internal.error.ErrorHandler;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.display.engine.DisplayRequestProcessor;
import fiorano.jms.services.msg.def.FioranoMessage;

import javax.jms.Message;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DisplayMessageListener extends AbstractSyncIOMessageListener<DisplayRequestProcessor> {

    public DisplayMessageListener(AbstractSyncIOTransportManager.TransportAssociation transportAssociation, DisplayRequestProcessor requestProcessor,
                                  ErrorHandler<Message> errorHandler, Logger logger) {
        super(transportAssociation, requestProcessor, errorHandler, logger);
    }


    public void onRequest(Message msg) throws ServiceExecutionException {
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RECEIVED_MSG,
                        new Object[]{msg}));
            }
        }
        catch (Throwable thr) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_GETTING_TEXT), thr);
            }
        }

        FioranoMessage document = null;
        try {
            document = (FioranoMessage) ((FioranoMessage) msg).clone();
        } catch (Exception e) {}

       try {
           sendResponse(Constants.OUT_PORT_NAME, document != null ? document : msg);
       } catch (ServiceExecutionException e) {
            handleException(e, msg);
       }
        try {
            if (document != null) {
                msg.setStringProperty(Constants.COMPONENT_PROCESSING_TIME, document.getStringProperty(Constants.COMPONENT_PROCESSING_TIME));
            }
            requestProcessor.process(msg);
        }
        catch (Throwable thr) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_MSG_INVIEW), thr);
            }
            if(thr instanceof OutOfMemoryError && (errorHandler != null && !errorHandler.isInMemory()))
                System.exit(101);
    }
}

}