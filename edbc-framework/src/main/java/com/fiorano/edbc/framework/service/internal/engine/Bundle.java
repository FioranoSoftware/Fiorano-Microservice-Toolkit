/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.engine;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 27-Jan-2011
 * Time: 16:01:12
 * To change this template use File | Settings | File Templates.
 */
public interface Bundle {

    /**
     * @msg.message msg="XML definition type {0} is unkown. Validation of request cannot be done."
     */
    static final String XML_DEFINITION_TYPE_UNKOWN = "xml_definition_type_unkown";

    /**
     * @msg.message msg="Unable to create parser for validating. Reason: {0}"
     */
    static final String PARSER_CREATION_FAILED_FOR_VALIDATION = "parser_creation_failed_for_validation";

    /**
     * @msg.message msg="Invalid input received."
     */
    static final String INVALID_REQUEST = "invalid_request";

    /**
     * @msg.message msg="Unable to validate. Reason: {0}"
     */
    static final String UNABLE_TO_VALIDATE = "unable_to_validate";
}
