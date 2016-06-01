/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt;

/**
 * This class contains the messages used by the service in logging
 * or events.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @created Thu, 22 Mar 2007
 */
public interface Bundle {

    /**
     * @msg.message msg="Sent Message to destination: {0}. Message: {1}"
     */
    String SENT_MESSAGE = "sent_message";

    /**
     * @msg.message msg=" Created producer."
     */
    String PRODUCER_CREATED = "producer_created";

    /**
     * @msg.message msg=" Naming exception while trying to lookup. Reason {0}. "
     */
    String NAMING_EXCEPTION = "naming_exception";

    /**
     * @msg.message msg=" Unable to send the message to destination: {0} <REASON>{1}</REASON>"
     */
    String SEND_DEST_FAILED = "send_dest_failed";

    /**
     * @msg.message msg=" Unable to create jms object <REASON>{0}</REASON>"
     */
    String JMS_OBJECT_CREATION_FAILED = "jms_object_creation_failed";

    /**
     * @msg.message msg=" Error in closing the connection  {0} <REASON>{1}</REASON>"
     */
    String ERROR_IN_CLOSE_CONNECTION = "error_in_close_connection";

    /**
     * @msg.message msg="service configuration is invalid."
     */
    String INVALID_CONFIGURATION = "invalid_configuration";

    /**
     * @msg.message msg="Processed the input request. {0}"
     */
    String REQUEST_PROCESSED = "request_processed";

    /**
     * @msg.message msg="Output XML Obtained"
     */
    String OUTPUT_MESSAGE = "output_message";

    /**
     * @msg.message msg="Processing Message..."
     */
    String PROCESSING_MESSAGE = "processing_message";

    /**
     * @msg.message msg="Created templates object"
     */
    String TEMPLATES_INIT_SUCCESS = "templates_init_success";

    /**
     * @msg.message msg="Could not create templates object. Reason: {0}"
     */
    String TEMPLATES_INIT_FAILED = "templates_init_failed";

    /**
     * @msg.message msg="Could not complete tranformation. Reason: {0}"
     */
    String TRANSFORM_FAILED = "transform_failed";

    /**
     * @msg.message msg="Failed to set parameters for transformer. Reason: {0}"
     */
    String PARAMETERS_LOAD_FAILED = "parameters_load_failed";

    /**
     * @msg.message msg="Failed to set transformer parameter {0}"
     */
    String PARAMETER_LOAD_FAILED = "parameter_load_failed";

    /**
     * @msg.message msg="Set the transformer parameter {0}"
     */
    String PARAMETER_LOAD_SUCCESFUL = "parameter_load_succesful";

    /**
     * @msg.message msg="All the parameters are set successfully"
     */
    String PARAMETERS_LOAD_SUCCESFUL = "parameters_load_succesful";

    /**
     * @msg.message msg="Application Context is empty"
     */
    String EMPTY_CONTEXT = "empty_context";

    /**
     * @msg.message msg="XSL not specified for transformation"
     */
    String XSL_NOT_SPECIFIED = "xsl_not_specified";

    /**
     * @msg.message msg="XSL specifed is not valid"
     */
    String XSL_NOT_VALID = "xsl_not_valid";

    /**
     * @msg.message msg="Input record is null"
     */
    String NULL_INPUT_RECORD = "null_input_record";


    /**
     * @msg.message msg="Failed to fetch context. Reason: {0}. "
     */
    String FAILED_TO_FETCH_CONTEXT = "failed_to_fetch_context";
    /**
     * @msg.message msg="Empty Transformer class "
     */
    String EMPTY_TRANSFORMER_CLASS = "empty_transformer_class";

    /**
     * @msg.message msg="Could not create event publisher. User events will not be sent"
     */
    String FAILED_TO_CREATE_EVENT_PUBLISHER = "failed_to_create_event_publisher";

    /**
     * @msg.message msg="Failed to convert 4.0 configuration to 5.0. Please reconfigure the service"
     */
    String PLEASE_RECONFIG = "please_reconfig";

    /**
     * @msg.message msg="Failed to set correlation ID with value {0}"
     */
    String FAILED_SET_ID = "failed_set_id";

    /**
     * @msg.message msg="Failed to set text with value {0}"
     */
    String FAILED_SET_TEXT = "failed_set_text";

    /**
     * @msg.message msg="Failed to remove property {0}"
     */
    String FAILED_REMOVE_PROPERTY = "failed_remove_property";

    /**
     * @msg.message msg="Failed to set property {0} with value {1}"
     */
    String FAILED_SET_PROP = "failed_set_prop";

    /**
     * @msg.message msg="Failed to initialize"
     */
    String FAILED_INITIALIZE = "failed_initialize";

    /**
     * @msg.message msg="using TransformerFactory: {0}"
     */
    String USING_TRANS_FACT = "using_trans_fact";
}
