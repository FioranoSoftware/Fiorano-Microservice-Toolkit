/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.engine;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 27-Jan-2011
 * Time: 11:55:22
 * To change this template use File | Settings | File Templates.
 */
public interface IRequestValidator<R> {
    void validate(R request) throws ServiceExecutionException;
}
