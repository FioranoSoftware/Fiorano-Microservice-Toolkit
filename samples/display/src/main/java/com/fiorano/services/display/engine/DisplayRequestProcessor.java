/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.engine;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.internal.engine.IRequestValidator;
import com.fiorano.services.display.runtime.swing.DisplayFrame;

import javax.jms.Message;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 9 Nov, 2010
 * Time: 7:03:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayRequestProcessor extends Module implements IRequestProcessor<Message, Message> {
    private DisplayFrame displayFrame;

    public DisplayRequestProcessor(IModule parent, DisplayFrame displayFrame) {
        super(parent);
        this.displayFrame = displayFrame;
    }

    public Message process(Message request) throws ServiceExecutionException {
        displayFrame.addDocument(request);
        return request;
    }

    public IRequestValidator<Message> getRequestValidator() {
        return null;
    }
}
