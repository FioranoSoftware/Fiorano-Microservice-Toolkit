/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.jms;

/**
 * Date: Mar 12, 2007
 * Time: 7:09:44 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Failed to send error onto error port"
     */
    String SEND_ERROR_FAILED = "send_error_failed";

    /**
     * @msg.message msg=" Created consumer on {0} for the session {1}."
     */
    String CONSUMER_CREATED = "consumer_created";

    /**
     * @msg.message msg=" Unable to send the message to destination: {0} <REASON>{1}</REASON>"
     */
    String SEND_DEST_FAILED = "send_dest_failed";

    /**
     * @msg.message msg=" Naming exception while trying to lookup. Reason {0}. "
     */
    String NAMING_EXCEPTION = "naming_exception";

    /**
     * @msg.message msg=" Unable to create jms object <REASON>{0}</REASON>"
     */
    String JMS_OBJECT_CREATION_FAILED = "jms_object_creation_failed";

    /**
     * @msg.message msg=" Unable to start jms transport <REASON>{0}</REASON>"
     */
    String JMS_START_FAILED = "jms_start_failed";

    /**
     * @msg.message msg=" Unable to stop jms transport <REASON>{0}</REASON>"
     */
    String JMS_STOP_FAILED = "jms_stop_failed";

    /**
     * @msg.message msg=" Error in closing the connection  {0} <REASON>{1}</REASON>"
     */
    String ERROR_IN_CLOSE_CONNECTION = "error_in_close_connection";

    /**
     * @msg.message msg="Failed to lookup ON_EXCEPTION port, send to error port action will not work."
     */
    String EXCEPTION_PORT_LOOKUP_FAILED = "exception_port_lookup_failed";
    /**
     * @msg.message msg=" Created producer."
     */
    String PRODUCER_CREATED = "producer_created";

    /**
     * @msg.message msg="Ackowledged message successfully."
     */
    String ACKNOWLEDGE_SUCCESS = "acknowledge_success";

    /**
     * @msg.message msg="Failed to ackowledge message."
     */
    String ACKNOWLEDGE_FAILED = "acknowledge_failed";

    /**
     * @msg.message msg="An unexpected error has been encountered while processing the message."
     */
    String UNEXPECTED_ERROR = "unexpected_error";

    /**
     * @msg.message msg="Unable to fetch message text from input message."
     */
    String FAILED_TO_FETCH_REQUEST = "failed_to_fetch_request";

    /**
     * @msg.message msg="Unable to prepare a response message to send to the output port."
     */
    String FAILED_TO_PREPARE_RESPONSE_MESSAGE = "failed_to_prepare_response_message";

    /**
     * @msg.message msg="Unable to create a response message to send to the output port."
     */
    String FAILED_TO_CREATE_RESPONSE_MESSAGE = "failed_to_create_response_message";

    /**
     * @msg.message msg="Cannot create Durable Subscription as destination {0} is not a Topic."
     */
    String ERROR_DURABLE_SUBSCRIPTION = "error_durable_subscription";

    /**
     * @msg.message msg="Service is stopped, hence stopping error handling."
     */
    String STOPPING_ERROR_HANDLING = "stopping_error_handling";

    /**
     * @msg.message msg="Failed to discard connection"
     */
    String DISCARD_CONNECTION_FAILED = "discard_connection_failed";

    /**
     * @msg.message msg="Connection not found"
     */
    String CONN_NOT_FOUND = "conn_not_found";

    /**
     * @msg.message msg="Session cannot be null"
     */
    String SESSION_NULL = "session_null";

    /**
     * @msg.message msg="Error occured while closing message consumer Reason - {0}"
     */
    String ERROR_CLOSING_CONSUMER = "error_closing_consumer";

    /**
     * @msg.message msg="Performing error handling action - {0}"
     */
    String PERFORMING_ERROR_HANDLING = "performing_error_handling";

    /**
     * @msg.message msg="Error occurred while converting message into writable mode."
     */
    String ERROR_CONVERTING_MESSAGE = "error_converting_message";
    /**
     * @msg.message msg="Error occurred while converting message into writable mode."
     */
    String TXN_MESG_ID_MISSING = "txn_mesg_id_missing";

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
     * @msg.message msg="Error occured while closing session. Reason - {0}"
     */
    String ERROR_CLOSING_SESSION = "error_closing_session";

    /**
     * @msg.message msg="Failed to send the message. Message Text: {0}"
     */
    String ERROR_SENDING_MESSAGE = "error_sending_message";

    /**
     * @msg.message msg="Could not send messsage. Producer is null"
     */
    String NULL_VALUE_DEST_OR_PROD = "null_value_dest_or_prod";

    /**
     * @msg.message msg="Could not fetch the body of input message"
     */
    String ERROR_FETCHING_DATA = "error_fetching_data";

    /**
     * @msg.message msg="Could not create errorXML that should be sent"
     */
    String ERROR_GENERATING_ERROR_XML = "error_generating_error_xml";

    /**
     * @msg.message msg="Could not set the body of the TextMessage. Body to set: {0}"
     */
    String ERROR_SETTING_MESSAGE = "error_setting_message";
    /**
     * @msg.message msg="Retry Count : {0}"
     */
    String RETRY_COUNT = "retry_count";

    /**
     * @msg.message msg="Error occurred : Reason - {0}"
     */
    String ERROR_OCCURRED = "error_occurred";

    /**
     * @msg.message msg="Message \" {0} \" sent successfully."
     */
    String MESSAGE_SENT_SUCCESSFULLY = "message_sent_successfully";
}
