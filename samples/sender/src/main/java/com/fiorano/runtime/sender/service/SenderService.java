/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.runtime.sender.service;


import com.fiorano.esb.wrapper.IJNDILookupHelper;
import com.fiorano.esb.wrapper.JNDILookupHelper;
import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import com.fiorano.microservice.common.log.LogManager;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.Data;
import com.fiorano.openesb.microservice.ccp.event.component.StatusEvent;
import com.fiorano.services.common.service.ServerAvailabilityListener;
import com.fiorano.services.common.service.ServiceLifeCycle;
import com.fiorano.util.ExceptionUtil;
import com.fiorano.util.JavaUtil;
import fiorano.esb.util.InMemoryLaunchable;
import  com.fiorano.microservice.common.log.LoggerUtil;
import fiorano.esb.util.ESBConstants;
import fiorano.esb.utils.RBUtil;
import fiorano.jms.common.FioranoException;
import com.fiorano.util.StringUtil;
import com.fiorano.runtime.sender.service.jms.*;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Collection;
import java.util.Hashtable;



import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Service class for Sender
 *
 * @author FSIPL
 * @version 1.0
 * @created May 20, 2005
 */
public class SenderService implements ExceptionListener, InMemoryLaunchable, ServiceLifeCycle {
    private Logger logger;
    private SenderCommandLineParams senderParams;
    private Connection connection[];
    private Session session[];
    private MessageProducer producer[];
    private ProducerThread producerThread[];
    private volatile boolean running = true;
    private String content;
    private Hashtable outPorts;
    private CCPEventManager ccpEventManager;
    private InitialContext context;


