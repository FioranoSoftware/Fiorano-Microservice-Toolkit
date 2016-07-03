/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

import javax.jms.TextMessage;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Date: Aug 25, 2007
 * Time: 11:29:10 PM
 *
 * @author Venkat
 * @version 1.1, 3 October 2008
 */
public interface ICBRProcessor {
    List<Evaluator> evaluate(TextMessage message) throws ServiceExecutionException;

    void addEvaluator(Evaluator evaluator) throws ServiceExecutionException;

    void initialize() throws ServiceExecutionException;
}
