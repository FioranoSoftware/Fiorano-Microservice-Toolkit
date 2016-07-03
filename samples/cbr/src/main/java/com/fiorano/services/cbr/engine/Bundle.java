/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

/**
 * Created by IntelliJ IDEA.
 * Date: Aug 26, 2007
 * Time: 7:20:48 PM
 *
 * @author Venkat
 * @author Deepthi
 * @version 1.4, 19 January 2010
 */
public interface Bundle {

    /**
     * @msg.message msg="Failed to add an evaluator to Firano CBR. Port: {0} Condition: {0}"
     */
    String EVALUATOR_ADDITION_FAILED = "evaluator_addition_failed";

    /**
     * @msg.message msg="Unable to evaluate message for condition."
     */
    String EVALUATE_FAILED = "evaluate_failed";

    /**
     * @msg.message msg="XPath evaluator added for xpath : {0} ."
     */
    String XPATH_EVALUATOR_ADDED = "xpath_evaluator_added";

    /**
     * @msg.message msg="XSLT evaluator added on {0} for xpaths: {1} ."
     */
    String XSLT_EVALUATOR_ADDED = "xslt_evaluator_added";

    /**
     * @msg.message msg="Messages satisfying xpath : {0} will be sent on port : {1} ."
     */
    String MESSAGES_SATISFYING_XPATH = "messages_satisfying_xpath";

    /**
     * @msg.message msg="Using xpath1.0 ."
     */
    String USING_XPATH1_0 = "using_xpath1_0";

    /**
     * @msg.message msg="Exception while creating the Transformer."
     */
    String TRANSFORMER_CREATION_EXCEPTION = "transformer_creation_exception";

    /**
     * @msg.message msg="Exception while creating the XSL."
     */
    String TRANSFORMER_CONFIGURATION_EXCEPTION = "transformer_configuration_exception";

    /**
     * @msg.message msg="Exception while creating the XSL."
     */
    String SAX_EXCEPTION = "sax_exception";
}
