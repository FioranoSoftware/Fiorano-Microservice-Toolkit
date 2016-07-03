/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.microservice.common.ccp;

import com.fiorano.esb.common.service.ServiceState;
import com.fiorano.openesb.microservice.ccp.event.component.StatusEvent;
import com.fiorano.services.common.util.RBUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens to state changes of the service and update StatusEvent. The StatusEvent held by this object can be
 * used by implementations of {@link ICCPEventHandler} to send StatusEvent
 * <p/>
 * Date: Mar 1, 2010
 * Time: 3:39:52 PM
 */
public class ServiceStateListener implements PropertyChangeListener {
    private ICCPEventGenerator ccpEventGenerator;
    private StatusEvent serviceStatus = new StatusEvent();
    private Logger logger;

    public ServiceStateListener(ICCPEventGenerator ccpEventGenerator, Logger logger) {
        this.ccpEventGenerator = ccpEventGenerator;
        this.logger = logger;
    }

    /**
     * Return the most recent status of the service. It could be possible one of the updates to service state is missed.
     *
     * @return
     */
    public StatusEvent getServiceStatus() {
        return serviceStatus;
    }

    /**
     * This method gets called when state of service or state of transport or business layers is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */

    public void propertyChange(PropertyChangeEvent evt) {
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.COMPONENT_STATE_CHANGED, new Object[]{evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()}));
        StatusEvent serviceStatus = new StatusEvent();
        serviceStatus.setDescription(evt.getSource().toString());
        serviceStatus.setOperationScope(this.serviceStatus.getOperationScope());
        serviceStatus.setStatusType(this.serviceStatus.getStatusType());
        serviceStatus.setStatus(this.serviceStatus.getStatus());

        //Status type
        if (evt.getNewValue() == ServiceState.State.UNDEFINED) {
            serviceStatus.setStatusType(StatusEvent.StatusType.ERROR);
        } else {
            serviceStatus.setStatusType(StatusEvent.StatusType.INFORMATION);
        }

        //Scope
        if (ServiceState.PROP_STATE.equals(evt.getPropertyName())) {
            if (evt.getNewValue() == ServiceState.State.STARTING || evt.getNewValue() == ServiceState.State.STARTED) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
            } else if (evt.getNewValue() == ServiceState.State.STOPPING || evt.getNewValue() == ServiceState.State.STOPPED) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_STOP);
            } else {
                if (evt.getOldValue() == ServiceState.State.STARTING) {
                    serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
                } else if (evt.getOldValue() == ServiceState.State.STOPPING) {
                    serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_STOP);
                } else {
                    serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_RUNNING);
                }
            }
        } else if (serviceStatus.getOperationScope() == null) {
            ServiceState state = (ServiceState) evt.getSource();
            if (state.getState() == ServiceState.State.STARTED) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_RUNNING);
            } else if (state.getState() == ServiceState.State.STOPPING) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_STOP);
            } else {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
            }
        }

        //Status
        boolean shouldSend = false;
        if (ServiceState.PROP_TRANSPORT_LAYER_STATE.equals(evt.getPropertyName())) {
            if (evt.getNewValue() == ServiceState.State.STARTED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_CONNECTED);
                shouldSend = true;
            } else if (evt.getNewValue() == ServiceState.State.STOPPING) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_DISCONNECTING);
                shouldSend = true;
            } else if (evt.getNewValue() == ServiceState.State.UNDEFINED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_UNKNOWN);
                shouldSend = true;
            }
        } else if (ServiceState.PROP_STATE.equals(evt.getPropertyName())) {
            if (evt.getNewValue() == ServiceState.State.STARTED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_STARTED);
                shouldSend = true;
            } else if (evt.getNewValue() == ServiceState.State.STOPPED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_STOPPED);
                shouldSend = true;
            } else if (evt.getNewValue() == ServiceState.State.UNDEFINED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_UNKNOWN);
                shouldSend = true;
            } else if (evt.getNewValue() == ServiceState.State.STARTING) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_LAUNCHING);
                shouldSend = true;
            }
        }

        if (shouldSend) {
            ccpEventGenerator.sendEvent(serviceStatus);
        }

        if (evt.getNewValue() == ServiceState.State.STARTED) {
            serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_RUNNING);
        }
        this.serviceStatus = serviceStatus;
    }
}
