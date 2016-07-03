/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

/**
 * Created by IntelliJ IDEA.
 * Date: Jan 25, 2007
 * Time: 4:22:48 PM
 *
 * @author
 */
public interface Bundle {

    /**
     * @msg.message msg="Value cannot be null for s KeyField"
     */
    String NULL_KEY_FIELD_VALUE = "null_key_field_value";

    /**
     * @msg.message msg="Name cannot be null for a Field"
     */
    String NULL_FIELD_NAME = "null_field_name";

    /**
     * @msg.message msg=" Cannot create field as value object of type {0} could not be created"
     */
    String CANNOT_CREATE_VALUE_OBJECT = "cannot_create_value_object";

    /**
     * @msg.message msg=" Field Definition cannot be null"
     */
    String INVALID_FIELD_DEFINITION = "invalid_field_definition";

    /**
     * @msg.message msg="Object of type {0} is not supported for value of Field"
     */
    String INVALID_FIELD_VALUE_CLASS = "invalid_field_value_class";

    /**
     * @msg.message msg="Invalid element found. Found: {0}. Required: {1}"
     */
    String INVALID_ELEMENT = "invalid_element";

    /**
     * @msg.message msg="Index: {0}"
     */
    String INDEX_OUT_OF_BOUNDS = "index_out_of_bounds";

}
