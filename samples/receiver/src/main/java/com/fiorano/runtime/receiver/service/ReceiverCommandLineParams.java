/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.runtime.receiver.service;

import com.fiorano.esb.util.CommandLineParameters;
import fiorano.esb.util.CommandLineParams;
import fiorano.esb.util.ESBConstants;

/**
 * This class captures the properties like number of connections, sessions, transaction size etc. which are set
 * in the Application Designer.
 *
 * @author FSIPL
 * @version 1.0
 * @created April 10, 2005
 */
public class ReceiverCommandLineParams extends CommandLineParameters {
    private final String TOTAL_MESSAGE_COUNT = "totalmessagecount";
    private final String TRANSACTION_SIZE = "transactionsize";
    private final String SELECTOR = "selector";
    private final String SLEEP_TIME = "sleeptime";

    private final String NUM_CONNECTIONS = "numconnections";
    private final String NUM_SESSIONS = "numsessions";
    private final String NUM_CONSUMERS = "numconsumers";
    private final String IS_TRANSACTED = "istransacted";

    private static final String DEFAULT_CONSUMER_DESTINATION_SUFFIX = "IN_PORT";
    private static final int TOTAL_MESSAGE_COUNT_DEF = 10000;
    private static final int TRANSACTION_SIZE_DEF = 1;
    private static final int NUM_CONNECTIONS_DEF = 1;
    private static final int NUM_SESSIONS_DEF = 1;
    private static final int NUM_CONSUMERS_DEF = 1;
    private static final int SLEEP_TIME_DEF = 1000;

    private int totalMessageCount = TOTAL_MESSAGE_COUNT_DEF;
    private int transactionSize = TRANSACTION_SIZE_DEF;
    private String selector;

    private int connections = NUM_CONNECTIONS_DEF;
    private int sessions = NUM_SESSIONS_DEF;
    private int consumers = NUM_CONSUMERS_DEF;
    private int sleepTime = SLEEP_TIME_DEF;
    private static final boolean TRANSACTED_DEF = true;
    private boolean transacted = TRANSACTED_DEF;

    /**
     * @param args commandline arguments
     */
    public ReceiverCommandLineParams(String[] args) throws Exception {
        for (int i = 0; i + 1 < args.length; i += 2)
            parameters.put((args[i]).toLowerCase().trim(), args[i + 1]);

        load();
    }

    /**
     * Returns sleep time
     */
    public int getSleepTime() {
        return sleepTime;
    }

    /**
     * Returns consumer destination for object
     */
    public String getConsumerDestination() {
        return DEFAULT_CONSUMER_DESTINATION_SUFFIX;
    }


    /**
     * Returns transaction size
     */
    public int getTransactionSize() {
        return transactionSize;
    }

    /**
     * Returns selector
     */
    public String getSelector() {
        return selector;
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
     * Returns number of consumers
     */
    public int getConsumers() {
        return consumers;
    }

    /**
     * Returns transaction size
     */
    public boolean getTransacted() {
        return transacted;
    }

    /**
     * Load Parameters
     */
    protected void load() {
        super.load();        
        String totalMessageCount = (String) parameters.get(TOTAL_MESSAGE_COUNT);
        if (totalMessageCount != null) {
            this.totalMessageCount = (int) Float.parseFloat(totalMessageCount);
        }

        String transactionSize = (String) parameters.get(TRANSACTION_SIZE);
        if (transactionSize != null) {
            this.transactionSize = (int) Float.parseFloat(transactionSize);
        }

        String selector = (String) parameters.get(SELECTOR);
        if (selector != null) {
            this.selector = selector;
        }

        String connections = (String) parameters.get(NUM_CONNECTIONS);
        if (connections != null) {
            this.connections = (int) Float.parseFloat(connections);
        }

        //One session cannot process two messages at the same time.
        //So to improve the performance we can increase session number to some extent.
        String sessions = (String) parameters.get(NUM_SESSIONS);
        if (sessions != null) {
            this.sessions = (int) Float.parseFloat(sessions);
        }

        //Increases consumers may also increase the receive rate.
        String consumers = (String) parameters.get(NUM_CONSUMERS);
        if (consumers != null) {
            this.consumers = (int) Float.parseFloat(consumers);
        }

        String sleepTime = (String) parameters.get(SLEEP_TIME);
        if (sleepTime != null) {
            this.sleepTime = (int) Float.parseFloat(sleepTime);
        }

        String transacted = (String) parameters.get(IS_TRANSACTED);
        if(transacted != null) {
            this.transacted = Boolean.valueOf(transacted).booleanValue();
        }

    }

}
