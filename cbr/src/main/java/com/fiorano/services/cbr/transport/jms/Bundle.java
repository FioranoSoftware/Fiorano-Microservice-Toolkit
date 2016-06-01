/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.cbr.transport.jms;

/**
 * This class contains the messages
 * used by the service in logging or events
 *
 * @author chaitanya
 * @author Deepthi
 * @version 1.9, 21st Jul 2010
 * @created Fri, 23 Feb 2007
 */

public interface Bundle {

    /**
     * @msg.message msg="Exception occured while processing the message"
     */
    String ERROR_PROCESSING_MESSAGE = "error_processing_message";

    /**
     * @msg.message msg="Using FioranoMQ CBR"
     */
    String USING_FIORANOMQ_CBR = "using_fioranomq_cbr";

    /**
     * @msg.message msg="Received message."
     */
    String MESSAGE_RECEIVED = "message_received";

    /**
     * @msg.message msg="Invalid input message."
     */
    String INVALID_REQUEST = "invalid_request";

    /**
     * @msg.message msg="No application context defined."
     */
    String NO_APP_CONTEXT = "no_app_context";

    /**
     * @msg.message msg="Cannot create durable Subscriber on destination {0}."
     */
    String INVALID_DESTINATION_DURABLE_SUBSCRIPTION = "invalid_destination_durable_subscription";

    /**
     * @msg.message msg="Using Saxon XPath evaluator."
     */
    String USING_SAXON_XPATH_CBR = "using_saxon_xpath_cbr";

    /**
     * @msg.message msg="Transaction commit successful."
     */
    String COMMIT_TRANSACTION_SUCCESSFUL = "commit_transaction_successful";

    /**
     * @msg.message msg="Failed to commit transaction."
     */
    String COMMIT_TRANSACTION_FAILED = "commit_transaction_failed";

    /**
     * @msg.message msg="message satified condition: {1}, sent out on port: {0}"
     */
    String CONDITION_SATISFIED = "condition_satisfied";

    /**
     * @msg.message msg="None of the conditions are satisfied, message sent on OUT_FALSE port."
     */
    String ALL_CONDITION_FAILED = "all_condition_failed";

    /**
     * @msg.message msg="Arg[{0}] -> {1}."
     */

    String PRINT_ARGS = "print_args";
    /**
     * @msg.message msg="Configuration deserialized successfully ."
     */

    String DESERIALIZATION_DONE = "deserialization_done";
    /**
     * @msg.message msg="Failed to deserialize configuration ."
     */

    String FAILED_TO_DESERIALIZE = "failed_to_deserialize";
    /**
     * @msg.message msg="Failed to lookup configuration ."
     */

    String FAILED_TO_LOOKUP_CONFIGURATION = "failed_to_lookup_configuration";
    /**
     * @msg.message msg="Unable to close context ."
     */

    String UNABLE_TO_CLOSE_CONTEXT = "unable_to_close_context";
    /**
     * @msg.message msg="CBRService started successfully ."
     */

    String STARTUP = "startup";
    /**
     * @msg.message msg="CBRService shutdown ."
     */

    String SHUTDOWN = "shutdown";
    /**
     * @msg.message msg="JMSobjects created successfully ."
     */

    String JMS_OBJECTS_CREATED = "jms_objects_created";
    /**
     * @msg.message msg="Xpath on context will be applied for XPath: {0}"
     */

    String XPATH_ON_CONTEXT = "xpath_on_context";

    /**
     * @msg.message msg="Xpath on body will be applied for XPath: {0}"
     */
    String XPATH_ON_BODY = "xpath_on_body";

    /**
     * @msg.message msg="Input message processed successfully ."
     */
    String TRANSACTION_COMPLETED = "transaction_completed";

    /**
     * @msg.message msg="Time taken (milli sec) to process request: {0}."
     */
    String TRANSACTION_TIME = "transaction_time";

    /**
     * @msg.message msg="Could not create event publisher. User events will not be sent"
     */
    String FAILED_TO_CREATE_EVENT_PUBLISHER = "failed_to_create_event_publisher";

    /**
     * @msg.message msg="Process ID : {0}"
     */
    String PROCESS_ID = "process_id";

    /**
     * @msg.message msg="Error logging process ID {0}"
     */
    String ERROR_LOGGING_PROCESS_ID = "error_logging_process_id";

    /**
     * @msg.message msg="Failed to create Xpath evaluators. Reason : {0}"
     */
    String FAILED_TO_CREATE_XPATH_EVALUATORS = "failed_to_create_xpath_evaluators";
}
