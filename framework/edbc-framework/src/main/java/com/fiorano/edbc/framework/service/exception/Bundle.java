/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

/**
 * Date: Mar 13, 2007
 * Time: 2:21:51 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Invalid action id for retry. {0}"
     */
    public final static String INVALID_RETRY_ACTION = "invalid_retry_action";

    /**
     * @msg.message msg="Number of retries should be either -1 (infinite) or greater than 0 (Zero). Specified value: {0}"
     */
    public final static String INVALID_RETRY_COUNT = "invalid_retry_count";

    /**
     * @msg.message msg="Time interval after which retry should be attempted should be greater than 0(Zero). Specified value: {0}"
     */
    public final static String INVALID_RETRY_INTERVAL = "invalid_retry_interval";

    /**
     * @msg.message msg="Action is invalid. Specified value: {0}"
     */
    public final static String INVALID_ACTION = "invalid_action";

    /**
     * @msg.message msg="A retry action con not contain retry action"
     */
    public final static String RECURSIVE_RETRY = "recursive_retry";

    /**
     * @msg.message msg="Invalid retries before action for {0}. Value: {1}. Expected range: 1 to {2}"
     */
    public final static String INVALID_OTHER_ACTIONS_RETRIES = "invalid_other_actions_retries";

    /**
     * @msg.message msg="Log to error logs"
     */
    public final static String LOG_TO_ERROR_LOGS_NAME = "log_to_error_logs_name";

    /**
     * @msg.message msg="Logs the exception and trace to error logs."
     */
    public final static String LOG_TO_ERROR_LOGS_DESC = "log_to_error_logs_desc";

    /**
     * @msg.message msg="Send to error port"
     */
    public final static String SEND_TO_ERROR_PORT_NAME = "send_to_error_port_name";

    /**
     * @msg.message msg="Send the error message on the error port."
     */
    public final static String SEND_TO_ERROR_PORT_DESC = "send_to_error_port_desc";

    /**
     * @msg.message msg="Re-execute request"
     */
    public final static String RE_EXECUTE_REQUEST_NAME = "re_execute_request_name";

    /**
     * @msg.message msg="Execute the request after the specified intervals of time until execution is successful."
     */
    public final static String RE_EXECUTE_REQUEST_DESC = "re_execute_request_desc";

    /**
     * @msg.message msg="Try reconnection"
     */
    public final static String TRY_RECONNECTION_NAME = "try_reconnection_name";

    /**
     * @msg.message msg="Reconnect and execute the request."
     */
    public final static String TRY_RECONNECTION_DESC = "try_reconnection_desc";

    /**
     * @msg.message msg="Throw fault on warnings"
     */
    public final static String THROW_FAULT_ON_WARNINGS_NAME = "throw_fault_on_warnings_name";

    /**
     * @msg.message msg="Generate error for the warnings encountered."
     */
    public final static String THROW_FAULT_ON_WARNINGS_DESC = "throw_fault_on_warnings_desc";

    /**
     * @msg.message msg="Stop service"
     */
    public final static String STOP_SERVICE_NAME = "stop_service_name";

    /**
     * @msg.message msg="Stop service execution."
     */
    public final static String STOP_SERVICE_DESC = "stop_service_desc";

    /**
     * @msg.message msg="Process invalid request"
     */
    public final static String PROCESS_INVALID_REQUEST_NAME = "process_invalid_request_name";

    /**
     * @msg.message msg="Do not stop processing in case request is invalid, continue processing."
     */
    public final static String PROCESS_INVALID_REQUEST_DESC = "process_invalid_request_desc";

    /**
     * @msg.message msg="Discard Connection"
     */
    public final static String DISCARD_CONNECTION_NAME = "discard_connection_name";

    /**
     * @msg.message msg="Discard the connection"
     */
    public final static String DISCARD_CONNECTION_DESC = "discard_connection_desc";
}
