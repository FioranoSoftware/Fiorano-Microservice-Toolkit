/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat;

import com.fiorano.bc.chat.jms.CCPEventManager;
import com.fiorano.bc.chat.model.ChatPM;
import com.fiorano.bc.chat.model.ChatPropertyModel;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.microservice.common.log.LogManager;
import com.fiorano.openesb.microservice.ccp.event.common.DataRequestEvent;
import com.fiorano.openesb.microservice.ccp.event.common.data.Data;
import com.fiorano.openesb.microservice.ccp.event.common.data.MicroserviceConfiguration;
import com.fiorano.openesb.microservice.ccp.event.component.StatusEvent;
import com.fiorano.services.common.service.ServiceLifeCycle;
import com.fiorano.util.JavaUtil;
import com.fiorano.util.StringUtil;
import fiorano.esb.util.ESBConstants;
import fiorano.esb.util.InMemoryLaunchable;
import fiorano.esb.util.LoggerUtil;
import fiorano.esb.utils.BeanUtils;
import fiorano.esb.utils.RBUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the Chat component
 *
 * @author FSIPL
 * @version 1.0
 * @created April 18, 2005
 */
public class ChatService implements ExceptionListener, MessageListener, InMemoryLaunchable, ServiceLifeCycle {

    public final static String DEFAULT_CONSUMER_DESTINATION_SUFFIX = "IN_PORT";
    public final static String DEFAULT_PRODUCER_DESTINATION_SUFFIX = "OUT_PORT";

    private Logger logger;
    private OutputPortHandler[] outputPortHandlers;

    public Logger getLogger() {
        return logger;
    }

    //  PropertyModel for Chat. ChatPM represents
    //  the model for storing all configurable properties
    //  for Chat (like username, font size, color etc.)
    ChatPM m_model;

    private String consumerDestination;
    private String producerDestination;

    // Command line params
    private CommandLineParameters cmdLineParameters;

    // JMS Session
    private Connection connection;
    private Session session;

    private MessageProducer producer;

    //  Chat Frame for displaying sent/received messages
    private ChatFrame frame;

    private ConnectionFactory cf;

    private Map<DataRequestEvent.DataIdentifier, Data> ccpConfiguration;
    private CCPEventManager ccpEventManager;
    private InitialContext context;
    /**
     * Convenience method to walk only through elements with the specified
     * tag name.  This just calls getNext() and filters out the nodes which
     * aren't desired.  It returns null when the iteration completes.
     *
     * @param tag       the tag to match, or null to indicate all elements
     * @param walker    tree to search in
     * @param startNode the node in the tree to start at
     * @return the next matching element, or else null
     */
    public Element getNextElement(String tag, TreeWalker walker, Node startNode) {
        for (Node next = walker.nextNode(); next != null; next = walker.nextNode()) {
            if (next.getNodeType() == Node.ELEMENT_NODE
                    && (tag == null || tag.equals(next.getNodeName()))) {
                return (Element) next;
            }
        }
        walker.setCurrentNode(startNode);
        return null;
    }

