/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener;

/**
 * Created by ADMIN on 5/24/2016.
 */
public interface Bundle {

    /**
     * @msg.message msg="Server JMXService URL"
     */
    public final static String SERVER_JMX_URL_NAME = "server_jmx_url_name";
    /**
     * @msg.message msg="Specify JMX Service URL"
     */
    public final static String SERVER_JMX_URL_DESC = "server_jmx_url_desc";

    /**
     * @msg.message msg="Use Durable Subscriptions"
     */
    public final static String USE_DURABLE_SUBSCRIPTIONS_NAME = "use_durable_subscriptions_name";
    /**
     * @msg.message msg="Yes - to create durable subscribers"
     */
    public final static String USE_DURABLE_SUBSCRIPTIONS_DESC = "use_durable_subscriptions_desc";

    /**
     * @msg.message msg="Time Slice To Ping Peers"
     */
    public final static String TIME_SLICE_FOR_PING_NAME = "time_slice_for_ping_name";
    /**
     * @msg.message msg="Time Slice Interval to Ping Peers"
     */
    public final static String TIME_SLICE_FOR_PING_DESC = "time_slice_for_ping_desc";

    /**
     * @msg.message msg="Regex of Topic Name"
     */
    public final static String TOPIC_REGEX_NAME = "topic_regex_name";
    /**
     * @msg.message msg="Specify regular expression table for topic name to which
     * to listen for exceptions , Regular expressions are case sensitive. Refer help for usage"
     */
    public final static String TOPIC_REGEX_DESC = "topic_regex_desc";

    /**
     * @msg.message msg="Security Principal"
     */
    public final static String JMX_USERNAME_NAME = "jmx_username_name";
    /**
     * @msg.message msg="Specifies the identity of the principal for authenticating the caller to the service."
     */
    public final static String JMX_USERNAME_DESC = "jmx_username_desc";

    /**
     * @msg.message msg="Security Credential"
     */
    public final static String JMX_PASSWORD_NAME = "jmx_password_name";
    /**
     * @msg.message msg="Specifies the credentials of the principal for authenticating the caller to the service."
     */
    public final static String JMX_PASSWORD_DESC = "jmx_password_desc";

    /**
     * @msg.message msg="JMS Provider"
     */
    public final static String JMS_PROVIDER_NAME = "jms_provider_name";
    /**
     * @msg.message msg="Choose JMS provider"
     */
    public final static String JMS_PROVIDER_DESC = "jms_provider_desc";

    /**
     * @msg.message msg="JNDI Configuration"
     */
    public final static String JNDI_CONFIG_DESC = "jndi_config_desc";
    /**
     * @msg.message msg="JNDI details"
     */
    public final static String JNDI_CONFIG_NAME = "jndi_config_name";

    /**
     * @msg.message msg="Server Connection Configuration"
     */
    public final static String CONNECTION_CONFIG_NAME = "connection_config_name";
    /**
     * @msg.message msg="Connection details to connect to JMS server"
     */
    public final static String CONNECTION_CONFIG_DESC = "connection_config_desc";


    /**
     * @msg.message msg="ConnectorHost"
     */
    public final static String JMX_HOST_NAME = "jmx_host_name";
    /**
     * @msg.message msg="The name or IP address of the host providing the JMXConnector service"
     */
    public final static String JMX_HOST_DESC = "jmx_host_desc";

    /**
     * @msg.message msg="ConnectorPort"
     */
    public final static String JMX_PORT_NAME = "jmx_port_name";
    /**
     * @msg.message msg="The port providing the JMXConnector service"
     */
    public final static String JMX_PORT_DESC = "jmx_port_desc";


    /**
     * @msg.message msg="Regex for the topic name can not be empty"
     */
    public final static String EMPTY_TOPIC_REGEX = "empty_topic_regex";

    /**
     * @msg.message msg="Name cannot be empty"
     */
    public final static String NAME_EMPTY = "name_empty";

