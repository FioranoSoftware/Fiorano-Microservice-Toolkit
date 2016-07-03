/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

import com.fiorano.services.common.util.RBUtil;

/**
 * <code>ErrorHandlingAction</code> holds the configuration of action that may be taken when an exception
 * occurs during execution of service.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @see com.fiorano.edbc.framework.service.exception.RetryAction
 * @see com.fiorano.edbc.framework.service.exception.ServiceErrorID
 * @see ErrorHandlingActionFactory
 */
public class ErrorHandlingAction {
    /**
     * <code>0</code> - id for action to log exception
     */
    public static final int LOG = 0;
    /**
     * <code>1</code> - id for action to send error message on <code>ON_EXCEPTION<code> port
     */
    public static final int SEND_TO_ERROR_PORT = 1;
    /**
     * <code>2</code> - id for action to retry execution with same request
     */
    public static final int RETRY_EXECUTION = 2;
    /**
     * <code>3</code> - id for action to retry execution after reconnecting to EIS
     */
    public static final int RECONNECT = 3;
    /**
     * <code>1</code> - id for action to treat warnings as exceptions
     */
    public static final int THROW_ERROR_ON_WARNING = 4;
    /**
     * <code>1</code> - id for action to stop service
     */
    public static final int STOP_SERVICE = 5;
    /**
     * <code>1</code> - id for action to continue processing invalid request
     */
    public static final int PROCESS_INVALID_REQUEST = 6;

    public static final int DISCARD_CONNECTION = 7;

    private final static int NAME_DETAIL_INDEX = 0;
    private final static int DESC_DETAIL_INDEX = 1;
    private final static int ACTION_COUNT = 8;
    private final static int DETAIL_COUNT = 2;

    private static String[][] ACTION_DETAILS = new String[ACTION_COUNT][DETAIL_COUNT];

    static {
        ACTION_DETAILS[LOG][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.LOG_TO_ERROR_LOGS_NAME);
        ACTION_DETAILS[LOG][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.LOG_TO_ERROR_LOGS_DESC);
        ACTION_DETAILS[SEND_TO_ERROR_PORT][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.SEND_TO_ERROR_PORT_NAME);
        ACTION_DETAILS[SEND_TO_ERROR_PORT][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.SEND_TO_ERROR_PORT_DESC);
        ACTION_DETAILS[RETRY_EXECUTION][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.RE_EXECUTE_REQUEST_NAME);
        ACTION_DETAILS[RETRY_EXECUTION][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.RE_EXECUTE_REQUEST_DESC);
        ACTION_DETAILS[RECONNECT][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.TRY_RECONNECTION_NAME);
        ACTION_DETAILS[RECONNECT][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.TRY_RECONNECTION_DESC);
        ACTION_DETAILS[THROW_ERROR_ON_WARNING][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.THROW_FAULT_ON_WARNINGS_NAME);
        ACTION_DETAILS[THROW_ERROR_ON_WARNING][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.THROW_FAULT_ON_WARNINGS_DESC);
        ACTION_DETAILS[STOP_SERVICE][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.STOP_SERVICE_NAME);
        ACTION_DETAILS[STOP_SERVICE][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.STOP_SERVICE_DESC);
        ACTION_DETAILS[PROCESS_INVALID_REQUEST][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.PROCESS_INVALID_REQUEST_NAME);
        ACTION_DETAILS[PROCESS_INVALID_REQUEST][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.PROCESS_INVALID_REQUEST_DESC);
        ACTION_DETAILS[DISCARD_CONNECTION][NAME_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.DISCARD_CONNECTION_NAME);
        ACTION_DETAILS[DISCARD_CONNECTION][DESC_DETAIL_INDEX] = RBUtil.getMessage(Bundle.class, Bundle.DISCARD_CONNECTION_DESC);
    }

    private int id;
    private boolean enabled;

    /**
     * Creates <code>ErrorHandlingAction</code> with no action defined. This constructor is present only
     * for serialization and should not be ideally used. Use {@link #ErrorHandlingAction(int)} instead.
     */
    public ErrorHandlingAction() {
    }

    /**
     * Creates <code>ErrorHandlingAction</code> with given <code>id</code>
     *
     * @param id value from {@link #LOG}, {@link #SEND_TO_ERROR_PORT}, {@link #RETRY_EXECUTION}, {@link #RECONNECT},
     *           {@link #THROW_ERROR_ON_WARNING}, {@link #STOP_SERVICE}, {@link #PROCESS_INVALID_REQUEST}
     */
    public ErrorHandlingAction(int id) {
        setId(id);
    }

    /**
     * Returns name of action specified by <code>id</code>
     *
     * @param id action whose name should be returned
     * @return name of action given by <code>id</code>
     */
    public static String getName(int id) {
        return ACTION_DETAILS[id][NAME_DETAIL_INDEX];
    }

    /**
     * Returns description of action specified by <code>id</code>
     *
     * @param id action whose description should be returned
     * @return description of action given by <code>id</code>
     */
    public static String getDescription(int id) {
        return ACTION_DETAILS[id][DESC_DETAIL_INDEX];
    }

    /**
     * Returns id representing the action, whose configuration this object holds
     *
     * @return id, value from {@link #LOG}, {@link #SEND_TO_ERROR_PORT}, {@link #RETRY_EXECUTION}, {@link #RECONNECT},
     * {@link #THROW_ERROR_ON_WARNING}, {@link #STOP_SERVICE}, {@link #PROCESS_INVALID_REQUEST}
     */
    public int getId() {
        return id;
    }

    /**
     * Set id representing the action, whose configuration this object holds
     *
     * @param id value from {@link #LOG}, {@link #SEND_TO_ERROR_PORT}, {@link #RETRY_EXECUTION}, {@link #RECONNECT},
     *           {@link #THROW_ERROR_ON_WARNING}, {@link #STOP_SERVICE}, {@link #PROCESS_INVALID_REQUEST}
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns whether this action is enabled or not. If an action is not enabled, it
     * will not be performed.
     *
     * @return <code>true</code> if action is enabled, <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this action has to be enabled or not. If an action is not enabled, it
     * will not be performed.
     *
     * @param enabled <code>true</code> if action is enabled, <code>false</code> otherwise
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns name of this action
     *
     * @return name
     */
    public String getName() {
        return ACTION_DETAILS[id][NAME_DETAIL_INDEX];
    }

    /**
     * Returns description of this action
     *
     * @return description
     */
    public String getDescription() {
        return ACTION_DETAILS[id][DESC_DETAIL_INDEX];
    }

    /**
     * Indicates if <code>that</code> is equal to this action. Two <code>ErrorHandlingAction</code> objects
     * are considered if the values returned by {@link #getId()} are equal
     *
     * @param that
     * @return <code>true</code> if <code>that</code> is equal to this object; <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (!(that instanceof ErrorHandlingAction)) {
            return false;
        }
        return this.getId() == ((ErrorHandlingAction) that).getId();
    }

    /**
     * Returns <code>id</code> as hashcode
     *
     * @return id as hashcode
     */
    public int hashCode() {
        return getId();
    }

}