    /**
     * Create JMS transport
     */
    private void create() throws JMSException, NamingException {
        IJNDILookupHelper lookupHelper = null;

        Destination producerDestination;

        try {
            //noinspection JNDIResourceOpenedButNotSafelyClosed
            context = new InitialContext(createInitialContextEnv());
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INITIAL_CONTEXT_CREATED));
        } catch (NamingException e) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.INITIAL_CONTEXT_CREATION_ERROR,
                    new String[]{e.getMessage()});

            logger.log(Level.SEVERE, message);
            shutdown(null);
        }

        ConnectionFactory cf = (ConnectionFactory) context.lookup(senderParams.getConnectionFactory());


        for (int i = 0; i < senderParams.getConnections(); i++) {
            connection[i] = cf.createConnection(senderParams.getUsername(), senderParams.getPassword());
            connection[i].setClientID(senderParams.getApplicationName() + "__" + String.valueOf(senderParams.getApplicationVersion()).replace(".", "_") + "__" + senderParams.getServiceInstanceName() + i);
            connection[i].setExceptionListener(this);
//            new ServerAvailabilityListener(this,connection[i],logger);
            connection[i].start();
        }


        for (int i = 0; i < senderParams.getSessions(); i++) {
            Connection connection = this.connection[i % senderParams.getConnections()];
            session[i] = connection.createSession(
                    senderParams.IsTransactedSession(), Session.AUTO_ACKNOWLEDGE);

        }

        try {
            createCCPObjects();
            ccpEventManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        producerDestination = session[0].createTopic(senderParams.getApplicationName() + "__" + String.valueOf(senderParams.getApplicationVersion()).replace(".", "_") + "__" + senderParams.getServiceInstanceName() + "__" + senderParams.getProducerDestination());

        // create producer
        for (int i = 0; i < senderParams.getProducers(); i++) {
            Session session = this.session[i % senderParams.getSessions()];
            producer[i] = session.createProducer(producerDestination);
            producerThread[i] = new ProducerThread(producer[i], session);
        }


        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.CONFIG_PARAMS));
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TOTAL_MESSAGES, new Object[]{
                senderParams.getTotalMessageCount()
        }));
        if (senderParams.IsTransactedSession()) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TRANSACTION_SIZE, new Object[]{
                    senderParams.getTransactionSize()
            }));
        }
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.MESSAGE_SIZE,
                new Object[]{senderParams.getMessageSize()}));

        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INITIALIZED_SENDER));

        StatusEvent statusEvent = new StatusEvent();
        statusEvent.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
        statusEvent.setStatus(StatusEvent.Status.COMPONENT_STARTED);
        statusEvent.setStatusType(StatusEvent.StatusType.INFORMATION);
        ccpEventManager.getCCPEventGenerator().sendEvent(statusEvent);
        //store the message from file into 'message'
        content = setMessage();

        for (int i = 0; i < senderParams.getProducers(); i++) {
            producerThread[i].start();
        }

        for (int i = 0; i < senderParams.getProducers(); i++) {
            try {
                producerThread[i].join();
            } catch (InterruptedException ex) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_IN_JOIN,
                        new Object[]{ex.getLocalizedMessage()}));
            }
        }
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.SENT_ALL_MESSAGES,
                new Object[]{senderParams.getTotalMessageCount()}));
        float averageRate = 0;
        for (int i = 0; i < senderParams.getProducers(); i++) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PUBLISH_RATE, new Object[]{
                    i,
                    producerThread[i].getRate()
            }));
            averageRate = averageRate + producerThread[i].getRate();
        }

        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.AVG_PUBLISH_RATE,
                new Object[]{averageRate / (senderParams.getProducers())}));

       if(running)
        close();
    }

    private void lookupOutputPorts(IJNDILookupHelper lookupHelper) throws NamingException {
        Collection outputPorts = lookupHelper.lookupOutputPorts();
        outPorts = new Hashtable(outputPorts.size());
        for (Object outputPort : outputPorts) {
            OutputPortInstanceAdapter o = (OutputPortInstanceAdapter) outputPort;
            outPorts.put(o.getName(), o);
        }
    }

    public Connection getConnection() {
        return connection[0];
    }

    public Logger getLogger() {
        return logger;
    }


    public SenderCommandLineParams getSenderParams() {
        return senderParams;
    }

    public String getServiceLookupName() {
        return senderParams.getApplicationName() + "__"
                + String.valueOf(senderParams.getApplicationVersion()).replace(".", "_") + "__"
                + senderParams.getServiceInstanceName();
    }


    protected void createCCPObjects() {

        ccpEventManager = new CCPEventManager(this);
    }


    /**
     * close JMS transport
     */
    public void close() {
        running = false;
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
        for (int i = 0; i < senderParams.getConnections(); i++) {
            if (connection[i] != null) {
                try {
                    connection[i].close();
                } catch (JMSException e) {
                    logger.log(Level.WARNING, ExceptionUtil.getStackTrace(e));
                }
            }
        }
        if (senderParams != null) {
            if (!senderParams.isInmemoryLaunchable()) {
                System.exit(-1);
            }
        }
    }

    /**
     * Overridden method from ExceptionListener.
     *
     * @param jmsException JMSException
     */
    public void onException(JMSException jmsException) {
        close();
    }

    /**
     * @param args commadline arguments
     */
    public static void main(String args[]) {
        SenderService sender = new SenderService();

        sender.startup(args);
    }

    protected LogManager logManager;

    public void clearOutLogs() {
        logManager.clearOutLogs(senderParams.getApplicationName() + "__"
                + String.valueOf(senderParams.getApplicationVersion()) + "__"
                + senderParams.getServiceInstanceName(), getLogger());
    }

    public void clearErrLogs() {
        logManager.clearErrLogs(senderParams.getApplicationName() + "__"
                + String.valueOf(senderParams.getApplicationVersion()) + "__"
                + senderParams.getServiceInstanceName(), getLogger());
    }


    private Hashtable<Object, Object> createInitialContextEnv() throws NamingException {
        //  JNDIConfiguration jndiConfiguration = configuration.getJndiConfiguration();
        Hashtable<Object, Object> env = new Hashtable<Object, Object>();
        if (!StringUtil.isEmpty(senderParams.getUsername())) {
            env.put(Context.SECURITY_PRINCIPAL, senderParams.getUsername());
        }
        if (!StringUtil.isEmpty(senderParams.getPassword())) {
            env.put(Context.SECURITY_CREDENTIALS, senderParams.getPassword());
        }
        env.put(Context.PROVIDER_URL, senderParams.getURL());
        if(senderParams.getInitialContextFactory() == null){
            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        }else{
            env.put(Context.INITIAL_CONTEXT_FACTORY, senderParams.getInitialContextFactory());
        }
        if (senderParams.getAdditionalEnvProperties() != null
                && senderParams.getAdditionalEnvProperties().size() > 0) {
            env.putAll(senderParams.getAdditionalEnvProperties());
        }
        return env;
    }



    /**
     * Starts the sender when the it is configured to launch in inMemory
     *
     * @param args commanline arguments
     */
    public void startup(String[] args) {
        try {
            senderParams = new SenderCommandLineParams(args);
            logManager = com.fiorano.microservice.common.log.LoggerUtil.createLogHandlers(senderParams);
            logger = LoggerUtil.getServiceLogger("COM.FIORANO.EDBC.SENDER", getServiceLookupName(),
                    senderParams.getServiceGUID());

            if (!senderParams.isInmemoryLaunchable()) {
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
            connection = new Connection[senderParams.getConnections()];
            session = new Session[senderParams.getSessions()];
            producer = new MessageProducer[senderParams.getProducers()];
            producerThread = new ProducerThread[senderParams.getProducers()];
            if (StringUtil.isEmpty(senderParams.getXMLFilePath()) && senderParams.getMessageSize() < 0)
                throw new Exception(RBUtil.getMessage(Bundle.class, Bundle.MESSAGE_SIZE_CANNOT_BE_NEGATIVE));
            create();


        } catch (Exception e) {
            if (logger != null && logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_SERVICE), e);
            }
            shutdown(e);
        }
    }

    /**
     * Stops the component when it is started in inMemory
     *
     * @param hint currently this parameter is unused
     */
    public void shutdown(Object hint) {
        close();
    }

    /**
     * reads the message from the file
     * and stores in 'message'
     */

    private String setMessage() {
        StringBuffer strOut = new StringBuffer();
        if (senderParams.getXMLFilePath() != null &&
                senderParams.getXMLFilePath().trim().length() > 0) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INPUT_IS_READ_FROM_FILE, new Object[]{senderParams.getXMLFilePath()}));
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.IF_CLAUSE));
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(senderParams.getXMLFilePath()));
                String strTemp;
                while ((strTemp = br.readLine()) != null) {
                    strOut.append(strTemp);
                }
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.XML_FILE_READ)
                        + senderParams.getXMLFilePath());

            } catch (IOException io) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.XML_PROCESS_ERROR,
                        new Object[]{io.getLocalizedMessage(), io}));

            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.XML_PROCESS_ERROR,
                                new Object[]{e.getLocalizedMessage(), e}));
                    }
                }
            }
        } else {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INPUT_STRING_IS_GENERATED));
            for (int i = 0; i < senderParams.getMessageSize(); i++)
                strOut.append("A");
        }
        return strOut.toString();
    }

    public void stop() {
        shutdown(this);
    }


    /**
     * This class is used to send messages
     *
     * @author FSIPL
     * @version 1.0
     * @created May 24, 2005
     */


    class ProducerThread extends Thread {
        private MessageProducer messageProducer;
        private Session session;
        private int messageCount;
        private float messageRate = 0;


        /**
         * Constructor
         *
         * @param producer message producer
         * @param session  session object
         */
        public ProducerThread(MessageProducer producer, Session session) {
            messageProducer = producer;
            this.session = session;
        }

        /**
         * Returns the publish rate of messages
         *
         * @return messageRate publish rate
         */
        public float getRate() {
            return messageRate;
        }

        /**
         * Main processing method for the ProducerThread object
         */
        public void run() {
            try {
                sendMessages();
            } catch (JMSException ex) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_ERROR,
                        new Object[]{ex.getLocalizedMessage(), ex}));
            }
        }

        /**
         * Sends the messages which is equal to the total message count specified in params
         *
         * @throws JMSException     if there's an exception in sending the message
         * @throws FioranoException if there's an exception in setting text message for FioranoXMLMessage.
         */

        private void sendMessages() throws JMSException {
            TextMessage message;
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < senderParams.getTotalMessageCount(); i++) {
                if (!running) {
                    break;
                }
                message = session.createTextMessage(content);
                sendMessage(message);
            }

            long totalTime = System.currentTimeMillis() - startTime;
            float dataSent = (senderParams.getTotalMessageCount());
            messageRate = (dataSent * 1000) / totalTime;
        }

        /**
         * Sends the message and increments the message count. Commits the session if the message count
         * equals the transaction size for a transacted session.
         *
         * @param message JMS Message
         * @throws JMSException if there's an exception in sending the message
         */
        private void sendMessage(Message message)
                throws JMSException {
            messageCount++;


            if (logger.isLoggable(Level.FINEST)) {

            }

            messageProducer.send(message);

            if (senderParams.IsTransactedSession() &&
                    ((messageCount % senderParams.getTransactionSize()) == 0)) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.COMMITTING_SESSION));
                session.commit();
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.SENT_MESSAGES,
                        new Object[]{messageCount}));
            }
        }
    }


    public int waitFor() throws InterruptedException {
        return 0;
    }

    public int exitValue() {
        return 0;
    }

}
