/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.xmlverification.model;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Dec 8, 2006
 * Time: 1:56:09 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Bundle {

    /**
     * @msg.message msg="XSD Structures is specified as {0} but the schema for {1} is not specified"
     */
    String SCHEMA_NOT_PROVIDED = "schema_not_provided";

    /**
     * @msg.message msg="Schema specified for {0} is not valid"
     */
    String SCHEMA_NOT_VALID = "schema_not_valid";

    /**
     * @msg.message msg="Started Connection ..."
     */
    String CONNECTION_STARTED = "connection_started";

    /**
     * @msg.message msg="Schema specified for {0} is not valid. Reason: {1}"
     */
    String SCHEMA_NOT_VALID_REASON = "schema_not_valid_reason";

    /**
     * @msg.message msg="Expected value for XSD Structures is one of {Body, Context, Context-Body}. Value found {0}"
     */
    String INVALID_STRUCTURES = "invalid_structures";

    /**
     * @msg.message msg="error in finding root element {0}"
     */
    String ERROR_IN_FINDING_ROOT_ELEMENT = "error_in_finding_root_element";

    /**
     * @msg.message msg="Input XML is Valid {0}"
     */
    String INPUT_XML_IS_VALID = "input_xml_is_valid";

    /**
     * @msg.message msg="Error in validating xml {0} <REASON> {1} <REASON>"
     */
    String ERROR_IN_VALIDATING_XML = "error_in_validating_xml";

    /**
     * @msg.message msg="XMLVerification Service received message"
     */
    String RECEIVED_MESSAGE = "received_message";

    /**
     * @msg.message msg="Input message : {0}"
     */
    String INPUT_MESSAGE = "input_message";

    /**
     * @msg.message msg="Message validated. Sending document forward."
     */
    String VALIDATED_MESSAGE = "validated_message";

    /**
     * @msg.message msg="Message Invalid. Sending document on FAILED_PORT."
     */
    String INVALID_MESSAGE = "invalid_message";

    /**
     * @msg.message msg="Validating Input XML against the specified XSD..."
     */
    String VALIDATING_INPUT_XML = "validating_input_xml";

    /**
     * @msg.message msg="Verifying XSD : {0}"
     */
    String VERIFYING_XSD = "verifying_xsd";

    /**
     * @msg.message msg="Verifying XML : {0}"
     */
    String VERIFYING_XML = "verifying_xml";

    /**
     * @msg.message msg="Invalid Root Element Specified : {0}"
     */
    String INVALID_ROOT_ELEMENT = "invalid_root_element";

    /**
     * @msg.message msg="Time taken (milli sec) to process request: {0}."
     */
    String TRANSACTION_TIME = "transaction_time";

    /**
     * @msg.message msg="Could not create event publisher. User events will not be sent"
     */
    String FAILED_TO_CREATE_EVENT_PUBLISHER = "failed_to_create_event_publisher";

    /**
     * @msg.message msg="No elements defined."
     */
    String NO_ELEMENTS = "no_elements";

    //Attributes

    /**
     * @msg.message msg="Structure for Message Body"
     */
    String BODY_NAME = "body_name";
    /**
     * @msg.message msg="The XSD/DTD with which content of message body has to be validated."
     */
    String BODY_DESC = "body_desc";

    /**
     * @msg.message msg="Structure for Application Context"
     */
    String CONTEXT_NAME = "context_name";
    /**
     * @msg.message msg="The XSD/DTD with which content of application context has to be validated."
     */
    String CONTEXT_DESC = "context_desc";

    /**
     * @msg.message msg="Source(s) of content to validate"
     */
    String XSD_STRUCTURES_NAME = "xsd_structures_name";
    /**
     * @msg.message msg="Select \"Body\" if the content of message body alone has to be validated.
     * +     * Select \"Context\" if the content of Application Context alone has to be validated.
     * +     * Select \"Context-Body\" if the verification has to be applied on the both Body and Application Context of input message."
     */
    String XSD_STRUCTURES_DESC = "xsd_structures_desc";

    /**
     * @msg.message msg="Input Xml is either not provided or Empty"
     */
    String EMPTY_INPUT_XML = "empty_input_xml";

    /**
     * @msg.message msg="Application Context is either not provided or Empty"
     */
    String EMPTY_CONTEXT_XML = "empty_context_xml";
}
