/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.pool;

import javax.jms.Message;
import java.util.List;
import java.util.Map;

public class RequestResponse {

    private Message request;
    private Map<String, List<Message>> responses;
    private Message response;

    public RequestResponse(Message request, Message response) {
        this.request = request;
        this.response = response;
    }

    public RequestResponse(Message request, Map<String, List<Message>> responses) {
        this.request = request;
        this.responses = responses;
    }

    public Map<String, List<Message>> getResponses() {
        return responses;
    }

    public Message getRequest() {
        return request;
    }

    public Message getResponse() {
        return response;
    }
}
