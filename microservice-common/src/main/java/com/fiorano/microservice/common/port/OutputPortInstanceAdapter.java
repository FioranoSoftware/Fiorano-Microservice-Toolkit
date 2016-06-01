/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.microservice.common.port;

import com.fiorano.openesb.application.application.OutputPortInstance;

import javax.jms.DeliveryMode;
import java.util.HashMap;

/**
 * <code>OutputPortInstanceAdapter</code> is an adapter (wrapper class) for <code>OutputPortInstance</code>. It holds an
 * <code>OutputPortInstance</code> object and delgates all calls on this object to corresponding method(s) of
 * <code>OutputPortInstance</code> object. Services should use <code>OutputPortInstanceAdapter</code> instead of
 * <code>OutputPortInstance</code> to avoid tight coupling with classes from FioranoESB.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public final class OutputPortInstanceAdapter extends PortInstanceAdapter {

    /**
     * Creates an <code>OutputPortInstanceAdapter</code> which holds <code>outputPortInstance<code>. This is used by classes in library to create
     * an adpater for <code>outputPortInstance<code> which can be passed to service
     *
     * @param outputPortInstance underlying output port instance object
     */
    public OutputPortInstanceAdapter(OutputPortInstance outputPortInstance) {
        super(outputPortInstance);
    }

    /**
     * Returns if the messages going out of this port will be persistent.
     *
     * @return the persistent property set on the output port.
     */
    public int getDeliveryMode() {
        return getPortInstance().isPersistent() ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
    }

    /**
     * Sets the persistent property for the messages going out of this port.
     *
     * @param deliveryMode true if the messages going out of this port have to be persistent.
     */
    void setDeliveryMode(int deliveryMode) {
        getPortInstance().setPersistent(DeliveryMode.PERSISTENT == deliveryMode);
    }

    /**
     * Returns the time to live that will be set on messages going out of this port.
     *
     * @return time after which message exprires
     */
    public long getTimeToLive() {
        return getPortInstance().getTimeToLive();
    }

    /**
     * Sets the time to live that will be set on messages going out of this port.
     *
     * @param timeToLive time after which message expires
     */
    void setTimeToLeave(long timeToLive) {
        getPortInstance().setTimeToLive(timeToLive);
    }

    /**
     * Returns priority that will be set on messages going out of this port
     *
     * @return priority to be set on messages
     */
    public int getPriority() {
        return getPortInstance().getPriority();
    }

    /**
     * Sets priority that will be set on messages going out of this port
     *
     * @param priority priority to be set on messages
     */
    void setPriority(int priority) {
        getPortInstance().setPriority(priority);
    }

    /**
     * Returns if messages on this port have to be compressed.
     *
     * @return <code>true</code> if messages have to be compressed, <code>false</code> otherwise.
     */
    public boolean shouldCompressMessages() {
        return getPortInstance().isCompressMessages();
    }

    /**
     * Set if messages on this port have to be compressed.
     *
     * @param shouldCompressMessages <code>true</code> if messages have to be compressed, <code>false</code> otherwise.
     */
    void setCompressMessages(boolean shouldCompressMessages) {
        getPortInstance().setCompressMessages(shouldCompressMessages);
    }

    /**
     * Returns the type of port of this portInstance
     *
     * @return <code>1</code> - {@link #OUTPUT_PORT}
     */
    public final int getType() {
        return OUTPUT_PORT;
    }

    /**
     * Returns the underlying port instance
     *
     * @return <code>portInstance</code> held by this object
     */
    public OutputPortInstance getPortInstance() {
        return (OutputPortInstance) portInstance;
    }

    public HashMap getMessageFilters() {
        return portInstance.getMessageFilters();
    }

}
