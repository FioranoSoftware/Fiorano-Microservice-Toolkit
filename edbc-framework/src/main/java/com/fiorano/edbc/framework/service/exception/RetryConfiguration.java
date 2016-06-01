/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

import com.fiorano.services.common.util.RBUtil;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>RetryConfiguration</code> holds configuration details of retries and other actions
 * that can be perfomed during retires for <code>RetryAction</code>
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class RetryConfiguration implements RetryConfigurationConstants {

    /**
     * Number of times to retry doing an action
     */
    private int retryCount = RETRY_COUNT;

    /**
     * Interval after which retry should be attempted
     */
    private long retryInterval = RETRY_INTERVAL;

    private Map otherActions = new Hashtable();

    /**
     * Returns number of retries.
     *
     * @return number of retries
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets number of retries
     *
     * @param retryCount number of retires
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * Returns interval (time in milliseconds) after which a retry has to be attempted
     *
     * @return time, in milliseconds, before next retry is attempted
     */
    public long getRetryInterval() {
        return retryInterval;
    }

    /**
     * Sets interval (time in milliseconds) after which a retry has to be attempted
     *
     * @param retryInterval time, in milliseconds, before next retry is attempted
     */
    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    /**
     * Returns a <code>java.util.Map</code> of <code>ErrorHandlingAction</code> versus number of retries after which
     * corresponding <code>ErrorHandlingAction</code> has to be perfomed. Typically other actions will be actions which
     * indicate progress. Eg, When repeating a reconnect action, an other action which logs after every three retries
     * to reconnect can be added.
     *
     * @return Map of other ErrorHandlingActions to be taken while retrying this action
     */
    public Map getOtherActions() {
        return otherActions;
    }

    /**
     * Sets a <code>otherActions</code>, <code>java.util.Map</code> of <code>ErrorHandlingAction</code> versus number
     * of retries after which corresponding <code>ErrorHandlingAction</code> has to be perfomed. If <code>otherActions</code>
     * contain keys which does not satisfy instanceof ErrorHandlingAction it is ignored.
     *
     * @param otherActions Map of other ErrorHandlingActions to be taken while retrying this action
     * @throws IllegalArgumentException if key contains a <code>null</code> value or a <code>RetryAction</code>
     */
    public void setOtherActions(Map otherActions) {
        this.otherActions.clear();
        if (otherActions != null) {
            Iterator entriesIterator = otherActions.entrySet().iterator();
            while (entriesIterator.hasNext()) {
                Map.Entry entry = (Map.Entry) entriesIterator.next();
                if (entry.getKey() instanceof ErrorHandlingAction && entry.getValue() instanceof Integer) {
                    addOtherAction((ErrorHandlingAction) entry.getKey(), (Integer) entry.getValue());
                }
            }
        }
    }

    /**
     * Adds <code>action</code> which has to be performed after every <code>retryCountBeforeAction</code> times
     * <code>this</code> action is executed
     *
     * @param action                 action to be executed after specified number of attempts of <code>this</code> action
     * @param retryCountBeforeAction number of times <code>this</code> action has to be performed before
     *                               performing <code>action</code>
     */
    public void addOtherAction(ErrorHandlingAction action, Integer retryCountBeforeAction) {
        if (action == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_ACTION, new Object[]{action}));
        }
        if (action instanceof RetryAction) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.RECURSIVE_RETRY));
        }
        if (retryCountBeforeAction == null) {
            retryCountBeforeAction = new Integer(1);
        }
        if (!isValidRetryCountBeforeAction(retryCountBeforeAction)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_RETRY_COUNT,
                    new Object[]{action.getName(), retryCountBeforeAction, "" + retryCount}));
        }
        otherActions.put(action, retryCountBeforeAction);
    }

    private boolean isValidRetryCountBeforeAction(Integer retryCountBeforeAction) {
        return retryCount == -1 || retryCountBeforeAction.intValue() <= retryCount;
    }

    /**
     * validates this object
     *
     * @throws Exception if configuration is invalid
     */
    public void validate() throws Exception {
        if (retryCount != -1 && retryCount < 0) {
            throw new Exception(RBUtil.getMessage(Bundle.class, Bundle.INVALID_RETRY_COUNT, new Object[]{new Integer(retryCount)}));
        }
        if (retryInterval <= 0) {
            throw new Exception(RBUtil.getMessage(Bundle.class, Bundle.INVALID_RETRY_INTERVAL, new Object[]{new Long(retryInterval)}));
        }
        Iterator otherActionsIterator = otherActions.entrySet().iterator();
        while (otherActionsIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) otherActionsIterator.next();
            ErrorHandlingAction action = (ErrorHandlingAction) entry.getKey();
            Integer retriesBeforeAction = (Integer) entry.getValue();
            if (!isValidRetryCountBeforeAction(retriesBeforeAction)) {
                throw new Exception(RBUtil.getMessage(Bundle.class, Bundle.INVALID_RETRY_COUNT,
                        new Object[]{action.getName(), retriesBeforeAction, "" + retryCount}));
            }
        }
    }

    /**
     * clones this object
     *
     * @return cloned object
     */
    public Object clone() {
        RetryConfiguration that = new RetryConfiguration();
        that.setRetryCount(this.retryCount);
        that.setRetryInterval(this.retryInterval);
        that.setOtherActions(this.getOtherActions());
        return that;
    }

    /**
     * Resets the values of this object to default values.
     */
    public void reset() {
        setRetryCount(RETRY_COUNT);
        setRetryInterval(RETRY_INTERVAL);
        if (getOtherActions() != null) {
            getOtherActions().clear();
        } else {
            otherActions = new Hashtable();
        }
    }
}