    /**
     * Gets the node value as string.
     *
     * @param node Description of the Parameter
     * @return The nodeValueAsString value
     * @throws Exception
     */
    public String getNodeValueAsString(Node node) throws Exception {
        if (node != null) {
            node = node.getFirstChild();
            if (node != null) {
                return node.getNodeValue();
            }
        }
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns consumer destination for object
     *
     * @return consumerDestination
     */
    public String getConsumerDestination() {
        if (consumerDestination != null) {
            return consumerDestination;
        }

        return consumerDestination = cmdLineParameters.getConnectionFactory() + ESBConstants.JNDI_CONSTANT + DEFAULT_CONSUMER_DESTINATION_SUFFIX;
    }

    /**
     * Returns producer destination for object
     *
     * @return producerDestination
     */
    public String getProducerDestination() {
        if (producerDestination != null) {
            return producerDestination;
        }

        return producerDestination = cmdLineParameters.getConnectionFactory() + ESBConstants.JNDI_CONSTANT + DEFAULT_PRODUCER_DESTINATION_SUFFIX;
    }

    public ConnectionFactory getCf() {
        return cf;
    }

    public String getServiceLookupName() {
        return cmdLineParameters.getApplicationName() + "__"
                + String.valueOf(cmdLineParameters.getApplicationVersion()).replace(".", "_") + "__"
                + cmdLineParameters.getServiceInstanceName();
    }

    /**
     * @throws NamingException
     * @throws JMSException
     */
    public void createJMSObjects() throws NamingException, JMSException {

        try {
            //noinspection JNDIResourceOpenedButNotSafelyClosed
            context = new InitialContext(createInitialContextEnv());
            logger.log(Level.INFO, RBUtil.getMessage(com.fiorano.bc.chat.Bundle.class,com.fiorano.bc.chat.Bundle.INITIAL_CONTEXT_CREATED));
        } catch (NamingException e) {
            String message = RBUtil.getMessage(com.fiorano.bc.chat.Bundle.class, com.fiorano.bc.chat.Bundle.INITIAL_CONTEXT_CREATION_ERROR,
                    new String[]{e.getMessage()});

            logger.log(Level.SEVERE, message);
            shutdown(null);
        }

        ConnectionFactory cf = (ConnectionFactory) context.lookup(cmdLineParameters.getConnectionFactory());
        connection = cf.createConnection(cmdLineParameters.getUsername(), cmdLineParameters.getPassword());
        connection.setClientID(getServiceLookupName());

        connection.setExceptionListener(this);

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.SESSION_CREATED));
        }

        start();

        try {
            createCCPObjects();
            ccpEventManager.start();
        } catch (Exception e) {
            e.printStackTrace();
            shutdown(null);
        }


        DataRequestEvent dataRequestEvent = new DataRequestEvent();
        dataRequestEvent.getDataIdentifiers().add(DataRequestEvent.DataIdentifier.COMPONENT_CONFIGURATION);
        dataRequestEvent.setReplyNeeded(true);

        ccpEventManager.getCCPEventGenerator().sendEvent(dataRequestEvent);

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MicroserviceConfiguration data = (MicroserviceConfiguration) ccpConfiguration
                .get(DataRequestEvent.DataIdentifier.COMPONENT_CONFIGURATION);

        m_model = getConfiguration(data.getConfiguration());


        Destination sendDest = session.createTopic(cmdLineParameters.getApplicationName() + "__" + String.valueOf(cmdLineParameters.getApplicationVersion()).replace(".", "_") + "__" + cmdLineParameters.getServiceInstanceName() + "__" + "OUT_PORT");
        producer = session.createProducer(sendDest);

        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PRODUCER_CREATED, new Object[]{sendDest}));
        }


        Destination receiveDest = session.createQueue(cmdLineParameters.getApplicationName() + "__" + String.valueOf(cmdLineParameters.getApplicationVersion()).replace(".", "_") + "__" + cmdLineParameters.getServiceInstanceName() + "__" + "IN_PORT");

        MessageConsumer consumer;

        consumer = session.createConsumer(receiveDest, null, false);


        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.CONSUMER_CREATED, new Object[]{receiveDest}));
        }
        consumer.setMessageListener(this);
    }


    protected void createCCPObjects() {

        ccpEventManager = new CCPEventManager(this);
    }

    public void updateConfiguration(Map<DataRequestEvent.DataIdentifier, Data> ccpConfiguration) {
        this.ccpConfiguration = ccpConfiguration;
    }


    private OutputPortHandler getOutputPortHandler(String outputportname) {
        for (OutputPortHandler outputPortHandler : outputPortHandlers) {
            if (outputPortHandler.getName().equals(outputportname)) {
                return outputPortHandler;
            }
        }
        return null;
    }

    /**
     * @throws JMSException
     */
    public void start() throws JMSException {
        if (connection != null) {
            connection.start();
        }
    }

    /**
     * @throws Exception
     */
    public void showFrame() throws Exception {
        final ChatService service = this;
        // Do the GUI related stuff here
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    frame = new ChatFrame(service, cmdLineParameters.getApplicationName() + "__" + String.valueOf(cmdLineParameters.getApplicationVersion()).replace(".", "_") + "__" + cmdLineParameters.getServiceInstanceName());

                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                    frame.setLocation((screenSize.width - frame.getWidth()) /
                                    2,
                            (screenSize.height - frame.getHeight()) /
                                    2);

                    if (cmdLineParameters.isInmemoryLaunchable()) {
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    } else {
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    }
                    frame.show();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_SHOWING_UI), e);
                }
            }
        });
    }

    /**
     */
    public void onClose() {
        try {
            if (frame != null) {
                frame.dispose();
            }
            if (connection != null)
                connection.close();
        } catch (Exception ex) {
            // ignore
        }
        if (cmdLineParameters != null) {
            if (!cmdLineParameters.isInmemoryLaunchable()) {
                System.exit(-1);
            }
        }
    }

    /**
     * Listens for JMS Exception
     */
    public void onException(JMSException e) {
        logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_MSG), e);
        onClose();
    }

    /**
     * Message Listener
     */
    public void onMessage(Message msg) {
        if (!(msg instanceof TextMessage)) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.RCVD_NONTXT_MSG, new Object[]{msg}));
            }

            return;
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RECIEVED_MESSAGE, new Object[]{msg}));
        }

        TextMessage tmsg = (TextMessage) msg;

        String xmlDoc = null;

        try {
            xmlDoc = tmsg.getText();
        } catch (JMSException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_GETTING_TEXT), ex);
            }
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, RBUtil.getMessage(Bundle.class, Bundle.MSG_CONTENT, new Object[]{xmlDoc}));
        }

        String sender = null;
        String displayMsg = null;

        try {
            if (xmlDoc != null) {
                //Input is an XML.
                //Try to get the sender and message from expected XML structure
                //Otherwise display the complete text.

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                factory.setNamespaceAware(false);
                factory.setValidating(false);

                DocumentBuilder builder = factory.newDocumentBuilder();

                StringReader reader = new StringReader(xmlDoc);
                InputSource source = new InputSource(reader);

                Document document = builder.parse(source);
                TreeWalker walker = ((DocumentTraversal) document).createTreeWalker(document, NodeFilter.SHOW_ALL, null,
                        true);

                Element appHeaderElement = getNextElement("Name", walker, document);

                if (appHeaderElement != null) {
                    //Set the name from input XML
                    String name = getNodeValueAsString(appHeaderElement);

                    if (name != null && !"".equals(name)) {
                        sender = name;
                    }
                }
                walker.setCurrentNode(document);

                appHeaderElement = getNextElement("Message", walker, document);

                //Get the message to be displayed from 'Message' tag
                displayMsg = getNodeValueAsString(appHeaderElement);
            }
        } catch (Exception exp) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_PARSING_XML), exp);
            }
        }

        //  Display the text in Chat frame

        if (sender == null) {
            sender = "";
        }

        if (displayMsg == null) {
            displayMsg = "";
        }

        if (frame != null) {
            frame.showText(sender + " ://> " + displayMsg, true);
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, RBUtil.getMessage(Bundle.class, Bundle.RECIEVED_MESSAGE, new Object[]{displayMsg}));
        }
    }

    /**
     * Send specified message on sendDestination
     */
    void sendMessage(String message) {

        try {
            String xmlMessage = _createXMLMessage(message);

            TextMessage tMsg = session.createTextMessage(xmlMessage);

            if (producer != null) {


                if (logger.isLoggable(Level.FINEST)) {

                }
                producer.send(tMsg);
            }

            frame.showText(m_model.getDisplayName() + " ://> " + message, false);

            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.SENT_MESSAGE, new Object[]{message}));
            }
        } catch (Throwable te) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.SEND_FAILED,
                        new Object[]{message, te.getMessage()}));
            }
        }
    }

    /**
     * Load Configuration object
     */
    private ChatPM getConfiguration(String configXML) {
//        String configXML;
        ChatPM configuration;


        try {
            Object obj = BeanUtils.deserialiseBean(configXML);
            if (obj instanceof ChatPropertyModel) {
                ChatPropertyModel config = (ChatPropertyModel) obj;
                configuration = new ChatPM();
                configuration.setDisplayName(config.getDisplayName());
                configuration.setEmailAddress(config.getEmailAddress());
                configuration.setInFontColor(config.getInFontColor());
                configuration.setOutFontColor(config.getOutFontColor());
                configuration.setInFontSize(config.getInFontSize());
                configuration.setOutFontSize(config.getOutFontSize());
                configuration.setInFontName(config.getInFontName());
                configuration.setOutFontName(config.getOutFontName());
                configuration.setInFontStyle(config.getInFontStyle());
                configuration.setOutFontStyle(config.getOutFontStyle());
            } else {
                configuration = (ChatPM) obj;
            }
        } catch (Throwable ex1) {
            configuration = new ChatPM();
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.FETCHED_CONFIG, new Object[]{configXML}));
        }

        return configuration;
    }

    /**
     * Lookup object from FMQ JNDI
     */


    private String _createXMLMessage(String message) throws Exception {
        //  Construct the Document XML using received
        //  Text and configured displatName/Email addresses
        //  (obtained from ChatPM.)
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document = dbf.newDocumentBuilder().newDocument();

        //  Start off the ChatMessage Node.
        Node root0 = (Node) document.createElement("ChatMessage");

        document.appendChild(root0);

        //Start off the Sender Node
        Node child1 = (Node) document.createElement("Sender");

        root0.appendChild(child1);

        //Name Node
        Node child2 = (Node) document.createElement("Name");

        child1.appendChild(child2);
        child2.appendChild(document.createTextNode(m_model.getDisplayName()));

        //Email Node
        Node child3 = (Node) document.createElement("Email");

        child1.appendChild(child3);
        child3.appendChild(document.createTextNode(m_model.getEmailAddress()));

        //Message Node
        Node child4 = (Node) document.createElement("Message");

        root0.appendChild(child4);
        child4.appendChild(document.createTextNode(message));

        return serializeDocument(root0);
    }

    private String serializeDocument(Node node) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        transformer.transform(new DOMSource(node), result);

        writer.flush();
        return writer.toString();
    }

    public void stop() {

        shutdown(this);
    }

    /**
     * SAX handler.
     *
     * @author FSIPL
     * @version 1.0
     * @created March 22, 2005
     */
    private static class CustomHandler extends DefaultHandler {
        /**
         * @param namespaceURI
         * @param localName
         * @param qName
         * @param atts
         * @throws SAXException
         */
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
                throws SAXException {
            if (atts.getIndex("", "targetNamespace") != -1) {
                throw new SAXException(atts.getValue(atts.getIndex("", "targetNamespace")));
            }
        }
    }

    protected LogManager logManager;

    public void clearOutLogs() {
        logManager.clearOutLogs(cmdLineParameters.getApplicationName() + "__"
                + String.valueOf(cmdLineParameters.getApplicationVersion()) + "__"
                + cmdLineParameters.getServiceInstanceName(), getLogger());
    }

    public void clearErrLogs() {
        logManager.clearErrLogs(cmdLineParameters.getApplicationName() + "__"
                + String.valueOf(cmdLineParameters.getApplicationVersion()) + "__"
                + cmdLineParameters.getServiceInstanceName(), getLogger());
    }

    public CommandLineParameters getCmdLineParameters() {
        return cmdLineParameters;
    }

    public void startup(String[] args) {
        try {
            // load command line params
            cmdLineParameters = new CommandLineParameters(args);
            logManager = com.fiorano.microservice.common.log.LoggerUtil.createLogHandlers(cmdLineParameters);
            logger = LoggerUtil.getServiceLogger("COM.FIORANO.EDBC.CHAT", cmdLineParameters.getConnectionFactory(),
                    cmdLineParameters.getServiceGUID());
            LoggerUtil.addFioranoConsoleHandler(logger);
            if (!cmdLineParameters.isInmemoryLaunchable()) {
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
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.RT_ARGS, new Object[]{String.valueOf(i), args[i]}));
                }
            }

            createJMSObjects();
            showFrame();

            StatusEvent statusEvent = new StatusEvent();
            statusEvent.setOperationScope(StatusEvent.OperationScope.COMPONENT_LAUNCH);
            statusEvent.setStatus(StatusEvent.Status.COMPONENT_STARTED);
            statusEvent.setStatusType(StatusEvent.StatusType.INFORMATION);
            ccpEventManager.getCCPEventGenerator().sendEvent(statusEvent);

        } catch (Exception e) {
            if (logger != null && logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_SERVICE), e);
            }
            shutdown(e);
        }
    }

    public static void main(String args[]) {
        ChatService chat = new ChatService();
        chat.startup(args);
    }

    public void shutdown(Object hint) {
        onClose();
    }

    public int waitFor() throws InterruptedException {
        return 0;
    }

    public int exitValue() {
        return 0;
    }


    private Hashtable<Object, Object> createInitialContextEnv() throws NamingException {
        //  JNDIConfiguration jndiConfiguration = configuration.getJndiConfiguration();
        Hashtable<Object, Object> env = new Hashtable<Object, Object>();
        if (!StringUtil.isEmpty(cmdLineParameters.getUsername())) {
            env.put(Context.SECURITY_PRINCIPAL, cmdLineParameters.getUsername());
        }
        if (!StringUtil.isEmpty(cmdLineParameters.getPassword())) {
            env.put(Context.SECURITY_CREDENTIALS, cmdLineParameters.getPassword());
        }
        env.put(Context.PROVIDER_URL, cmdLineParameters.getURL());
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        if (cmdLineParameters.getAdditionalEnvProperties() != null
                && cmdLineParameters.getAdditionalEnvProperties().size() > 0) {
            env.putAll(cmdLineParameters.getAdditionalEnvProperties());
        }
        return env;
    }

    private boolean isConnectionError(NamingException e) {
        return false; //todo
    }

}