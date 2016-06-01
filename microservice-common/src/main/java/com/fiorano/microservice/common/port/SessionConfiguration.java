/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.microservice.common.port;

import com.fiorano.openesb.application.application.InputPortInstance;

/**
 * Configuration details for creating <code>javax.jms.Session</code>
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public final class SessionConfiguration {

    /**
     * <code>1</code> - Indicates acknowledge mode for <code>javax.jms.Session</code>
     * is {@link javax.jms.Session#AUTO_ACKNOWLEDGE}
     */
    public static final int AUTO_ACKNOWLEDGE = InputPortInstance.ACKNOWLEDGEMENT_MODE_AUTO;
    /**
     * <code>2</code> - Indicates acknowledge mode for <code>javax.jms.Session</code>
     * is {@link javax.jms.Session#CLIENT_ACKNOWLEDGE}
     */
    public static final int CLIENT_ACKNOWLEDGE = InputPortInstance.ACKNOWLEDGEMENT_MODE_CLIENT;
    /**
     * <code>3</code> - Indicates acknowledge mode for <code>javax.jms.Session</code>
     * is {@link javax.jms.Session#DUPS_OK_ACKNOWLEDGE}
     */
    public static final int DUPS_OK_ACKNOWLEDGE = InputPortInstance.ACKNOWLEDGEMENT_MODE_DUPS_OK;

    private boolean transacted;
    private int acknowledgementMode = DUPS_OK_ACKNOWLEDGE;
    private int count = 1;
    private int transactionSize = 0;

    /**
     * Creates <code>SessionConfiguration</code> object with given details
     *
     * @param transacted <code>true</code> if session to be created should be transacted,
     * <code>false</code> otherwise
     * @param acknowledgementMode acknowledgement mode, one of {@link #AUTO_ACKNOWLEDGE}, {@link #AUTO_ACKNOWLEDGE} or {@link #DUPS_OK_ACKNOWLEDGE}
     * @param count number of sessions to be created
     * @param transactionSize size of a transaction for this session
     */
    SessionConfiguration(boolean transacted, int acknowledgementMode, int count, int transactionSize) {
        this.transacted = transacted;
        this.acknowledgementMode = acknowledgementMode;
        this.count = count;
        this.transactionSize = transactionSize;
    }

    /**
     * Returns whether <code>javax.jms.Session</code> to be created should be transacted or not.
     *
     * @return <code>true</code> if session to be created should be transacted,
     *         <code>false</code> otherwise
     */
    public boolean isTransacted() {
        return transacted;
    }

    /**
     * Sets whether <code>javax.jms.Session</code> to be created should be transacted or not.
     *
     * @param transacted <code>true</code> if session to be created should be transacted,
     * <code>false</code> otherwise
     */
    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    /**
     * Returns acknowledgement mode for <code>javax.jms.Session</code> to be created.
     *
     * @return {@link #AUTO_ACKNOWLEDGE}, {@link #CLIENT_ACKNOWLEDGE}, {@link #DUPS_OK_ACKNOWLEDGE}
     */
    public int getAcknowledgementMode() {
        return acknowledgementMode;
    }

    /**
     * Sets acknowledgement mode for <code>javax.jms.Session</code> to be created.
     *
     * @param acknowledgementMode possible value {@link #AUTO_ACKNOWLEDGE}, {@link #CLIENT_ACKNOWLEDGE}, {@link #DUPS_OK_ACKNOWLEDGE}
     */
    public void setAcknowledgementMode(int acknowledgementMode) {
        this.acknowledgementMode = acknowledgementMode;
    }

    /**
     * Returns number of sessions to be created.
     *
     * @return number of sessions
     */
    public int getCount() {
        return count;
    }

    /**
     * Set number of sessions to be created
     *
     * @param count number of sessions
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Returns the transaction size. Transaction size is the number of messages processed after which
     * {@link javax.jms.Session#commit()} should be called
     *
     * @return size of a JMS transaction
     */
    public int getTransactionSize() {
        return transactionSize;
    }

    /**
     * Sets the transaction size. Transaction size is the number of messages processed after which
     * {@link javax.jms.Session#commit()} should be called
     *
     * @param transactionSize size of a JMS transaction
     */
    public void setTransactionSize(int transactionSize) {
        this.transactionSize = transactionSize;
    }
}
