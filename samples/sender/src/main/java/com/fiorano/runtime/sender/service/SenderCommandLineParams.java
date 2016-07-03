/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.runtime.sender.service;

import com.fiorano.esb.util.CommandLineParameters;
//import fiorano.esb.util.ESBConstants;

/**
 * This class captures the params set in the Application Designer for the Sender component
 *
 * @author FSIPL
 * @version 1.0
 * @created April 10, 2005
 */
public class SenderCommandLineParams extends CommandLineParameters {
    private final String XML_FILE_PATH = "xmlfilepath";
    private final String TOTAL_MESSAGE_COUNT = "totalmessagecount";
    private final String TRANSACTION_SIZE = "transactionsize";
    private final String MESSAGE_SIZE = "msgsize";
    private final String NUM_CONNECTIONS = "numconnections";
    private final String NUM_SESSIONS = "numsessions";
    private final String NUM_PRODUCERS = "numproducers";
    private final String IS_TRANSACTED = "isTransacted";

    private static final int TOTAL_MESSAGE_COUNT_DEF = 10000;
    private static final int TRANSACTION_SIZE_DEF = 500;
    private static final int MESSAGE_SIZE_DEF = 1024;
    private static final int NUM_CONNECTIONS_DEF = 1;
    private static final int NUM_SESSIONS_DEF = 1;
    private static final int NUM_PRODUCERS_DEF = 1;
    private static final String DEFAULT_PRODUCER_DESTINATION_SUFFIX = "OUT_PORT";

    private String xmlFilePath;
    private int totalMessageCount = TOTAL_MESSAGE_COUNT_DEF;
    private int transactionSize = TRANSACTION_SIZE_DEF;
    private int messageSize = MESSAGE_SIZE_DEF;

    private int connections = NUM_CONNECTIONS_DEF;
    private int sessions = NUM_SESSIONS_DEF;
    private int producers = NUM_PRODUCERS_DEF;
    private boolean TransactedSession = IS_TRANSACTED_SESSION_DEF;

    /**
     * @param args commandline arguments
     */
    public SenderCommandLineParams(String[] args) throws Exception {
        super(args);
        for (int i = 0; i + 1 < args.length; i += 2)
            parameters.put((args[i]).toLowerCase().trim(), args[i + 1]);

        load();
    }

    /**
     * Returns producer destination for object
     */
    public String getProducerDestination() {
        // return getConnectionFactory() + ESBConstants.JNDI_CONSTANT +
        //      DEFAULT_PRODUCER_DESTINATION_SUFFIX;
        return DEFAULT_PRODUCER_DESTINATION_SUFFIX;
    }

    /**
     * Returns xml file path for object
     */
    public String getXMLFilePath() {
        return xmlFilePath;
    }

    /**
     * Returns transaction size
     */
    public int getTransactionSize() {
        return transactionSize;
    }

    /**
     * Returns message size
     */
    public int getMessageSize() {
        return messageSize;
    }

    /**
     * Returns total mesage count
     */
    public int getTotalMessageCount() {
        return totalMessageCount;
    }

    /**
     * Returns number of connections
     */
    public int getConnections() {
        return connections;
    }

    /**
     * Returns number of sessions
     */
    public int getSessions() {
        return sessions;
    }

    /**
     * Returns number of producers
     */
    public int getProducers() {
        return producers;
    }

    /**
     * Returns whether session is transacted or not
     *
     */
    public boolean IsTransactedSession() {
        return TransactedSession;
    }

    /**
     * Loads Parameters
     */
    protected void load() {
        super.load();

        if (getParameter(XML_FILE_PATH) != null)
            xmlFilePath = (String) getParameter(XML_FILE_PATH);
        else
            xmlFilePath = "";
        String totalMessageCount = (String) getParameter(TOTAL_MESSAGE_COUNT);
        if (totalMessageCount != null) {
            this.totalMessageCount = (int) Float.parseFloat(totalMessageCount);
        }

        String transactionSize = (String) getParameter(TRANSACTION_SIZE);
        if (transactionSize != null) {
            this.transactionSize = (int) Float.parseFloat(transactionSize);
        }

        String messageSize = (String) getParameter(MESSAGE_SIZE);
        if (messageSize != null) {
            this.messageSize = (int) Float.parseFloat(messageSize);
        }

        String connections = (String) getParameter(NUM_CONNECTIONS);
        if (connections != null) {
            this.connections = (int) Float.parseFloat(connections);
        }

        //One session cannot process two messages at the same time.
        //So to improve the performance we can increase session number to some extent.
        String sessions = (String) getParameter(NUM_SESSIONS);
        if (sessions != null) {
            this.sessions = (int) Float.parseFloat(sessions);
        }

        //Increases producers may also increase the send rate
        String producers = (String) getParameter(NUM_PRODUCERS);
        if (producers != null) {
            this.producers = (int) Float.parseFloat(producers);
        }

        String isTransacted = (String) getParameter(IS_TRANSACTED);
        TransactedSession = Boolean.valueOf(isTransacted);
    }

}
