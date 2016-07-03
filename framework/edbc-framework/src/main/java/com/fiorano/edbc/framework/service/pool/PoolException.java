/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.pool;

import javax.jms.Message;

public class PoolException extends Exception {

    private Message request;

    public PoolException(Message request, Exception cause) {
        super(cause);
        this.request = request;
    }

    public Message getRequest() {
        return request;
    }
}
