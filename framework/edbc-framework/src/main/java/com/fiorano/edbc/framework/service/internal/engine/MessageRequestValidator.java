/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.engine;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.MessageUtil;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 29-Jan-2011
 * Time: 14:04:50
 * To change this template use File | Settings | File Templates.
 */
public class MessageRequestValidator implements IRequestValidator<Message> {

    private IRequestValidator<String> stringRequestValidator;

    public MessageRequestValidator(IRequestValidator<String> stringRequestValidator) {
        this.stringRequestValidator = stringRequestValidator;
    }

    public void validate(Message request) throws ServiceExecutionException {
        try {
            stringRequestValidator.validate(MessageUtil.getTextData(request));
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_VALIDATE,
                    new String[]{e.getMessage()}), e, ServiceErrorID.INVALID_REQUEST_ERROR);
        }
    }
}
