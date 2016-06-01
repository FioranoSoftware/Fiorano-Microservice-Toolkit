/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.internal;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Launch configuration is not provided."
     */
    String LAUNCH_CONFIG_NOT_PROVIDED = "launch_config_not_provided";

    /**
     * @msg.message msg="Transport manager is not provided."
     */
    String TRANSPORT_MANAGER_NOT_PROVIDED = "transport_manager_not_provided";

    /**
     * @msg.message msg="Lookup helper is not provided."
     */
    String LOOKUP_HELPER_NOT_PROVIDED = "lookup_helper_not_provided";

    /**
     * @msg.message msg="Peer communications manager is not provided."
     */
    String PEER_COMMUNICATIONS_MANAGER_NOT_PROVIDED = "peer_communications_manager_not_provided";

    /**
     * @msg.message msg="Service is already configured with container details. Ignoring new details."
     */
    String SERVICE_ALREADY_CONFIGURED = "service_already_configured";

    /**
     * @msg.message msg="Process ID : {0}"
     */
    String PROCESS_ID = "process_id";

    /**
     * @msg.message msg="Error logging process ID : {0}"
     */
    String ERROR_LOGGING_PROCESS_ID = "error_logging_process_id";

    /**
     * @msg.message msg="Arg[{0}] -> {1}"
     */
    String RT_ARGS = "rt_args";

    /**
     * @msg.message msg="Failed to lookup configuration. This might have happened because component is not configured. "
     */
    String CONFIGURATION_LOOKUP_FAILED = "configuration_lookup_failed";

    /**
     * @msg.message msg="Failed to lookup manageable properties. Cannot load environment specific properties. Reason {0} "
     */
    String MANAGEABLE_PROPERTIES_LOOKUP_FAILED = "manageable_properties_lookup_failed";

    /**
     * @msg.message msg="Creating new configuration."
     */
    String CREATING_NEW_CONFIGURATION = "creating_new_configuration";

    /**
     * @msg.message msg="Configuration is valid."
     */
    String CONFIGURATION_VALID = "configuration_valid";

    /**
     * @msg.message msg="An exception occured while trying to stop the service. Cleanup might not have been done properly"
     */
    String EXCEPTION_ON_STOP = "exception_on_stop";

    /**
     * @msg.message msg="State of module {0} changed. Old state : {1}, new state: {2}"
     */
    String MODULE_STATE_CHANGE = "module_state_change";

    /**
     * @msg.message msg="Module {0} cannot move to state: {1}. Current state: {2} "
     */
    String MODULE_STATE_CHANGE_NOT_ALLOWED = "module_state_change_not_allowed";

    /**
     * @msg.message msg="Failed to lookup named configuration. Reason {0}"
     */
    String NAMED_CONFIG_LOOKUP_FAILED = "named_config_lookup_failed";

    /**
     * @msg.message msg="There is no license for component {0} or the license provided is invalid. Reason: {1}"
     */
    public final static String INVALID_LICENSE = "invalid_license";

    /**
     * @msg.message msg="The license for component {0} is valid."
     */
    public final static String VALID_LICENSE = "valid_license";
}
