/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import fiorano.esb.util.InMemoryLaunchable;

/**
 * Date: Mar 9, 2007
 * Time: 2:22:01 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractInmemoryService extends AbstractService implements InMemoryLaunchable {
    private final Object lock = new Object();
    private int exitValue = 0;

    /**
     * This method is called by the Peer server when it wants to launch the component.
     * Start method should start the component.
     *
     * @param args - Arguments needs for the component. Ex- applicationGUID, serviceinstance, FPS connect url, Backup urls etc ...
     */
    public void startup(String[] args) {
        try {
            start(args);
        } catch (ServiceExecutionException e) {
            shutdown(e);
        }
    }

    /**
     * Causes the current thread to wait, if necessary, until the service has
     * terminated. This method returns immediately if the service has already terminated. If the service has not yet
     * terminated, the calling thread will be blocked until the service terminates.
     *
     * @return the exit value of the service. By convention, <code>0</code> indicates normal termination.
     * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another thread
     *                              while it is waiting, then the wait is ended and an {@link InterruptedException} is thrown.
     */
    public int waitFor() throws InterruptedException {
        if (!commandLineParams.isCCPEnabled()) {
            return 0;
        }
        if (started) {
            synchronized (lock) {
                lock.wait();
            }
        }
        return exitValue;
    }

    /**
     * Returns the exit value for the service.
     *
     * @return the exit value of the service. by convention, the value
     * <code>0</code> indicates normal termination.
     * @throws IllegalThreadStateException if the service has not yet terminated.
     */
    public int exitValue() {
        if (!commandLineParams.isCCPEnabled()) {
            return 0;
        }
        if (started) {
            throw new IllegalThreadStateException();
        } else {
            return exitValue;
        }
    }

    /**
     * This methods will be called from the peer when the component is stopped.
     * Shutdown methods should cleanup all resources used by the component and leave no open handles/connections.
     *
     * @param hint - hintable param for shutdown
     */
    public void shutdown(Object hint) {
        stop();
    }

    /**
     * Marks the InMemory launched service as stopped and notifies any thread waiting on {@link #waitFor()}.
     * <code>exitValue</code> is stored and is returned on all subsequent calls to {@link #exitValue()}.
     *
     * @param exitValue
     */
    protected void exit(int exitValue) {
        if (commandLineParams.isInmemoryLaunchable()) {
            if (!commandLineParams.isCCPEnabled()) {
                return;
            }
            this.exitValue = exitValue;
            synchronized (lock) {
                lock.notify();
            }
        } else {
            super.exit(exitValue);
        }
    }

}
