/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.microservice.common.port;

import com.fiorano.esb.wrapper.JMSUserIdentity;
//import com.fiorano.esb.wrapper.PortSchemaUtil;
import com.fiorano.esb.wrapper.UserDefinedDestination;
import com.fiorano.openesb.application.application.PortInstance;
import com.fiorano.util.Util;
import fiorano.esb.record.ESBRecordDefinition;

/**
 * <code>PortInstanceAdapter</code> is an adapter (wrapper class) for <code>PortInstance</code>. It holds an
 * <code>PortInstance</code> object and delgates all calls on this object to corresponding method(s) of
 * <code>PortInstance</code> object. Services should use <code>PortInstanceAdapter</code> instead of
 * <code>PortInstance</code> to avoid tight coupling with classes from FioranoESB.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class PortInstanceAdapter {

    /**
     * PortInstance object to which this class delegates the calls on its methods.
     */
    protected PortInstance portInstance;
    /**
     * value indicating port is input port
     */
    public static int INPUT_PORT = 0;
    /**
     * value indicating port is output port
     */
    public static int OUTPUT_PORT = 1;

    protected PortInstanceAdapter(PortInstance portInstance) {
        if (portInstance == null) {
            throw new IllegalArgumentException("Cannot wrap a null port instance");
        }
        this.portInstance = portInstance;
    }

    /**
     * Returns the name of <code>portInstance</code>
     *
     * @return port name
     */
    public String getName() {
        return portInstance.getName();
    }

    /**
     * Sets the name of <code>portInstance</code>
     *
     * @param name port name
     */
    public void setName(String name) {
        portInstance.setName(name);
    }

    /**
     * Returns the schema for messages passing through this <code>portInstance</code>. If messages expected
     * on this port are not XML then null is returned.
     *
     * @return <code>ESBRecordDefinition</code> for XML messages passing through this port or null if messages exoected are non XML
     */
    public ESBRecordDefinition getSchema() {
        return PortSchemaUtil.getPortSchema(portInstance);
    }

    /**
     * Sets the schema for messages passing through this <code>portInstance</code>. If messages expected
     * on this port are not XML then null is returned.
     *
     * @param schemaDefinition <code>ESBRecordDefinition</code> for XML messages passing through this port or null if messages exoected are non XML
     */
    public void setSchema(ESBRecordDefinition schemaDefinition) {
        PortSchemaUtil.setPortSchema(portInstance, schemaDefinition);
    }

    /**
     * Returns description of <code>portInstance</code>. Typically this a short statement explaining purpose and/or behaviour of portInstance
     *
     * @return description of port
     */
    public String getDescription() {
        return portInstance.getDescription();
    }

    /**
     * Sets description of <code>portInstance</code>. Typically a short statement explaining purpose and/or behaviour of portInstance
     *
     * @param description description of port
     */
    public void setPortDescription(String description) {
        portInstance.setDescription(description);
    }

    /**
     * Returns whether <code>portInstance</code> supports request reply.
     *
     * @return true if request-reply is supported, false otherwise
     */
    public boolean requestReplySupported() {
        return portInstance.isRequestReply();
    }

    /**
     * Enables / disables request reply functionality on portInstance. If enabled, responses for messages received on this port
     * should be send to {@link javax.jms.Message#getJMSReplyTo()}, if {@link javax.jms.Message#getJMSReplyTo()} is not null
     *
     * @param requestReplySupported true if request reply should be enabled false otherwise.
     */
    void supportRequestReply(boolean requestReplySupported) {
        portInstance.setRequestReply(requestReplySupported);
    }

    /**
     * Determines if <code>portInstance</code> is enabled or disabled. If <code>portInstance</code> is disabled, related JMS objects
     * may not be created.
     *
     * @return true if port is enabled, false if disabled
     */
    public boolean isEnabled() {
        return portInstance.isEnabled();
    }

    /**
     * Enables /disables <code>portInstance</code>. If <code>portInstance</code> is disabled, related JMS objects
     * may not be created.
     *
     * @param enabled true if port has to be enabled, false otherwise.
     */
    public void setEnabled(boolean enabled) {
        portInstance.setEnabled(enabled);
    }

    /**
     * value indicating port destination type is <code>javax.jms.Queue</code>
     */
    public static final int DESTINATION_TYPE_QUEUE = PortInstance.DESTINATION_TYPE_QUEUE;
    /**
     * value indicating port destination type is <code>javax.jms.Topic</code>
     */
    public static final int DESTINATION_TYPE_TOPIC = PortInstance.DESTINATION_TYPE_TOPIC;

    /**
     * Returns destination type of <code>portInstance</code>
     *
     * @return {@link #DESTINATION_TYPE_QUEUE} if port destination is <code>javax.jms.Queue</code> <br>
     *         {@link #DESTINATION_TYPE_TOPIC} if port destination is <code>javax.jms.Topic</code>
     */
    public int getDestinationType() {
        return portInstance.getDestinationType();
    }

    /**
     * Sets destination type of <code>portInstance</code>
     *
     * @param destinationType {@link #DESTINATION_TYPE_QUEUE} if port destination is <code>javax.jms.Queue</code> <br>
     * {@link #DESTINATION_TYPE_TOPIC} if port destination is <code>javax.jms.Topic</code>
     */
    public void setDestinationType(int destinationType) {
        portInstance.setDestinationType(destinationType);
    }

    /**
     * Returns the client ID can to be set on <code>javax.jms.Connection</code>
     *
     * @return client ID to be set on <code>javax.jms.Connection</code>
     */
    public String getClientID() {
        return portInstance.getClientID();
    }

    /**
     * Sets the client ID can to be set on <code>javax.jms.Connection</code>
     *
     * @param clientID client ID to be set on <code>javax.jms.Connection</code>
     */
    void setClientID(String clientID) {
        portInstance.setClientID(clientID);
    }

    /**
     * Returns user identity details for creating <code>javax.jms.Connection</code>
     *
     * @return user identity details for creating <code>javax.jms.Connection</code>
     */
