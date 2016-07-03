/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat;

import fiorano.tifosi.dmi.application.OutputPortInstance;

import javax.jms.DeliveryMode;

/**
 * Sets the output port properties
 */
public class OutputPortHandler {

    private long timeToLive;
    private int priority;
    private String name;
    private boolean isPersistent;

    public void setPortProperties(OutputPortInstance outputPortInstance) {
        setTTL(outputPortInstance.getTimeToLive());
        setPriority(outputPortInstance.getPriority());
        setName(outputPortInstance.getName());
        setPersistent(outputPortInstance.isPersistent());
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    private void setPriority(int priority) {
        this.priority = priority;
    }

    public long getTTL() {
        return timeToLive;
    }

    private void setTTL(long timeToLive) {
       this.timeToLive = timeToLive;
    }
         public int getDelMode() {
        return this.isPersistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
    }

    private void setPersistent(boolean persistent) {
        this.isPersistent = persistent;
    }
}
