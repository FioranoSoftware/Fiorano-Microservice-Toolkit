/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.bc.chat;

/**
 * Created by IntelliJ IDEA.
 * User: chaitanya
 * Date: Jan 31, 2008
 * Time: 11:39:12 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Bundle {
    /**
     * @msg.message msg="Received Message {0}"
     */
    public final static String RECIEVED_MESSAGE = "recieved_message";
    /**
     * @msg.message msg="Ignoring number format exception : {0}"
     */
    public final static String INGNORING_NFE = "ingnoring_nfe";

    /**
     * @msg.message msg="Ignoring Bad Location Exception : {0}"
     */
    public final static String INGNORING_BLE = "ingnoring_ble";

    /**
     * @msg.message msg="created the Session .... "
     */
    public final static String SESSION_CREATED = "session_created";

     /**
     * @msg.message msg=" Created producer on {0}."
     */
    public final static String PRODUCER_CREATED = "producer_created";

     /**
     * @msg.message msg=" Created consumer on {0}."
     */
    public final static String CONSUMER_CREATED = "consumer_created";

     /**
     * @msg.message msg="Error showing Chat UI"
     */
    public final static String ERROR_SHOWING_UI = "error_showing_ui";

    /**
     * @msg.message msg="JMSException Occured- "
     */
    public final static String EXCEPTION_MSG = "exception_msg";

    /**
     * @msg.message msg="Received a non text message {0}"
     */
    public final static String RCVD_NONTXT_MSG = "rcvd_nontxt_msg";

    /**
     * @msg.message msg="Exception occured while getting text"
     */
    public final static String ERROR_GETTING_TEXT = "error_getting_text";

    /**
     * @msg.message msg="Message Content : {0}"
     */
    public final static String MSG_CONTENT = "msg_content";

    /**
     * @msg.message msg="Exception occured while Parsing xmlMessage"
     */
    public final static String ERROR_PARSING_XML = "error_parsing_xml";

     /**
     * @msg.message msg="{0} JMS Properties: {1}"
     */
    public final static String JMS_PROPERTIES = "jms_properties";

    /**
     * @msg.message msg="Sent Message <MESSAGE>{0}</MESSAGE>"
     */
    public final static String SENT_MESSAGE = "sent_message";

     /**
     * @msg.message msg=" Unable to send the message {0} Reason {1}"
     */
    public final static String SEND_FAILED = "send_failed";

     /**
     * @msg.message msg="Fetched configuration {0}"
     */
    public final static String FETCHED_CONFIG = "fetched_config";

    /**
       * @msg.message msg="Arg[{0}] -> {1} "
       */
      public final static String RT_ARGS = "rt_args";

    /**
       * @msg.message msg="Look up failed"
       */
      public final static String LOOKUP_FAILED = "lookup_failed";

    /**
     * @msg.message msg="Error creating Chat Service"
     */
    public final static String ERROR_CREATING_SERVICE = "error_creating_service";

    /**
     * @msg.message msg="Error Initializing Identity Panel"
     */
    public final static String ERROR_INITIALISING_IDENTITY_PANEL= "error_initialising_identity_panel";

      /**
     * @msg.message msg="Error Initializing Settings Panel"
     */
    public final static String ERROR_INITIALISING_SETTINGS_PANEL= "error_initialising_settings_panel";
   /**
     * @msg.message msg="Cannot create durable subscriber on destination {0}"
     */
   public final static String CANNOT_CREATE_DURABLE_SUBSCRIBER = "cannot_create_durable_subscriber";

    /**
     * @msg.message msg="CHAT_FRAME_INIT_ERROR : chat frame couldnt be initiated : {0}"
     */
    public final static String CHAT_FRAME_INIT_ERROR = "chat_frame_init_error";

    /**
     * @msg.message msg="Display Name Missing"
     */
    public final static String NAME_MISSING = "name_missing";
    /**
     * @msg.message msg="Display Name is not specified."
     */
    public final static String NAME_MISSING_DESC = "name_missing_desc";
    /**
     * @msg.message msg="Email Missing"
     */
    public final static String EMAIL_MISSING = "email_missing";
    /**
     * @msg.message msg="Email is not specified."
     */
    public final static String EMAIL_MISSING_DESC = "email_missing_desc";
    /**
     * @msg.message msg="Invalid Email Address"
     */
    public final static String INVALID_EMAIL = "invalid_email";
    /**
     * @msg.message msg="Please check out the example email address"
     */
    public final static String INVALID_EMAIL_DESC = "invalid_email_desc";

    /**
     * @msg.message msg="Process ID : {0}"
     */
    public final static String PROCESS_ID = "process_id";

    /**
     * @msg.message msg="Error logging process ID {0}"
     */
    public final static String ERROR_LOGGING_PROCESS_ID = "error_logging_process_id";

    /**
     * @msg.message msg="Initial Context Created"
     */
    public final static String INITIAL_CONTEXT_CREATED = "initial_context_created";

    /**
     * @msg.message msg="Initial Context creation Failed"
     */
    public final static String INITIAL_CONTEXT_CREATION_ERROR = "initial_context_creation_error";
}

