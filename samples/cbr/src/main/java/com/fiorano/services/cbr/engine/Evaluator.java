/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

import fiorano.jms.cbr.IConsumer;

/**
 * Created by IntelliJ IDEA.
 * Date: Aug 26, 2007
 * Time: 2:08:03 AM
 *
 * @author Venkat
 * @version 1.0, 26 August 2007
 */
public class Evaluator implements IConsumer {
    private CBRConfiguration configuration;

    public Evaluator(CBRConfiguration configuration) {
        this.configuration = configuration;
    }

    public CBRConfiguration getConfiguration() {
        return configuration;
    }

    public void setParameters(CBRConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getFilter() {
        return configuration == null ? null : configuration.getCondition();
    }
}
