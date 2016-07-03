/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

/**
 * Date: Mar 11, 2007
 * Time: 9:44:13 AM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Failed to unregister a helper class for CPS. ESB related operations will fail"
     */
    String CPS_UNREGISTER_FAILED = "cps_unregister_failed";

    /**
     * @msg.message msg="Failed to unregister configuration object."
     */
    String CONFIG_OBJECT_UNREGISTER_FAILED = "config_object_unregister_failed";

    /**
     * @msg.message msg="Failed to register a helper class for CPS. ESB related operations will fail"
     */
    String CPS_REGISTER_FAILED = "cps_register_failed";

    /**
     * @msg.message msg="Failed to register configuration object."
     */
    String CONFIG_OBJECT_REGISTER_FAILED = "config_object_register_failed";

    /**
     * @msg.message msg="Could not deserialize the configuration."
     */
    String DESERIALIZATION_FAILED = "deserialization_failed";

    /**
     * @msg.message msg="Could not serialize the configuration."
     */
    String SERIALIZATION_FAILED = "serialization_failed";

    /**
     * @msg.message msg="{0} [{1}:{2}] - Configuration"
     */
    String CPS_WINDOW_TITLE = "cps_window_title";

    /**
     * @msg.message msg="{0} [{1}:{2}] - Configuration (Running)"
     */
    String RUNNING_CPS_WINDOW_TITLE = "running_cps_window_title";

    /**
     * @msg.message msg="{0} [{1}:{2}] - Configuration (Read Only)"
     */
    String READONLY_CPS_WINDOW_TITLE = "readonly_cps_window_title";

    /**
     * @msg.message msg="Failed to create CPS UI"
     */
    String FAILED_TO_CREATE_CPS_UI = "failed_to_create_cps_ui";

    /**
     * @msg.message msg="Configuration property sheet is in read only mode. Press 'Cancel' to close it."
     */
    String READ_ONLY_WIZARD = "read_only_wizard";

    /**
     * @msg.message msg="Validate"
     */
    String VALIDATE = "validate";

    /**
     * @msg.message msg="Validation Successful."
     */
    String VALIDATION_SUCCESSFUL = "validation_successful";

    /**
     * @msg.message msg="Test Successful."
     */
    String TEST_SUCCESSFUL = "test_successful";

    /**
     * @msg.message msg="The configuration provided is invalid."
     */
    String CONFIG_INVALID = "config_invalid";

    /**
     * @msg.message msg="Test"
     */
    String TEST = "test";

    /**
     * @msg.message msg="Time between retries(ms)"
     */
    String RETRY_INTERVAL = "retry_interval";

    /**
     * @msg.message msg="Number of retries"
     */
    String RETRY_COUNT = "retry_count";

    /**
     * @msg.message msg="Actions during retries"
     */
    String ACTIONS_DURING_RETRES = "actions_during_retres";

    /**
     * @msg.message msg="Action"
     */
    String ACTION = "action";

    /**
     * @msg.message msg="Number of retries before action is performed"
     */
    String RETRIES_BEFORE_ACTION = "retries_before_action";

    /**
     * @msg.message msg="Errors"
     */
    String ERRORS = "errors";

    /**
     * @msg.message msg="Retry configuration"
     */
    String RETRY_CONFIGURATION = "retry_configuration";

    /**
     * @msg.message msg="Remedial actions"
     */
    String REMEDIAL_ACTIONS = "remedial_actions";

    /**
     * @msg.message msg="Failed to initialize custom editor: {0}"
     */
    String CUSTOM_EDITOR_INIT_FAILED = "custom_editor_init_failed";
}