//    public JMSUserIdentity getJMSUserIdentity() {
//        return new JMSUserIdentity(portInstance.getUser(), portInstance.getPassword());
//    }
//
//    /**
//     * Sets user identity details for creating <code>javax.jms.Connection</code>
//     *
//     * @param jmsUserIdentity user identity details for creating <code>javax.jms.Connection</code>
//     */
//    void setJMSUserIdentity(JMSUserIdentity jmsUserIdentity) {
//        if (jmsUserIdentity == null) {
//            return;
//        }
//        portInstance.setUser(jmsUserIdentity.getUsername());
//        portInstance.setPassword(jmsUserIdentity.getPassword());
//    }

//    /**
//     * Returns configuration details of user defined destination. User defined destination is used to bind port to a
//     * specific destination rather than the default destination. Default desitnation of port is
//     * <code>[application_GUID]__[service_instance_name]__[port_name]</code>. If this value is not null and
//     * {@link com.fiorano.esb.wrapper.UserDefinedDestination#isEnabled()} is true, then the destination specified in
//     * <code>UserDefinedDestination</code> should be used
//     *
//     * @return configuration details of user defined destination
//     */
//    public UserDefinedDestination getUserDefinedDestination() {
//        return new UserDefinedDestination(portInstance.isSpecifiedDestinationUsed(), portInstance.getDestinationType(), portInstance.getDestination());
//    }

    /**
     * Returns configuration details of user defined destination. User defined destination is used to bind port to a
     * specific destination rather than the default destination. Default desitnation of port is
     * <code>[application_GUID]__[service_instance_name]__[port_name]</code>. If this value is set and
     * {@link com.fiorano.esb.wrapper.UserDefinedDestination#isEnabled()} is true, then the destination specified in
     * <code>UserDefinedDestination</code> should be used
     *
     * @param userDefinedDestination configuration details of user defined destination
     */
    void setUserDefinedDestination(UserDefinedDestination userDefinedDestination) {
        if (userDefinedDestination == null) {
            return;
        }
        portInstance.setSpecifiedDestinationUsed(userDefinedDestination.isEnabled());
        portInstance.setDestinationType(userDefinedDestination.getType());
        portInstance.setDestination(userDefinedDestination.getName());
    }

    /**
     * Resets <code>portInstance</code>. All values except name, description, Requestreply and Schema are reset to default values.
     */
    public void reset() {
        portInstance.reset();
    }

    /**
     * Returns the type of port of this portInstance
     *
     * @return <code>0</code> - {@link #INPUT_PORT} <br>
     *         <code>1</code> - {@link #OUTPUT_PORT}
     */
    public abstract int getType();

    /**
     * Overriding this so that when used in collections the collection will not contain two ports with same name.
     * Two <code>PortInstanceAdapter</code>s are equal if they have same name.
     *
     * @param that object which should be compared with this object
     * @return <code>true</code> if this object is the same as <code>that</code>
     *         argument; <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        if (!this.getClass().isInstance(that)) {
            return false;
        }
        if (that == this) {
            return true;
        }
        return Util.equals(this.getName(), ((com.fiorano.esb.wrapper.PortInstanceAdapter) that).getName());
    }

    /**
     * Hashcode value for this object. Returns {@link #getName()}.hashCode(), so a Set or Map can contain only one
     * port instance for a given name
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return getName() == null ? 0 : getName().hashCode();
    }

}
