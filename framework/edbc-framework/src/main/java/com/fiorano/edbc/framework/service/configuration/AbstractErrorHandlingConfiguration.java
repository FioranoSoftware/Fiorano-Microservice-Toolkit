/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingActionFactory;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.ErrorListener;
import com.fiorano.util.StringUtil;
import fiorano.esb.common.ESBException;
import fiorano.esb.utils.BeanUtils;

import java.util.*;

/**
 * <code>AbstractErrorHandlingConfiguration</code> contains configuration details for
 * actions to be taken when an error / exception occurs during the runtime of
 * service. Different actions that can be taken for a specific error id are defined in this class.
 * Classes extending <code>AbstractErrorHandlingConfiguration</code> should override
 * {@link #loadErrorActions()}. Sub classes should provide implementation for {@link #loadErrorActions()}
 * to define mappings for different <code>ServiceErrorID</code>s.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @fiorano.xmbean
 */
public abstract class AbstractErrorHandlingConfiguration implements IServiceConfigurationDetail {
    private Map<ServiceErrorID, Set<ErrorHandlingAction>> errorActionsMap = new Hashtable<>();

    /**
     * Creates mapping of error id with actions that may be taken when the error occurs.
     * This is acheived by calling {@link #loadErrorActions()}.
     */
    public AbstractErrorHandlingConfiguration() {
        loadErrorActions();
    }

    /**
     * Returns a <code>java.util.Map</code> containing <code>ServiceErrorID</code> and corresponding <code>java.util.Collection</code>
     * of <code>ErrorHandlingAction</code>s that may be taken when an exception occurs
     *
     * @return a <code>java.util.Map</code> &lt;<code>ServiceErrorID</code>, <code>java.util.Collection</code>&gt;
     */
    public Map getErrorActionsMap() {
        return errorActionsMap;
    }

    /**
     * Returns a <code>java.util.Map</code> containing <code>ServiceErrorID</code> and corresponding <code>java.util.Collection</code>
     * of <code>ErrorHandlingAction</code>s that may be taken when an exception occurs
     *
     * @param errorActionsMap a <code>java.util.Map</code> &lt;<code>ServiceErrorID</code>, <code>java.util.Collection</code>&gt;
     */
    public void setErrorActionsMap(Map<ServiceErrorID, Set<ErrorHandlingAction>> errorActionsMap) {
        if (errorActionsMap == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_ERROR_ACTION_MAP));
        }
        this.errorActionsMap = errorActionsMap;
    }

    /**
     * Returns <code>java.util.Collection</code> of <code>ErrorHandlingAction</code>s that may be taken for given <code>ServiceErrorID</code>
     *
     * @param errorID <code>ServiceErrorID</code> for which, corresponding <code>ErrorHandlingAction</code>s have to be returned
     * @return <code>java.util.Collection</code> of <code>ErrorHandlingAction</code>s
     */
    public Collection getActions(ServiceErrorID errorID) {
        Set actions = null;
        if (errorID != null) {
            actions = (Set) errorActionsMap.get(errorID);
        }
        return actions == null ? Collections.EMPTY_SET : actions;
    }

    /**
     * Returns all <code>ServiceErrorID</code>s that are present in configuration.
     *
     * @return <code>java.util.Collection</code> of <code>ServiceErrorID</code>s
     */
    public Collection getErrors() {
        return errorActionsMap.keySet();
    }

    /**
     * Removes all mappings stored in this configuration
     */
    public void reset() {
        errorActionsMap.clear();
    }

    /**
     * Returns actions that may be taken when {@link com.fiorano.edbc.framework.service.exception.ServiceErrorID#INVALID_CONFIGURATION_ERROR} occurs
     *
     * @return <code>ErrorHandlingAction</code>s for Invalid Request
     */
    protected Set<ErrorHandlingAction> getActionsForInvalidRequest() {
        Set<ErrorHandlingAction> actions = new LinkedHashSet<>();
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.LOG));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.PROCESS_INVALID_REQUEST));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.STOP_SERVICE));
        return actions;
    }

    /**
     * Returns actions that may be taken when {@link ServiceErrorID#REQUEST_EXECUTION_ERROR} occurs
     *
     * @return <code>ErrorHandlingAction</code>s for errors during request execution
     */
    protected Set<ErrorHandlingAction> getActionsForRequestExecutionError() {
        Set<ErrorHandlingAction> actions = new LinkedHashSet<>();
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.LOG));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.RETRY_EXECUTION));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.STOP_SERVICE));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.THROW_ERROR_ON_WARNING));
        return actions;
    }

    /**
     * Returns actions that may be taken when {@link ServiceErrorID#RESPONSE_GENERATION_ERROR} occurs
     *
     * @return <code>ErrorHandlingAction</code>s for errors during response generation
     */
    protected Set<ErrorHandlingAction> getActionsForResponseGeneration() {
        Set<ErrorHandlingAction> actions = new LinkedHashSet<>();
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.LOG));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT));
        return actions;
    }

    /**
     * Returns actions that may be taken when {@link ServiceErrorID#TRANSPORT_ERROR} occurs
     *
     * @return <code>ErrorHandlingAction</code>s caused because of transport(JMS)
     */
    protected Set<ErrorHandlingAction> getActionsForTransportError() {
        Set<ErrorHandlingAction> actions = new LinkedHashSet<>();
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.LOG));
        actions.add(ErrorHandlingActionFactory.createErrorHandlingAction(ErrorHandlingAction.STOP_SERVICE));
        return actions;
    }

    /**
     * Creates a mapping of an <code>error (ServiceErrorID</code>) and corresponding <code>actionsList (ErrorHandlingAction</code>)
     * that may be taken when an <code>error</code> occurs
     *
     * @param error       <code>ServiceErrorID</code>
     * @param actionsList <code>ErrorHandlingAction</code>s
     */
    protected void addError(ServiceErrorID error, Set<ErrorHandlingAction> actionsList) {
        if (error == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_ERROR_ADDED));
        }
        if (actionsList == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_ERROR_ACTION_LIST_ADDED));
        }
        errorActionsMap.put(error, actionsList);
    }

    /**
     * Creates mappings of <code>ServiceErrorID</code> vs. <code>ErrorHandlingAction</code>s.
     */
    protected abstract void loadErrorActions();

    public void validate(ErrorListener Listener) throws ServiceConfigurationException {
    }

    public String getAsFormattedString() {
        if (errorActionsMap == null || errorActionsMap.isEmpty()) {
            return "";
        }
        Iterator<Map.Entry<ServiceErrorID, Set<ErrorHandlingAction>>> errorActionMapping = errorActionsMap.entrySet().iterator();
        StringBuilder buffer = new StringBuilder();
        while (errorActionMapping.hasNext()) {
            Map.Entry<ServiceErrorID, Set<ErrorHandlingAction>> entry = errorActionMapping.next();
            buffer.append("Error: ").append((entry.getKey()).getName()).append(StringUtil.LINE_SEP);
            Collection<ErrorHandlingAction> actions = entry.getValue();
            for (ErrorHandlingAction action : actions) {
                buffer.append(action.getName()).append(StringUtil.LINE_SEP);
            }
            buffer.append(StringUtil.LINE_SEP);
        }
        return buffer.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            String xml = BeanUtils.serialiseBean(this);
            return BeanUtils.deserialiseBean(xml);
        } catch (ESBException e) {
            Throwable linkedException = e.getLinkedException();
            if (linkedException == null) {
                linkedException = e;
            }
            throw new CloneNotSupportedException(linkedException.getMessage());
        }
    }

}
