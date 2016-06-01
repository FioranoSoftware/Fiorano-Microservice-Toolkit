/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.internal.peer.PeerCommunicationsManager;
import com.fiorano.esb.common.monitor.ExecutionTimeDetailsEventPublisher;
import com.fiorano.services.common.monitor.ExecutionTimeMonitor;
import com.fiorano.services.common.monitor.ExecutionTimeMonitorNotificationTask;
import com.fiorano.services.common.monitor.configuration.MonitoringConfiguration;
import com.fiorano.services.common.service.ServiceDetails;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 30-Jan-2011
 * Time: 19:03:53
 * To change this template use File | Settings | File Templates.
 */
public class MonitorFactory {
    private Timer timer;
    private PeerCommunicationsManager peerCommunicationsManager;
    private ServiceDetails serviceDetails;
    private MonitoringConfiguration configuration;

    public MonitorFactory(PeerCommunicationsManager peerCommunicationsManager, MonitoringConfiguration configuration, ServiceDetails serviceDetails) {
        this.peerCommunicationsManager = peerCommunicationsManager;
        this.configuration = configuration;
        this.serviceDetails = serviceDetails;
    }

//    @todo
    public ExecutionTimeMonitor createMonitor(Logger logger) {
        return null;
    }

    public synchronized void stopAllMonitors() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
