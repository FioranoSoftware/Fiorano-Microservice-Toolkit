/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.error;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 10, 2010
 * Time: 3:13:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Bundle {
    /**
     * @msg.message msg="Service is stopped, hence stopping error handling."
     */
    public static final String STOPPING_ERROR_HANDLING = "stopping_error_handling";

    /**
     * @msg.message msg="Failed to send error onto error port"
     */
    public final static String SEND_ERROR_FAILED = "send_error_failed";

    /**
     * @msg.message msg="Failed to discard connection"
     */
    public final static String DISCARD_CONNECTION_FAILED = "discard_connection_failed";

    /**
     * @msg.message msg="Unable to stop the service."
     */
    public final static String SERVICE_STOP_FAILED = "service_stop_failed";

    /**
     * @msg.message msg="Action {0} not supported"
     */
    public final static String ACTION_NOT_SUPPORTED = "action_not_supported";
    /**
     * @msg.message msg="Performing error handling action - {0}"
     */
    public final static String PERFORMING_ERROR_HANDLING = "performing_error_handling";

    /**
     * @msg.message msg="Retry Count : {0}"
     */
    public final static String RETRY_COUNT = "retry_count";

    /**
     * @msg.message msg="Error occurred : Reason - {0}"
     */
    public final static String ERROR_OCCURRED = "error_occurred";

}
