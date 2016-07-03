/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Component state changed. State: {0}. Changed: {1} from {2} to {3}."
     */
    String COMPONENT_STATE_CHANGED = "component_state_changed";

    /**
     * @msg.message msg="Producer or session is not set. Not sending the event."
     */
    String EVENT_GENERATOR_NOT_READY = "event_generator_not_ready";

    /**
     * @msg.message msg="Sending event: {0}"
     */
    String SENDING_EVENT = "sending_event";

    /**
     * @msg.message msg="Failed to send CCP event. Reason - {0}"
     */
    String FAILED_TO_SEND_CCP_EVENT = "failed_to_send_ccp_event";

    /**
     * @msg.message msg="Failed to stop CCP Event Handler. Reason - {0}."
     */
    String CCP_EVENT_HANDLER_STOP_FAILED = "ccp_event_handler_stop_failed";

    /**
     * @msg.message msg="Successfullly started the CCP Event manager."
     */
    String CCP_EVENT_MANAGER_START_COMPLETE = "ccp_event_manager_start_complete";

    /**
     * @msg.message msg="Failed to start CCP Event Manager. Reason - {0}. Performing cleanup.."
     */
    String CCP_EVENT_MANAGER_START_FAILED = "ccp_event_manager_start_failed";

    /**
     * @msg.message msg="Error while handling CCP message. Reason - {0}"
     */
    String ERROR_HANDLING_CCP_MESSAGE = "error_handling_ccp_message";

    /**
     * @msg.message msg="Received event: {0}"
     */
    String RECEIVED_EVENT = "received_event";

    /**
     * @msg.message msg="Received shutdown command from the peer server. Correlation ID - {0}"
     */
    String RECEIVED_SHUTDOWN_COMMAND = "received_shutdown_command";

    /**
     * @msg.message msg="Unable to establish communication with Peer."
     */
    String FAILED_TO_START_COMMUNICATION_WITH_PEER = "failed_to_start_communication_with_peer";

    /**
     * @msg.message msg="Established communication with Peer."
     */
    String COMMUNICATION_WITH_PEER_ESTABLISHED = "communication_with_peer_established";

    /**
     * @msg.message msg="Closed communication with Peer."
     */
    String COMMUNICATION_WITH_PEER_CLOSED = "communication_with_peer_closed";

    /**
     * @msg.message msg="Unable to close communication with Peer."
     */
    String FAILED_TO_CLOSE_COMMUNICATION_WITH_PEER = "failed_to_close_communication_with_peer";
}
