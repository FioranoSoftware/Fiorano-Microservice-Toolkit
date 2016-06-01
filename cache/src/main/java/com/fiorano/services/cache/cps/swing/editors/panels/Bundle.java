/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.swing.editors.panels;

/**
 * Created by IntelliJ IDEA.
 * Date: Jan 25, 2007
 * Time: 4:22:48 PM
 *
 * @author
 */
public interface Bundle {

    /**
     * @msg.message msg="Field Definition Table"
     */
    String FIELD_DEFINITION_TABLE = "field_definition_table";

    /**
     * @msg.message msg="Add"
     */
    String ADD = "add";

    /**
     * @msg.message msg="Remove"
     */
    String REMOVE = "remove";

    /**
     * @msg.message msg="Name"
     */
    String NAME = "name";

    /**
     * @msg.message msg="Key"
     */
    String KEY = "key";

    /**
     * @msg.message msg="XSD"
     */
    String XSD = "xsd";

    /**
     * @msg.message msg="Type"
     */
    String TYPE = "type";

    /**
     * @msg.message msg="Unable to set port schema"
     */
    String UNABLE_TO_SET_PORT_XSD = "unable_to_set_port_xsd";

    /**
     * @msg.message msg="Field name contains invalid characters. Field name can contain only alpha numeric characters and _"
     */
    String INVALID_FIELD_NAME = "invalid_field_name";

    /**
     * @msg.message msg="A field with specified name already exists"
     */
    String DUPLICATE_FIELD_NAME = "duplicate_field_name";

    /**
     * @msg.message msg="Column index cannot be less than 0"
     */
    String INVALID_COLUMN_INDEX = "invalid_column_index";
}
