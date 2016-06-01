/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.peer;

import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.IService;
import com.fiorano.edbc.framework.service.internal.ServiceUtil;
import com.fiorano.edbc.framework.service.internal.StateListener;
import com.fiorano.esb.common.ccp.Bundle;
import com.fiorano.microservice.common.ccp.ICCPEventGenerator;
import com.fiorano.openesb.microservice.ccp.event.component.StatusEvent;
import com.fiorano.services.common.util.RBUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens to state changes of the service and update StatusEvent. The StatusEvent held by this object can be
 * used by implementations of {@link com.fiorano.esb.common.ccp.ICCPEventHandler} to send StatusEvent
 * <p/>
 * Date: Mar 1, 2010
 * Time: 3:39:52 PM
 */
public class ServiceStateListener extends StateListener {
    private ICCPEventGenerator ccpEventGenerator;
    private StatusEvent serviceStatus = new StatusEvent();
    private IService service;
    private Logger logger;

    public ServiceStateListener(IService service, ICCPEventGenerator ccpEventGenerator, Logger logger) {
        this.service = service;
        this.ccpEventGenerator = ccpEventGenerator;
        this.logger = logger;
    }

    /**
     * This method gets called when state of service or state of transport or business layers is changed.
     *
     * @param evt A IModule.StateChangeEvent object describing the event source
     *            and the property that has changed.
     */
    protected void stateChanged(IModule.StateChangeEvent evt) {
        logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.COMPONENT_STATE_CHANGED, new Object[]{ServiceUtil.getServiceState(service), evt.getSource(), evt.getOldState(), evt.getNewState()}));
        StatusEvent serviceStatus = new StatusEvent();
        serviceStatus.setDescription(evt.getSource().toString());
        serviceStatus.setOperationScope(this.serviceStatus.getOperationScope());
        serviceStatus.setStatusType(this.serviceStatus.getStatusType());
        serviceStatus.setStatus(this.serviceStatus.getStatus());

        //Status type
        if (evt.getNewState() == IModule.State.UNDEFINED) {
            serviceStatus.setStatusType(StatusEvent.StatusType.ERROR);
        } else {
            serviceStatus.setStatusType(StatusEvent.StatusType.INFORMATION);
        }

        //Scope
        if (evt.getSource().equals(service)) {
            if (evt.getNewState() == IModule.State.CREATING || evt.getNewState() == IModule.State.STARTED) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
            } else if (evt.getNewState() == IModule.State.STOPPING || evt.getNewState() == IModule.State.STOPPED) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_STOP);
            } else {
                if (evt.getOldState() == IModule.State.CREATING) {
                    serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
                } else if (evt.getOldState() == IModule.State.STOPPING) {
                    serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_STOP);
                } else {
                    serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_RUNNING);
                }
            }
        } else if (serviceStatus.getOperationScope() == null) {
            if (service.getState() == IModule.State.STARTED) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_RUNNING);
            } else if (service.getState() == IModule.State.STOPPING) {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_STOP);
            } else {
                serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
            }
        }

        //Status
        boolean shouldSend = false;
        if (evt.getSource().equals(service.getTransportManager())) {
            if (evt.getNewState() == IModule.State.STARTED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_CONNECTED);
                shouldSend = true;
            } else if (evt.getNewState() == IModule.State.STOPPING) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_DISCONNECTING);
                shouldSend = true;
            } else if (evt.getNewState() == IModule.State.UNDEFINED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_UNKNOWN);
                shouldSend = true;
            }
        } else if (evt.getSource().equals(service)) {
            if (evt.getNewState() == IModule.State.STARTING) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_LAUNCHING);
                shouldSend = true;
            } else if (evt.getNewState() == IModule.State.STARTED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_STARTED);
                shouldSend = true;
            } else if (evt.getNewState() == IModule.State.DESTROYED || evt.getNewState() == IModule.State.STOPPED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_STOPPED);
                shouldSend = true;
            } else if (evt.getNewState() == IModule.State.UNDEFINED) {
                serviceStatus.setStatus(StatusEvent.Status.COMPONENT_UNKNOWN);
                shouldSend = true;
            }
        }

        if (shouldSend) {
            ccpEventGenerator.sendEvent(serviceStatus);
        }

        if (evt.getNewState() == IModule.State.STARTED) {
            serviceStatus.setOperationScope(StatusEvent.OperationScope.COMPONENT_RUNNING);
        }
        this.serviceStatus = serviceStatus;
    }
}