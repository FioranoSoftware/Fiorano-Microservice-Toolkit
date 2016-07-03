/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.runtime.receiver.service;

/**
 * @author FSIPL
 * @version 1.0
 * @created May 20, 2005
 */
public interface Bundle {

    /**
     * @msg.message msg="Total messages received for consumer {0} = {1} "
     */
     public final static String CONSUMER_TOTAL_MESSAGES = "consumer_total_messages";

    /**
     * @msg.message msg="Total messages received so far from all consumers "
     */
     public final static String ALL_CONSUMER_MESSAGES = "all_consumer_messages";

    /**
     * @msg.message msg="Initialized Receiver"
     */
     public final static String INIT_RECEIVER = "init_receiver";

     /**
      * @msg.message msg="Consumer {0} received {1} messages with rate = {2} messages per sec "
      */
     public final static String FINAL_CONSUMER_RATE = "final_consumer_rate";

    /**
     * @msg.message msg=" messages per sec "
     */
     public final static String MSGS_PER_SEC = "msgs_per_sec";

    /**
     * @msg.message msg="Committed session. "
     */
    public final static String COMMITTED_SESSION = "committed_session";

    /**
     * @msg.message msg="Error in Committing the Session {0}. Reason {1}"
     */
    public final static String COMMIT_ERROR = "commit_error";

    /**
     * @msg.message msg="Error in sleep to receive all the messages {0}"
     */
    public final static String SLEEP_ERROR = "sleep_error";

    /**
     * @msg.message msg="Configuration Parameters "
     */
    public final static String CONFIG_PARAMS = "config_params";

    /**
     * @msg.message msg="Total Messages = {0} "
     */
    public final static String TOTAL_MESSAGES = "total_messages";

    /**
     * @msg.message msg="Transaction Size = {0} "
     */
    public final static String TRANSACTION_SIZE = "transaction_size";

    /**
     * @msg.message msg="Is Transacted = {0} "
     */
    public final static String IS_TRANSACTED = "is_transacted";

    /**
     * @msg.message msg="Selector = {0} "
     */
    public final static String SELECTOR = "selector";

    /**
     * @msg.message msg="Sleep Interval = {0} milliseconds. "
     */
    public final static String SLEEP_INTERVAL = "sleep_interval";

    /**
     * @msg.message msg="Average receive rate {0}  messages per sec. "
     */
    public final static String AVERAGE_RATE = "average_rate";

    /**
     * @msg.message msg="All {0} messages have been received. "
     */
    public final static String ALL_MESSAGES_RECEIVED = "all_messages_received";

    /**
     * @msg.message msg=" Sleeping for {0} ms. "
     */
    public final static String SLEEPING_FOR = "sleeping_for";

    /**
     * @msg.message msg=" Arg[ {0} ] -> {1}. "
     */
     public final static String PRINT_ARGS = "print_args";
    /**
     * @msg.message msg="{0}"
     */
    public final static String EXCEPTION_MSG = "exception_msg";
    /**
     * @msg.message msg="Cannot create durable subscriber on destination {0}"
     */
    public final static String CANNOT_CREATE_DURABLE_SUBSCRIBER = "cannot_create_durable_subscriber";

    /**
     * @msg.message msg="Error creating Receiver Service"
     */
    public final static String ERROR_CREATING_SERVICE = "error_creating_service";

    /**
     * @msg.message msg="Process ID : {0}"
     */
    public final static String PROCESS_ID = "process_id";

    /**
     * @msg.message msg="Error logging process ID {0}"
     */
    public final static String ERROR_LOGGING_PROCESS_ID = "error_logging_process_id";

}
