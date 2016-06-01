/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport.jms;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 22-Nov-2010
 * Time: 17:54:56
 * To change this template use File | Settings | File Templates.
 */
public interface Bundle extends com.fiorano.edbc.framework.service.internal.transport.Bundle {

    /**
     * @msg.message msg="An unexpected error has been encountered while processing the message."
     */
    String UNEXPECTED_ERROR = "unexpected_error";

    /**
     * @msg.message msg="Input message ackowledged message successfully."
     */
    String ACKNOWLEDGE_SUCCESS = "acknowledge_success";

    /**
     * @msg.message msg="Failed to ackowledge input message."
     */
    String ACKNOWLEDGE_FAILED = "acknowledge_failed";

    /**
     * @msg.message msg="Unable to prepare a response message to send to the output port."
     */
    String FAILED_TO_PREPARE_RESPONSE_MESSAGE = "failed_to_prepare_response_message";

    /**
     * @msg.message msg="Unable to create a response message to send to the output port."
     */
    String FAILED_TO_CREATE_RESPONSE_MESSAGE = "failed_to_create_response_message";

    /**
     * @msg.message msg="Response is sent on output port successfully."
     */
    String RESPONSE_SEND_SUCCESSFUL = "response_send_successful";

    /**
     * @msg.message msg="Received request. Message: {0}."
     */
    String RECEIVED_REQUEST = "received_request";

    /**
     * @msg.message msg="Sending response. Message: {0}."
     */
    String SENDING_RESPONSE = "sending_response";

    /**
     * @msg.message msg="Failed to extract message. Message: {0}."
     */
    String FAILED_TO_EXTRACT_REQUEST = "failed_to_extract_request";

    /**
     * @msg.message msg="Failed to decrypt input message."
     */
    String DECRYPTION_FAILED = "decryption_failed";

    /**
     * @msg.message msg="Agreement Id of received Message : {0}"
     */
    String AGREEMENT_ID_MESSAGE = "agreement_id_message";

    /**
     * @msg.message msg="New Consumer Created with MessageSelector as {0}"
     */
    String FILTER_CONSUMER_CREATED = "filter_consumer_created";

    /**
     * @msg.message msg="Main Consumer closed"
     */
    String DEFAULT_CONSUMER_CLOSED = "default_consumer_closed";

    /**
     * @msg.message msg="Input record cannot be null"
     */
    String INVALID_REQUEST = "invalid_request";
}
