/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal.transport;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Failed to commit transport - {0}. Reason: {1}"
     */
    static final String FAILED_TO_COMMIT_TRANSPORT = "failed_to_commit_transport";

    /**
     * @msg.message msg="Failed to initialize input transport. Reason: {0}"
     */
    static final String FAILED_TO_INITIALIZE_INPUT_TRANSPORT = "failed_to_initialize_input_transport";

    /**
     * @msg.message msg="Failed to close input transport. Reason: {0}"
     */
    static final String FAILED_TO_DESTROY_INPUT_TRANSPORT = "failed_to_destroy_input_transport";

    /**
     * @msg.message msg="Input transport initialized successfully."
     */
    static final String INPUT_TRANSPORT_INITIALIZED = "input_transport_initialized";

    /**
     * @msg.message msg="Closed input transport."
     */
    static final String INPUT_TRANSPORT_DESTROYED = "input_transport_destroyed";

    /**
     * @msg.message msg="Failed to initialize output transport. Reason: {0}"
     */
    static final String FAILED_TO_INITIALIZE_OUTPUT_TRANSPORT = "failed_to_initialize_output_transport";

    /**
     * @msg.message msg="Failed to close output transport. Reason: {0}"
     */
    static final String FAILED_TO_DESTROY_OUTPUT_TRANSPORT = "failed_to_destroy_output_transport";

    /**
     * @msg.message msg="Output transport initialized successfully."
     */
    static final String OUTPUT_TRANSPORT_INITIALIZED = "output_transport_initialized";

    /**
     * @msg.message msg="Closed output transport."
     */
    static final String OUTPUT_TRANSPORT_DESTROYED = "output_transport_destroyed";

    /**
     * @msg.message msg="Failed to initialize error transport. Reason: {0}"
     */
    static final String FAILED_TO_INITIALIZE_ERROR_TRANSPORT = "failed_to_initialize_error_transport";

    /**
     * @msg.message msg="Failed to close error transport. Reason: {0}"
     */
    static final String FAILED_TO_DESTROY_ERROR_TRANSPORT = "failed_to_destroy_error_transport";

    /**
     * @msg.message msg="Error transport initialized successfully."
     */
    static final String ERROR_TRANSPORT_INITIALIZED = "error_transport_initialized";

    /**
     * @msg.message msg="Closed error transport."
     */
    static final String ERROR_TRANSPORT_DESTROYED = "error_transport_destroyed";

    /**
     * @msg.message msg="Failed to send error response. Reason: {0}"
     */
    static final String FAILED_TO_SEND_ERR_RESPONSE = "failed_to_send_err_response";

    /**
     * @msg.message msg="Failed to create response. Reason: {0}"
     */
    static final String FAILED_TO_CREATE_RESPONSE = "failed_to_create_response";

    /**
     * @msg.message msg="Failed to send response. Reason: {0}"
     */
    static final String FAILED_TO_SEND_RESPONSE = "failed_to_send_response";

    /**
     * @msg.message msg="Failed to set request processor. Reason: {0}"
     */
    static final String FAILED_TO_SET_REQUEST_PROCESSOR = "failed_to_set_request_processor";

    /**
     * @msg.message msg="Unable to start transport layer. Reason: {0}"
     */
    static final String FAILED_TO_START_TRANSPORT = "failed_to_start_transport";

    /**
     * @msg.message msg="Unable to create transport layer. Reason: {0}"
     */
    static final String FAILED_TO_INITIALIZE_TRANSPORT = "failed_to_initialize_transport";

    /**
     * @msg.message msg="Unable to stop transport layer. Reason: {0}"
     */
    static final String FAILED_TO_STOP_TRANSPORT = "failed_to_stop_transport";

    /**
     * @msg.message msg="Stopped transport layer."
     */
    static final String TRANSPORT_STOPPED = "transport_stopped";

    /**
     * @msg.message msg="Transport layer closed successfully."
     */
    static final String TRANSPORT_DESTROYED = "transport_destroyed";

    /**
     * @msg.message msg="Unable to close transport layer. Reason: {0}"
     */
    static final String FAILED_TO_DESTROY_TRANSPORT = "failed_to_destroy_transport";
}
