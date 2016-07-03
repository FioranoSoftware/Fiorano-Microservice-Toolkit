/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps.swing.panels;

/**
 * Constants used for logging
 *
 * @author FSIPL
 */
public interface Bundle {

    /**
     * @msg.message msg="Error in schema validation. Reason: {0}"
     */
    public final static String SCHEMA_VALIDATION_ERROR = "schema_validation_error";

    /**
     * @msg.message msg="Schema Validation Failed. Reason: {0}"
     */
    public final static String SCHEMA_VALIDATION_FAILED = "schema_validation_failed";

    /**
     * @msg.message msg="Error in DTD specified. Reason: {0}"
     */
    public final static String ERROR_IN_DTD = "error_in_dtd";

    /**
     * @msg.message msg="Error in xsd specified. Reason: {0}"
     */
    public final static String ERROR_IN_XSD = "error_in_xsd";

    /**
     * @msg.message msg="model is null"
     */
    public final static String MODEL_NULL = "model_null";

}
