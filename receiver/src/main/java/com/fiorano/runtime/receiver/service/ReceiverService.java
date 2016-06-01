/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.runtime.receiver.service;

import com.fiorano.microservice.common.log.LogManager;
import com.fiorano.openesb.microservice.ccp.event.component.StatusEvent;
import com.fiorano.runtime.receiver.service.handlers.ReceiverMessageListener;

import com.fiorano.runtime.receiver.service.jms.CCPEventManager;
import com.fiorano.services.common.service.ServerAvailabilityListener;
import com.fiorano.services.common.service.ServiceLifeCycle;
import com.fiorano.services.libraries.jms.helper.MQHelperException;
import com.fiorano.util.ExceptionUtil;
import com.fiorano.util.JavaUtil;
import com.fiorano.util.StringUtil;
import fiorano.esb.util.CommandLineParams;
import fiorano.esb.utils.RBUtil;
import fiorano.esb.util.InMemoryLaunchable;
import com.fiorano.microservice.common.log.LoggerUtil;
import fiorano.esb.util.ESBConstants;
import fiorano.tifosi.dmi.application.InputPortInstance;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Enumeration;
import java.util.Collections;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;

/**
 * Execution class for Receiver.
 *
 * @author FSIPL
 * @version 1.0
 * @created May 20, 2005
 */
public class ReceiverService implements ExceptionListener, InMemoryLaunchable{
    private Logger logger;
    private ReceiverCommandLineParams receiverParams;

    private Connection connection[];
    private Session session[];
    private MessageConsumer consumer[];
    private ReceiverMessageListener messageListener[];

