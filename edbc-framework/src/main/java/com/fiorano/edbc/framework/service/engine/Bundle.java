/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.engine;

/**
 * Date: Mar 8, 2007
 * Time: 10:54:16 AM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Input xml validation is enabled but there is no schema to validate against."
     */
    String NO_SCHEMA_TO_VALIDATE = "no_schema_to_validate";

    /**
     * @msg.message msg="Invalid input xml received."
     */
    String INVALID_REQUEST = "invalid_request";

    /**
     * @msg.message msg="Failed to decrypt input message."
     */
    String DECRYPTION_FAILED = "decryption_failed";

    /**
     * @msg.message msg="Failed to encrypt input message."
     */
    String ENCRYPTION_FAILED = "encryption_failed";
}
