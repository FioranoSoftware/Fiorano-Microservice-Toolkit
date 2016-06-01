/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.microservice.common.port;

import com.fiorano.openesb.application.application.InputPortInstance;

import java.util.HashMap;

/**
 * <code>InputPortInstanceAdapter</code> is an adapter (wrapper class) for <code>InputPortInstance</code>. It holds an
 * <code>InputPortInstance</code> object and delgates all calls on this object to corresponding method(s) of
 * <code>InputPortInstance</code> object. Services should use <code>InputPortInstanceAdapter</code> instead of
 * <code>InputPortInstance</code> to avoid tight coupling with classes from FioranoESB.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public final class InputPortInstanceAdapter extends PortInstanceAdapter {

    /**
     * Creates an <code>InputPortInstanceAdapter</code> which holds <code>inputPortInstance<code>. This is used by classes in library to create
     * an adpater for <code>intputPortInstance<code> which can be passed to service
     *
     * @param inputPortInstance underlying input port instance object
     */
    public InputPortInstanceAdapter(InputPortInstance inputPortInstance) {
        super(inputPortInstance);
    }

    /**
     * Returns whether a durable subscriber shoud be created on the topic denoted by this port. This has no effect if the destination is
     * <code>jvax.jms.Queue</code>
     *
     * @return true if durable subscriber should be created on topic, false otherwise
     */
    public boolean isDurableSubscription() {
        return getPortInstance().isDurableSubscription();
    }

    /**
     * Sets whether {@link #getPortInstance()} should indicate service that durable subsriber has to be created or not.
     *
     * @param durableSubsrciption true, if this port should indicate service to create durable subscriber, false otherwise
     */
    void setDurableSubscription(boolean durableSubsrciption) {
        getPortInstance().setDurableSubscription(durableSubsrciption);
    }

    /**
     * Returns the subscription name to use when durable subscription is being used, this should be used while creating a consumer and works only if
     * destination is <code>javax.jms.Topic</code>.
     *
     * @return subscription name for durable subscription
     */
    public String getSubscriptionName() {
        return getPortInstance().getSubscriptionName();
    }

    /**
     * Sets the subscription name to use when durable subscription is being used, this should be used while creating a consumer and works only if
     * destination is topic
     *
     * @param subscriptionName name for durable durable subscription
     */
    void setSubscriptionName(String subscriptionName) {
        getPortInstance().setSubscriptionName(subscriptionName);
    }

    /**
     * Returns the message selector which can be set on Message Consumer. The messages which satisfy the selection only will be delivered to the
     * consumer
     *
     * @return selector to be used on messages before delivering them to consumer
     */
    public String getMessageSelector() {
        return getPortInstance().getMessageSelector();
    }

    /**
     * Sets the message selector which can be should be set on consumer created on the destination specififed by {@link #getPortInstance()}
     *
     * @param messageSelector selector to be applied on messages
     */
    void setMessageSelector(String messageSelector) {
        getPortInstance().setMessageSelector(messageSelector);
    }

    /**
     * Returns configuration details for creating JMS session which can be used to create JMS objects that handle request coming on this port
     *
     * @return Configuration of JMS Session that has to be created.
     */
    public SessionConfiguration getSessionConfiguration() {
        return new SessionConfiguration(getPortInstance().isTransacted(), getPortInstance().getAcknowledgementMode(),
                                        getPortInstance().getSessionCount(), getPortInstance().getTransactionSize());
    }

    /**
     * Returns configuration details for creating JMS session which can be used to create JMS objects that handle request coming on this port
     *
     * @param sessionConfiguration Configuration of JMS Session that has to be created
     */
    void setSessionConfiguration(SessionConfiguration sessionConfiguration) {
        if (sessionConfiguration == null) {
            return;
        }
        getPortInstance().setAcknowledgementMode(sessionConfiguration.getAcknowledgementMode());
        getPortInstance().setSessionCount(sessionConfiguration.getCount());
        getPortInstance().setTransacted(sessionConfiguration.isTransacted());
        getPortInstance().setTransactionSize(sessionConfiguration.getTransactionSize());
    }


    /**
     * Returns the type of port of this portInstance
     *
     * @return <code>0</code> - {@link #INPUT_PORT}
     */
    public final int getType() {
        return INPUT_PORT;
    }

    /**
     * Returns the underlying port instance
     *
     * @return <code>portInstance</code> held by this object
     */
    public InputPortInstance getPortInstance() {
        return (InputPortInstance) portInstance;
    }

    public HashMap getMessageFilters() {
        return portInstance.getMessageFilters();
    }

}
