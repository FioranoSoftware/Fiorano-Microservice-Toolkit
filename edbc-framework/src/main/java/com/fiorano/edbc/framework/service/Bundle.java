/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service;

/**
 * Date: Mar 8, 2007
 * Time: 12:36:16 AM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Failed to close context. Reason: {0}"
     */
    String CONTEXT_CLOSE_FAILED = "context_close_failed";

    /**
     * @msg.message msg="Component stopped."
     */
    String COMPONENT_STOPPED = "component_stopped";

    /**
     * @msg.message msg="An exception occured while trying to stop the service. Cleanup might not have been done properly"
     */
    String EXCEPTION_ON_STOP = "exception_on_stop";

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
     * @msg.message msg="Arg[{0}] -> {1}"
     */
    String RT_ARGS = "rt_args";

    /**
     * @msg.message msg="Process ID : {0}"
     */
    String PROCESS_ID = "process_id";

    /**
     * @msg.message msg="Error logging process ID : {0}"
     */
    String ERROR_LOGGING_PROCESS_ID = "error_logging_process_id";

    /**
     * @msg.message msg="Error while crating loggers"
     */
    String ERROR_WHILE_CREATING_LOGGERS = "ERROR_WHILE_CREATING_LOGGERS";

    /**
     * @msg.message msg="Stopping the service as an error occured when starting the service. Error: {0}"
     */
    String SERVICE_STARTUP_FAILED = "service_startup_failed";

    /**
     * @msg.message msg="Failed to lookup named configuration. Reason {0}"
     */
    String NAMED_CONFIG_LOOKUP_FAILED = "named_config_lookup_failed";

    /**
     * @msg.message msg="CCP must be enabled to use Transactions"
     */
    String CCP_FOR_TRANSACTION = "ccp_for_transaction";
}
