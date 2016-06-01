/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.internal.transport.IRequestListener;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 22-Nov-2010
 * Time: 19:37:30
 * To change this template use File | Settings | File Templates.
 */
public interface IJMSRequestListener extends IRequestListener<Message>, MessageListener {
}
