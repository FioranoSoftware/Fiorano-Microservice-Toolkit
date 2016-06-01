/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

import com.fiorano.services.common.util.RBUtil;

/**
 * <code>RetryAction</code> is an action that repeats a remedial task at regular intervals of time until the
 * action succeeds or repetition count equals configured number of retries. Configuration details of retries
 * are present in {@link RetryConfiguration}
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @see RetryConfiguration
 */
public class RetryAction extends ErrorHandlingAction {
    private RetryConfiguration configuration;

    /**
     * Creates <code>RetryAction</code> with no action defined. This constructor is present only
     * for serialization and should not be ideally used. Use {@link #RetryAction(int)} instead.
     */
    public RetryAction() {
        super();
    }

    /**
     * Creates <code>RetryAction</code> with given <code>id</code>
     *
     * @param id, {@link #RETRY_EXECUTION} or {@link #RECONNECT}
     * @throws IllegalArgumentException if id is not <code>RETRY_EXECUTION</code> or <code>RECONNECT</code>
     */
    public RetryAction(int id) {
        if (!isSupported(id)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_RETRY_ACTION, new Object[]{getName(id)}));
        }
        setId(id);
    }

    /**
     * Returns <code>RetryConfiguration</code> containing configuration details of retries and other actions
     * that have to be executed
     *
     * @return configuration details of retries and other actions to be executed
     */
    public RetryConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets <code>RetryConfiguration</code> containing configuration details of retries and other actions
     * that have to be executed
     *
     * @param configuration configuration details of retries and other actions to be executed
     */
    public void setConfiguration(RetryConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Indicates whether the action given by <code>id</code> can be treated as a <code>RetryAction</code>
     *
     * @param id
     * @return <code>true</code> if <code>id</code> can be repeated at regurlar intervals;
     * <code>false</code> otherwise
     */
    protected boolean isSupported(int id) {
        return id == RECONNECT || id == RETRY_EXECUTION;
    }
}
