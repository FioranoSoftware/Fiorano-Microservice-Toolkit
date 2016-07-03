/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

/**
 * Date: Mar 18, 2007
 * Time: 11:35:43 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface RetryConfigurationConstants {
    /**
     * default value for enabled
     */
    public boolean ENABLED = true;
    /**
     * default value for retry count
     */
    public int RETRY_COUNT = -1;
    /**
     * default value for retry interval
     */
    public long RETRY_INTERVAL = 30000;
}
