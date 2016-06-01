/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.runtime.swing;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: Dec 23, 2009
 * Time: 6:41:29 PM
 * To change this template use File | Settings | File Templates.
 */

public interface Bundle {
    /**
     * @msg.message msg="#"
     */
    public final static String INDEX = "index";
    /**
     * @msg.message msg="Sent                      "
     */
    public final static String SENT = "sent";
    /**
     * @msg.message msg="Message"
     */
    public final static String MESSAGE = "message";

    /**
     * @msg.message msg="Exception loading frame"
     */
    public final static String EROOR_LOADING_FRAME = "error_loading_frame";

    /**
     * @msg.message msg="Exception occured while sending message from Feeder"
     */
    public final static String EXCEP_SENDING_MESSAGE = "excep_sending_message";

    /**
     * @msg.message msg="Unable to load Default Message Panel "
     */
    public final static String LOAD_DEFAULT_FAIL = "load_default_fail";

    /**
     * @msg.message msg="Unable to load Header Panel "
     */
    public final static String LOAD_HEADER_FAIL = "load_header_fail";

    /**
     * @msg.message msg="Invalid History Size"
     */
    public final static String INVALID_HISTORY_SIZE = "invalid_history_size";

    /**
     * @msg.message msg="History size should be an integer greater than 1 and less than or equal to 1000."
     */
    public final static String INVALID_HISTORY_SIZE_DESC = "invalid_history_size_desc";

    /**
     * @msg.message msg="Unable to update data"
     */
    public final static String UPDATE_DATA_FAIL = "update_data_fail";

    /**
     * @msg.message msg="Invalid XML"
     */
    public final static String INVALID_XML = "invalid_xml";

    /**
     * @msg.message msg="Validation Successful"
     */
    public final static String VALIDATION_SUCCESSFUL = "validation_successful";

    /**
     * @msg.message msg="{0}: Invalid value: {0} for type: {1}"
     */
    public final static String INVALID_VALUE = "invalid_value";

    /**
     * @msg.message msg="Failed to encrypt input message."
     */
    String ENCRYPTION_FAILED = "encryption_failed";
}
