/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.connection;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Connection established successfully."
     */
    String CONNECTION_CREATED_SUCCESSFULLY = "connection_created_successfully";

    /**
     * @msg.message msg="Unable to establish connection. Reason: {0}"
     */
    String CONNECTION_CREATION_FAILED = "connection_creation_failed";

    /**
     * @msg.message msg="Unable to close connection. Reason: {0}"
     */
    String CONNECTION_CLOSE_FAILED = "connection_close_failed";

    /**
     * @msg.message msg="Connection is closed successfully. Reason: {0}"
     */
    String CONNECTION_CLOSE_SUCCESSFUL = "connection_close_successful";

    /**
     * @msg.message msg="Unable to validate connection, treating the connection as invalid. Reason: {0}"
     */
    String CONNECTION_VALIDATION_FAILED = "connection_validation_failed";

    /**
     * @msg.message msg="Connection is valid"
     */
    String CONNECTION_VALIDATION_SUCCESSFUL = "connection_validation_successful";

}
