/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.runtime.sender.service;

/**
 * This interface contains Bundle strings which are used for logging pupose.
 * @author FSIPL
 */
public interface Bundle {

    /**
     * @msg.message msg="Initialized Sender"
     */
     public final static String INITIALIZED_SENDER = "initialized_sender";

    /**
     * @msg.message msg="Exception in join for producer thread. Reason  {0} "
     */
     public final static String ERROR_IN_JOIN = "error_in_join";

    /**
     * @msg.message msg="Exception in sending messages. Reason  {0}  {1} "
     */
     public final static String SEND_ERROR = "send_error";

    /**
     * @msg.message msg="Exception in processing the input XML file. Reason  {0}  {1} "
     */
     public final static String XML_PROCESS_ERROR = "xml_process_error";

    /**
     * @msg.message msg="Completed reading the XML file "
     */
     public final static String XML_FILE_READ = "xml_file_read";

    /**
     * @msg.message msg=" Arg[ {0} ] -> {1}. "
     */
     public final static String PRINT_ARGS = "print_args";

    /**
     * @msg.message msg="Looked up destination {0} "
     */
     public final static String LOOKED_UP_DESTINATION = "looked_up_destination";

     /**
     * @msg.message msg="Total Messages = {0} "
     */
    public final static String TOTAL_MESSAGES = "total_messages";

    /**
     * @msg.message msg="Transaction Size = {0} "
     */
    public final static String TRANSACTION_SIZE = "transaction_size";

    /**
     * @msg.message msg="Message size = {0} Bytes"
     */
     public final static String MESSAGE_SIZE = "message_size";

    /**
     * @msg.message msg="Configuration Parameters. "
     */
     public final static String CONFIG_PARAMS = "config_params";

    /**
     * @msg.message msg="Publish rate for producer {0} =  {1} messages per sec."
     */
     public final static String PUBLISH_RATE = "publish_rate";

    /**
     * @msg.message msg="Average publish rate = {0} messages per second"
     */
     public final static String AVG_PUBLISH_RATE = "avg_publish_rate";

    /**
     * @msg.message msg="Sent {0} messages. "
     */
     public final static String SENT_MESSAGES = "sent_messages";

    /**
     * @msg.message msg="Sent all {0} messages. "
     */
    public final static String SENT_ALL_MESSAGES = "sent_all_messages";

    /**
     * @msg.message msg="Committing the session "
     */
    public final static String COMMITTING_SESSION = "committing_session";

    /**
     * @msg.message msg="Error creating Sender Service"
     */
    public final static String ERROR_CREATING_SERVICE = "error_creating_service";

    /**
     * @msg.message msg="if clause"
     */
    public final static String IF_CLAUSE = "if_clause";

    /**
     * @msg.message msg=" Delivery Mode {0} Priority {1} Time To Live {2}"
     */
    public final static String JMS_PROPERTIES_DESC = "jms_properties_desc";

    /**
     * @msg.message msg="{0} JMS Properties: {1}"
     */
    public final static String JMS_PROPERTIES = "jms_properties";

    /**
     * @msg.message msg="Process ID : {0}"
     */
    public final static String PROCESS_ID = "process_id";

    /**
     * @msg.message msg="Error logging process ID {0}"
     */
    public final static String ERROR_LOGGING_PROCESS_ID = "error_logging_process_id";

    /**
     * @msg.message msg="input is read from the file {0}"
     */
    public final static String INPUT_IS_READ_FROM_FILE = "input_is_read_from_file";


    /**
     * @msg.message msg="Generating input message"
     */
    public final static String INPUT_STRING_IS_GENERATED = "input_string_is_generated";

    /**
     * @msg.message msg="message size cannot be negative "
     */
    public final static String MESSAGE_SIZE_CANNOT_BE_NEGATIVE = "message_size_cannot_be_negative";

    /**
     * @msg.message msg="Created initial context."
     */
    static final String INITIAL_CONTEXT_CREATED = "initial_context_created";

    /**
     * @msg.message msg="Failed to create Initial context. Reason: {0}."
     */
    static final String INITIAL_CONTEXT_CREATION_ERROR = "initial_context_creation_error";

    /**
     * @msg.message msg="Failed to lookup {0}. Reason: {1}."
     */
    static final String LOOKUP_FAILED = "lookup_failed";



}