    private volatile long messageCount = 0;
    private boolean isDurable;
    private String subscriptionName;
    private CCPEventManager ccpEventManager;
    private InitialContext context;
    /**
     * Creates JMS objects
     */
    private void create() throws JMSException, NamingException {


        try {
            //noinspection JNDIResourceOpenedButNotSafelyClosed
            context = new InitialContext(createInitialContextEnv());
            logger.log(Level.INFO, RBUtil.getMessage(com.fiorano.runtime.receiver.service.Bundle.class, com.fiorano.runtime.receiver.service.Bundle.INITIAL_CONTEXT_CREATED));
        } catch (NamingException e) {
            String message = RBUtil.getMessage(com.fiorano.runtime.receiver.service.Bundle.class, com.fiorano.runtime.receiver.service.Bundle.INITIAL_CONTEXT_CREATION_ERROR,
                    new String[]{e.getMessage()});

            logger.log(Level.SEVERE, message);
            shutdown(null);
        }

        ConnectionFactory cf = (ConnectionFactory) context.lookup(receiverParams.getConnectionFactory());

        for (int i = 0; i < receiverParams.getConnections(); i++) {
            connection[i] = cf.createConnection(receiverParams.getUsername(), receiverParams.getPassword());
            connection[i].setClientID(receiverParams.getApplicationName() + "__" + String.valueOf(receiverParams.getApplicationVersion()).replace(".", "_") + "__" + receiverParams.getServiceInstanceName() + i);
            connection[i].setExceptionListener(this);

        }


        for (int i = 0; i < receiverParams.getSessions(); i++) {
            Connection connection = this.connection[i % receiverParams.getConnections()];
            session[i] = connection.createSession(
                    receiverParams.getTransacted(), Session.AUTO_ACKNOWLEDGE);
        }


        try {
            createCCPObjects();
            ccpEventManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Destination consumerDestination = (Destination) session[0].createQueue(receiverParams.getApplicationName() + "__" + String.valueOf(receiverParams.getApplicationVersion()).replace(".", "_") + "__" + receiverParams.getServiceInstanceName() + "__" + receiverParams.getConsumerDestination());


        if (isDurable && !(consumerDestination instanceof Topic)) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.CANNOT_CREATE_DURABLE_SUBSCRIBER, new Object[]{consumerDestination.toString()}));
        }
        //Set message listener
        for (int i = 0; i < receiverParams.getConsumers(); i++) {
            Session session = this.session[i % receiverParams.getSessions()];
            if (isDurable && consumerDestination instanceof Topic) {
                consumer[i] = session.createDurableSubscriber((Topic) consumerDestination, subscriptionName, null, false);
            } else {
                consumer[i] = session.createConsumer(consumerDestination, null, false);
            }
            messageListener[i] = new ReceiverMessageListener(this, session);
            consumer[i].setMessageListener(messageListener[i]);
        }

        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.CONFIG_PARAMS));
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TOTAL_MESSAGES, new Object[]{
                receiverParams.getTotalMessageCount()
        }));
        if (receiverParams.getTransacted()) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TRANSACTION_SIZE, new Object[]{
                    receiverParams.getTransactionSize()
            }));
        }
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.IS_TRANSACTED, new Object[]{
                receiverParams.getTransacted()
        }));
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.SELECTOR,
                new Object[]{receiverParams.getSelector()}));
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.SLEEP_INTERVAL,
                new Object[]{receiverParams.getSleepTime()}));

        for (int i = 0; i < receiverParams.getConnections(); i++) {
            connection[i].start();
        }

        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INIT_RECEIVER));


        StatusEvent statusEvent = new StatusEvent();
        statusEvent.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
        statusEvent.setStatus(StatusEvent.Status.COMPONENT_STARTED);
        statusEvent.setStatusType(StatusEvent.StatusType.INFORMATION);
        ccpEventManager.getCCPEventGenerator().sendEvent(statusEvent);


        while (!allMessagesReceived()) {
            if (logger.isLoggable(Level.FINE)) {
                if (receiverParams.getConsumers() > 1) {
                    for (int i = 0; i < receiverParams.getConsumers(); i++) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.CONSUMER_TOTAL_MESSAGES,
                                    new Object[]{i + 1, messageListener[i].getMessageCount()
                                    }));
                        }
                    }
                }
            }
            if (logger.isLoggable(Level.INFO)) {
                if (receiverParams.getConsumers() >= 1) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ALL_CONSUMER_MESSAGES)
                            + messageCount);
                }
            }
            try {
                Thread.sleep(receiverParams.getSleepTime());
            } catch (InterruptedException exp) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SLEEP_ERROR,
                        new Object[]{exp.getLocalizedMessage()}));
            }
        }
        float rate;
        float totalRate = 0;
        int unusedConsumers = 0;
        for (int i = 0; i < receiverParams.getConsumers(); i++) {
            rate = messageListener[i].getRate();
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.FINAL_CONSUMER_RATE, new Object[]{
                    i + 1, messageListener[i].getMessageCount(), rate}));
            // Average rate decreases incase where listeners > messages
            if (rate != -1) {
                totalRate = totalRate + rate;
            } else {
                unusedConsumers++;
            }
        }

        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ALL_MESSAGES_RECEIVED,
                new Object[]{getTotalMessageCount()}));
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.AVERAGE_RATE, new Object[]{
                totalRate / (receiverParams.getConsumers() - unusedConsumers)}));

        close();
    }

    public ReceiverCommandLineParams getReceiverParams() {
        return receiverParams;
    }

    public Connection getConnection() {
        return connection[0];
    }

    protected void createCCPObjects() {

        ccpEventManager = new CCPEventManager(this);
    }


    public String getServiceLookupName() {
        return receiverParams.getApplicationName() + "__"
                + String.valueOf(receiverParams.getApplicationVersion()).replace(".", "_") + "__"
                + receiverParams.getServiceInstanceName();
    }


    /**
     * Closes all the connections
     */
    public void close() {

        StatusEvent statusEvent = new StatusEvent();
        statusEvent.setOperationScope(StatusEvent.OperationScope.COMPONENT_STOP);
        statusEvent.setStatus(StatusEvent.Status.COMPONENT_STOPPED);
        statusEvent.setStatusType(StatusEvent.StatusType.INFORMATION);
        ccpEventManager.getCCPEventGenerator().sendEvent(statusEvent);
        try {
            ccpEventManager.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < receiverParams.getConnections(); i++) {
            try {
                if (connection[i] != null) {
                    connection[i].close();
                }
            } catch (JMSException e) {
                logger.log(Level.WARNING, ExceptionUtil.getStackTrace(e));
            }
        }
        if (receiverParams != null) {
            if (!receiverParams.isInmemoryLaunchable()) {
                System.exit(-1);
            }
        }
    }

    /**
     * Returns logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the transaction size
     */
    public int getTransactionSize() {
        return receiverParams.getTransactionSize();
    }

    /**
     * Returns transacted
     */
    public boolean getTransacted() {
        return receiverParams.getTransacted();
    }


    /**
     * Returns the total message count
     */
    private int getTotalMessageCount() {
        return receiverParams.getTotalMessageCount();
    }

    /**
     * Checks whether all messages are received or not
     */
    private boolean allMessagesReceived() {
        return (messageCount >= getTotalMessageCount());
    }

    /**
     * Increments the message count
     */
    public synchronized void incrementMessageCount() {
        messageCount++;
    }

    /**
     * Overridden method from Exception Listener
     *
     * @param jmsException JMSException object
     */
    public void onException(JMSException jmsException) {
        close();
    }


    /**
     * @param args commandline arguments
     */
    public static void main(String args[]) {
        ReceiverService receiver = new ReceiverService();

        receiver.startup(args);
    }

    protected LogManager logManager;

    public void clearOutLogs() {
        logManager.clearOutLogs(receiverParams.getApplicationName() + "__"
                + String.valueOf(receiverParams.getApplicationVersion()) + "__"
                + receiverParams.getServiceInstanceName(), getLogger());
    }

    public void clearErrLogs() {
        logManager.clearErrLogs(receiverParams.getApplicationName() + "__"
                + String.valueOf(receiverParams.getApplicationVersion()) + "__"
                + receiverParams.getServiceInstanceName(), getLogger());
    }


    /**
     * Called for inmemory launch
     *
     * @param args commandline arguments
     */
    public void startup(String[] args) {
        try {
            receiverParams = new ReceiverCommandLineParams(args);
            logManager = com.fiorano.microservice.common.log.LoggerUtil.createLogHandlers(receiverParams);
            logger = LoggerUtil.getServiceLogger("COM.FIORANO.EDBC.RECEIVER", getServiceLookupName(),
                    receiverParams.getServiceGUID());
            //   LoggerUtil.addFioranoConsoleHandler(logger);
            if (!receiverParams.isInmemoryLaunchable()) {
                try {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PROCESS_ID, new Object[]{JavaUtil.getPID()}));
                } catch (Exception e) {
                    logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_LOGGING_PROCESS_ID, new Object[]{e.getMessage()}));
                }
            }
            for (int i = 0; i < args.length; i++) {
                if (ESBConstants.PASSWORD.equals(args[i])) {
                    i++;
                } else {
                    logger.log(Level.INFO,
                            RBUtil.getMessage(Bundle.class, Bundle.PRINT_ARGS, new Object[]{i, args[i]}));
                }
            }
            connection = new Connection[receiverParams.getConnections()];
            session = new Session[receiverParams.getSessions()];
            consumer = new MessageConsumer[receiverParams.getConsumers()];
            messageListener = new ReceiverMessageListener[receiverParams.getConsumers()];
            create();


        } catch (Exception e) {
            if (logger != null && logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_SERVICE), e);
            }
            shutdown(e);
        }
    }

    /**
     * Called when the component is launched in inmemory
     *
     * @param hint currently not used
     */
    public void shutdown(Object hint) {
        close();
    }

    public int waitFor() throws InterruptedException {
        return 0;
    }

    public int exitValue() {
        return 0;
    }

    public void stop() {
        shutdown(this);
    }


    private Hashtable<Object, Object> createInitialContextEnv() throws NamingException {
        //  JNDIConfiguration jndiConfiguration = configuration.getJndiConfiguration();
        Hashtable<Object, Object> env = new Hashtable<Object, Object>();
        if (!StringUtil.isEmpty(receiverParams.getUsername())) {
            env.put(Context.SECURITY_PRINCIPAL, receiverParams.getUsername());
        }
        if (!StringUtil.isEmpty(receiverParams.getPassword())) {
            env.put(Context.SECURITY_CREDENTIALS, receiverParams.getPassword());
        }
        env.put(Context.PROVIDER_URL, receiverParams.getURL());
        if(receiverParams.getInitialContextFactory() == null) {
            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        }else{
            env.put(Context.INITIAL_CONTEXT_FACTORY, receiverParams.getInitialContextFactory());
        }
        if (receiverParams.getAdditionalEnvProperties() != null
                && receiverParams.getAdditionalEnvProperties().size() > 0) {
            env.putAll(receiverParams.getAdditionalEnvProperties());
        }
        return env;
    }

    private boolean isConnectionError(NamingException e) {
        return false; //todo
    }

}
