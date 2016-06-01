/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.exception.ServiceException;
import fiorano.esb.util.InMemoryLaunchable;

import java.util.logging.Level;

public abstract class InMemoryServiceLauncher extends ServiceLauncher implements InMemoryLaunchable {
    private final Object lock = new Object();
    private int exitValue = 0;
    private int shutdownCheckAttempts = 0;

    /**
     * This method is called by the Peer server when it wants to launch the component.
     * Start method should start the component.
     *
     * @param args - Arguments needs for the component. Ex- applicationGUID, serviceinstance, FPS connect url, Backup urls etc ...
     */
    public void startup(String[] args) {
        try {
            launch(args);
        } catch (ServiceException e) {
            if (service != null && service.getLogger() != null) {
                service.getLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            shutdown(e);
        }
    }

    /**
     * This methods will be called from the peer when the component is stopped.
     * Shutdown methods should cleanup all resources used by the component and leave no open handles/connections.
     *
     * @param hint - hintable param for shutdown
     */
    public void shutdown(Object hint) {
        terminate();
    }

    /**
     * Causes the current thread to wait, if necessary, until the process represented by this <code>Process</code> object has
     * terminated. This method returns immediately if the subprocess has already terminated. If the subprocess has not yet
     * terminated, the calling thread will be blocked until the subprocess exits.
     *
     * @return the exit value of the process. By convention, <code>0</code> indicates normal termination.
     * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another thread
     *                              while it is waiting, then the wait is ended and an {@link InterruptedException} is thrown.
     */
    public int waitFor() throws InterruptedException {
        if (!service.getLaunchConfiguration().isCCPEnabled()) {
            return 0;
        }
        if (isNotStopped()) {
            synchronized (lock) {
                lock.wait();
            }
        }
        return exitValue;
    }

    /**
     * Returns the exit value for the subprocess.
     *
     * @return the exit value of the subprocess represented by this <code>Process</code> object. by convention, the value
     * <code>0</code> indicates normal termination.
     * @throws IllegalThreadStateException if the subprocess represented by this <code>Process</code> object has not yet terminated.
     */
    public int exitValue() {
        if (!service.getLaunchConfiguration().isCCPEnabled()) {
            return 0;
        }
        if (isNotStopped()) {
            throw new IllegalThreadStateException();
        } else {
            return exitValue;
        }
    }

    private boolean isNotStopped() {
        if (shutdownCheckAttempts++ <= 8)
            return !IModule.State.DESTROYED.equals(service.getTransportProvider().getState());
        else
            return !IModule.State.DESTROYED.equals(service.getTransportProvider().getState()) && !IModule.State.DESTROYING.equals(service.getTransportProvider().getState());

    }


    /**
     * Marks the InMemory launched service as stopped and notifies any thread waiting on {@link #waitFor()}.
     * <code>exitValue</code> is stored and is returned on all subsequent calls to {@link #exitValue()}.
     *
     * @param exitValue
     */
    public void exit(int exitValue) {
        if (service.getLaunchConfiguration().isInmemoryLaunchable()) {
            if (!service.getLaunchConfiguration().isCCPEnabled()) {
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