    /**
     * @msg.message msg="Value for {0} should be either 'true' or 'false'"
     */
    public final static String TRUE_OR_FALSE = "true_or_false";
    /**
     * @msg.message msg="Session close failed. "
     */
    public final static String SESSION_CLOSE_FAILED = "session_close_failed";
    /**
     * @msg.message msg="Message <MESSAGE>{0}</MESSAGE>"
     */
    public final static String RECIEVED_MESSAGE = "recieved_message";

    /**
     * @msg.message msg="Lookup failed while connecting to {0} "
     */
    public final static String ERROR_CONNECTING_PROVIDER_URL = "error_connecting_provider_url";

    /**
     * @msg.message msg="Error Connecting to FioranoServiceProvider  {0}"
     */
    public final static String ERROR_CREATING_SERVICE_PROVIDER = "error_creating_service_provider";

    /**
     * @msg.message msg="Connected to Server ..."
     */
    public final static String CONNECTED_TO_SERVER = "connected_to_server";

    /**
     * @msg.message msg="JMS Server Connection Reset !!!. Stopping the component. {0}"
     */
    public final static String JMS_SERVER_CONNECTION_RESET = "jms_server_connection_reset";

    /**
     * @msg.message msg="Error in Subscribing to topics. {0}"
     */
    public final static String ERROR_SUBSCRIBING_TO_TOPICS = "error_subscribing_to_topics";

    /**
     * @msg.message msg="Error in getting topics from peers. One of the possible reasons may be Enterprise restart "
     */
    public final static String ERROR_IN_GETTING_TOPICS_FROM_PEERS = "error_in_getting_topics_from_peers";

    /**
     * @msg.message msg="Error in getting adminservice elements."
     */
    public final static String ERROR_IN_GETTING_ADMINSERVICE_ELEMENTS = "error_in_getting_adminservice_elements";

    /**
     * @msg.message msg="Error in getting peers from service provider."
     */
    public final static String ERROR_IN_GETTING_PEERS_FROM_SERVICEPROVIDER = "error_in_getting_peers_from_serviceprovider";

    /**
     * @msg.message msg="Subscribing to topic: {0} "
     */
    public final static String SUBSCRIBING_TO_TOPIC = "subscribing_to_topic";

    /**
     * @msg.message msg=" Connecting to URL : {0}"
     */
    public final static String CONNECTING_TO_URL = "connecting_to_url";

    /**
     * @msg.message msg=" Connected to Peer Server : {0}"
     */
    public final static String PEER_SERVER_NAME = "peer_server_name";

    /**
     * @msg.message msg=" Topic Forcibly Closed : {0}"
     */
    public final static String TOPIC_FORCIBLY_CLOSED = "topic_forcibly_closed";

    /**
     * @msg.message msg="Polling of Topics is Stopped"
     */
    public final static String POLLING_STOPED = "polling_stoped";

    /**
     * @msg.message msg="Unable to send the message to the OUT_PORT destination: {0}"
     */
    public final static String ERROR_SENDING_MESSAGE = "error_sending_message";

    /**
     * @msg.message msg="Look up failed in Initial Context: {0}"
     */
    public final static String ERROR_LOOKUP_FAILED = "error_lookup_failed";

    /**
     * @msg.message msg="Error creating MQ Admin Connection"
     */
    public final static String ERROR_CREATING_MQADMINCONNECTION = "error_creating_mqadminconnection";

    /**
     * @msg.message msg="server is not active"
     */
    public final static String SERVER_NOT_ACTIVE = "server_not_active";

    /**
     * @msg.message msg="Exception Occured : {0}"
     */
    public final static String EXCEP_OCC = "excep_occ";

    /**
     * @msg.message msg="Exception - {0}. Message - {1}."
     */
    public final static String EXCEP_MSG = "excep_msg";

    /**
     * @msg.message msg="ERROR WHILE SENDING THE EXCEPTION: "
     */
    public final static String SEND_EXCEP_ERROR = "send_excep_error";

    /**
     * @msg.message msg="Exception Occurred While Closing Connections : {0} "
     */
    public final static String EXCP_WHILE_CLOSING_CONNECTIONS = "excp_while_closing_connections";

}
