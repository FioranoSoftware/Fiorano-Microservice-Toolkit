/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.internal.engine.IEngine;
import com.fiorano.edbc.framework.service.internal.transport.ITransportManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceUtil {
    public static void logServiceState(IModule module) {
        IService service = getService(module);
        if (service != null) {
            logCompleteModuleState("", service.getLogger(), service);
        }
    }

    private static void logCompleteModuleState(String indent, Logger logger, IModule module) {
        logger.log(Level.FINE, indent + module.getName() + " : " + module.getState());
        List<? extends IModule> children = module.getChildren();
        for (IModule child : children) {
            logCompleteModuleState(indent + "\t", logger, child);
        }
    }

    public static String getServiceState(IService service) {
        String serviceState = String.valueOf(service.getState());
        String transportLayerState = "TP- " + service.getTransportProvider().getState() + ",TM- " + (service.getTransportManager() != null ? service.getTransportManager().getState() : IModule.State.DESTROYED);
        String businessLayerState = String.valueOf(service.getEngine() != null ? service.getEngine().getState() : IModule.State.DESTROYED);
        return "Service State: " + serviceState + ";Transport Layer State: " + transportLayerState + ";Business Layer State: " + businessLayerState;
    }

    public static void addStateListener(IService service, StateListener stateListener) {
        service.addStateListener(stateListener);
        service.getTransportProvider().addStateListener(stateListener);
        ITransportManager transportManager = service.getTransportManager();
        if (transportManager != null) {
            transportManager.addStateListener(stateListener);
        }
        IEngine engine = service.getEngine();
        if (engine != null) {
            engine.addStateListener(stateListener);
        }
    }

    public static void removeStateListener(IService service, StateListener stateListener) {
        service.removeStateListener(stateListener);
        service.getTransportProvider().removeStateListener(stateListener);
        service.getTransportManager().removeStateListener(stateListener);
        IEngine engine = service.getEngine();
        if (engine != null) {
            engine.removeStateListener(stateListener);
        }
    }

    private static IService getService(IModule module) {
        IModule parent = module.getParent();
        while (parent != null) {
            module = parent;
            parent = module.getParent();
        }
        if (module instanceof IService) {
            return (IService) module;
        } else {
            return null;
        }
    }

    public static Logger getLogger(IModule module, String loggerName) {
        IService service = getService(module);
        return service != null ? service.getLogger(loggerName) : null;
    }

}
