/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xmlverification;

public interface Bundle {

    /**
     * @msg.message msg="Error creating {0} panel"
     */
    public final static String ERROR_CREATING_PANEL = "error_creating_panel";

    /**
     * @msg.message msg="Error creating CPS Helper Reason : {0}"
     */
    public final static String ERROR_CREATING_CPSHELPER = "error_creating_cpshelper";

    /**
     * @msg.message msg="Exception while processing the message"
     */
    public final static String EXCEP_PROCESSING = "excep_processing";

    /**
     * @msg.message msg="Invalid configuration - No schema provided."
     */
    public final static String INVALID_CONFIG = "invalid_config";

}
