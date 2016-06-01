/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.bcgateway;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jul 26, 2005
 * Time: 11:00:37 AM
 * To change this template use File | Settings | File Templates.
 *
 * @author FSIPL
 * @version 1.0
 * @created July 26, 2005
 */
public interface Bundle {
    /**
     * @msg.message msg="The component is not configured. Please launch CPS to configure the component"
     */
    public final static String NO_CONFIG_PROVIDED = "no_config_provided";
    /**
     * @msg.message msg="Failed to lookup manageable properties. Cannot load environment specific properties. Reason {0} "
     */
    public final static String MANAGEABLE_PROPERTIES_LOOKUP_FAILED = "manageable_properties_lookup_failed";

    /**
     * @msg.message msg="Error doing lookup for configuration"
     */
    public final static String ERROR_LOOKUP = "error_lookup";

    /**
     * @msg.message msg="Set the BpelProcessFile system property as : {0}"
     */
    public final static String SET_BPEL = "set_bpel";

    /**
     * @msg.message msg="Error starting TR Gateway"
     */
    public final static String ERROR_TR_GATEWAY = "error_tr_gateway";

    /**
     * @msg.message msg="Error looking up component configuration. Reason : {0}"
     */
    public final static String ERROR_COMP_CONFIG = "error_comp_config";

    /**
     * @msg.message msg="Trying to load default configurations..."
     */
    public final static String LOAD_DEFAULT = "load_default";

    /**
     * @msg.message msg="Default component configurations loaded successfully."
     */
    public final static String LOADED_SUCCESSFULLY = "loaded_successfully";

    /**
     * @msg.message msg="Error Serializing BC configuration"
     */
    public final static String ERROR_SERIALIZING = "error_serializing";

    /**
     * @msg.message msg="Process ID : {0}"
     */
    public final static String PROCESS_ID = "process_id";

    /**
     * @msg.message msg="Error logging process ID {0}"
     */
    public final static String ERROR_LOGGING_PROCESS_ID = "error_logging_process_id";

    /**
     * @msg.message msg="Unable to start CCP. Reason : {0}"
     */
    public final static String ERROR_IN_STARTING_CCP = "error_in_starting_ccp";
}
