/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.engine.AbstractRequestProcessor;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.xslt.configuration.XsltPM;
import com.fiorano.util.StringUtil;
import com.fiorano.util.Util;
import com.fiorano.xml.sax.SAXUtil;
import com.fiorano.xml.transform.TransformerUtil;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CarryForwardContext;
import fiorano.esb.util.MessagePropertyNames;
import fiorano.esb.util.MessageUtil;
import net.sf.saxon.TransformerFactoryImpl;
import org.xml.sax.InputSource;
import sun.misc.BASE64Encoder;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayWriter;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processes the input request.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class RequestProcessor extends AbstractRequestProcessor {
    private Templates templates = null;
    private Transformer xslTransformer = null;
    private Transformer jmsTransformer = null;
    private Templates JMSMessagetemplates = null;
    private XsltPM xsltConfiguration;


    /**
     * creates an instance of Request Processor.
     *
     * @param schema               If there's any schema on the input port to be validated.
     * @param logger               logger used for logging.
     * @param serviceConfiguration configuration object.
     */
    public RequestProcessor(ESBRecordDefinition schema, Logger logger,
                            IServiceConfiguration serviceConfiguration) {
        super(schema, logger, serviceConfiguration);
        xsltConfiguration = (XsltPM) serviceConfiguration;
        try {
            initialize();
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.FAILED_INITIALIZE), e);
        }

    }

    private void initialize() throws ServiceExecutionException {
        logger.log(Level.FINE, "Intializing");
        try {
            TransformerFactory factory;

            factory = TransformerUtil.createFactory(xsltConfiguration.getTfClassToUse());
            Boolean bIsStripWhiteSpaces = xsltConfiguration.getStripWhiteSpacesBool();

            if (bIsStripWhiteSpaces != null && factory instanceof TransformerFactoryImpl) {
                ((TransformerFactoryImpl) factory).getConfiguration().setStripsAllWhiteSpace(bIsStripWhiteSpaces.booleanValue());
            }
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.USING_TRANS_FACT, new Object[]{factory}));
            factory.setErrorListener(createErrorListener());
            if (!StringUtil.isEmpty(xsltConfiguration.getXSL())) {
                templates = factory.newTemplates(new SAXSource(new InputSource(new
                        StringReader(xsltConfiguration.getXSL()))));
                if (!XsltPM.SAXON.equals(xsltConfiguration.getXsltEngine())) {
                    xslTransformer = templates.newTransformer();
                    xslTransformer.setErrorListener(createErrorListener());
                }
            }
            if (!StringUtil.isEmpty(xsltConfiguration.getUserJMSMessageXSL())) {
                JMSMessagetemplates = factory.newTemplates(new SAXSource(new InputSource(new
                        StringReader(xsltConfiguration.getUserJMSMessageXSL()))));
                if (!XsltPM.SAXON.equals(xsltConfiguration.getXsltEngine())) {
                    jmsTransformer = JMSMessagetemplates.newTransformer();
                    jmsTransformer.setOutputProperty("method", "xml");
                    jmsTransformer.setErrorListener(createErrorListener());
                }
            }

            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.TEMPLATES_INIT_SUCCESS));

        } catch (TransformerConfigurationException tce) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.TEMPLATES_INIT_FAILED, new Object[]{tce.getLocalizedMessage()}),
                    tce, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }
    }

    private ErrorListener createErrorListener() {
        return new ErrorListener() {
            public void warning(TransformerException tex) throws TransformerException {
                logger.log(Level.WARNING, tex.getMessage());
                if (xsltConfiguration.getFailOnErrors()) {
                    throw tex;
                }
            }

            public void error(TransformerException tex) throws TransformerException {
                if (xsltConfiguration.getFailOnErrors()) {
                    throw tex;
                } else {
                    logger.log(Level.WARNING, tex.getMessage(), tex);
                }
            }

            public void fatalError(TransformerException tex) throws TransformerException {
                logger.log(Level.SEVERE, tex.getMessage(), tex);
                throw tex;

            }
        };
    }

    /**
     * Processes the input request. By default it returns the input message.
     *
     * @param request request string
     * @return response message.
     * @throws ServiceExecutionException if there is any exception in processing the request
     */
    public String process(String request) throws ServiceExecutionException {
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.REQUEST_PROCESSED, new Object[]{request}));
        return request;
    }

    /**
     * Processes the input request. By default it returns the input object.
     *
     * @param request request object
     * @return response object.
     * @throws ServiceExecutionException if there is any exception in processing the request.
     */
    public Object process(Object request) throws ServiceExecutionException {
        logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.REQUEST_PROCESSED, new Object[]{request}));
        return request;
    }

    public Message process(Message request) throws ServiceExecutionException {
        try {
            request = super.process(request);
            validate(MessageUtil.getTextData(request));
        } catch (JMSException e) {
            throw new ServiceExecutionException(e, ServiceErrorID.INVALID_REQUEST_ERROR);
        }

        logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.PROCESSING_MESSAGE));
        if (templates == null || (!StringUtil.isEmpty(xsltConfiguration.getUserJMSMessageXSL()) && JMSMessagetemplates == null)) {
            initialize();
        }
        decryptMessage(request, xsltConfiguration.getElementsToDecrypt());
        invokeTransformer(request);
        encryptMessage(request, xsltConfiguration.getElementsToEncrypt());
        return request;
    }

    private void invokeTransformer(Message request) throws ServiceExecutionException {
        try {
            if (XsltPM.SAXON.equals(xsltConfiguration.getXsltEngine()) && !StringUtil.isEmpty(xsltConfiguration.getXSL())) {
                xslTransformer = templates.newTransformer();
                xslTransformer.setErrorListener(createErrorListener());
            }
            if (XsltPM.SAXON.equals(xsltConfiguration.getXsltEngine()) && !StringUtil.isEmpty(xsltConfiguration.getJMSMessageXSL())) {
                jmsTransformer = JMSMessagetemplates.newTransformer();
                jmsTransformer.setOutputProperty("method", "xml");
                jmsTransformer.setErrorListener(createErrorListener());
            }
            String result = xslTransformer == null ? null : invokeTransformer(request, xslTransformer);
            String jmsResult = jmsTransformer != null
                    ? invokeTransformer(request, jmsTransformer)
                    : null;

            if (result != null) {
                if (xsltConfiguration.getOutputStructure().equals(XsltPM.BODY)) {
                    MessageUtil.setTextData(request, result);
                } else {
                    setApplicationContext(request, result);
                }
            }

            if (jmsResult != null) {
                InputSource is = new InputSource(new StringReader(jmsResult));

                SAXUtil.createSAXParser(false, false, false).parse(is, new JMSMessageHandler(request, logger));
            }
        } catch (Exception ex) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.TRANSFORM_FAILED);
            logger.log(Level.SEVERE, message, ex);
            throw new ServiceExecutionException(message, ex, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        } finally {
            if (xslTransformer != null) {
                xslTransformer.clearParameters();
            }
            if (jmsTransformer != null) {
                jmsTransformer.clearParameters();
            }
        }
    }

    private String invokeTransformer(Message inputMessage, Transformer transformer)
            throws ServiceExecutionException {

        try {
            //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            String content = MessageUtil.getTextData(inputMessage);
            if (StringUtil.isEmpty(content)) {
                content = "<TIFOSI>tifosi</TIFOSI>";
            }

            //fetch Application context
            String context = getApplicationContext(inputMessage, true);

            // If it is Body or Body Context, mark Body to be passed for the transformation
            boolean useBody = xsltConfiguration.getInputStructures().startsWith(XsltPM.BODY);
            String inputXML = useBody
                    ? content : context;

//            if (xsltConfiguration.getInputStructures().indexOf("-") != -1)
            // has second structure
//            {
            transformer.setURIResolver(new XSLURIResolver(content));
//            }

            // optimization
            content = context = null;
            if (xsltConfiguration.getDoOptimization()) {
                if (JMSMessagetemplates == null || (JMSMessagetemplates == templates)) {
                    if (xsltConfiguration.getOutputStructure().equals(XsltPM.BODY)) {
                        MessageUtil.setTextData(inputMessage, null);
                    } else {
                        setApplicationContext(inputMessage, null);
                    }
                }
            }

            pluginParameters(inputMessage, transformer);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.PARAMETERS_LOAD_SUCCESFUL));
            }

            CharArrayWriter result = new CharArrayWriter();
            SAXSource source = new SAXSource(new InputSource(new StringReader(inputXML)));
            transformer.transform(source, new StreamResult(result));
            return result.toString();
        } catch (Exception ex) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.TRANSFORM_FAILED);
            logger.log(Level.SEVERE, message, ex);
            throw new ServiceExecutionException(message, ex, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            //error  in Transform
        }
    }


    private void pluginParameters(Message inputMessage, Transformer transformer)
            throws ServiceExecutionException {

        try {
            Set<Map.Entry> properties = MessageUtil.getAllProperties(inputMessage).entrySet();

            for (Map.Entry property : properties) {
                Object name = property.getKey();
                Object value = property.getValue();

                if (value != null) {
                    transformer.setParameter("_TIF_HEADER_" + name, value);
                }
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.PARAMETER_LOAD_SUCCESFUL, new Object[]{"header: " + name}));
                }
            }

            String text = MessageUtil.getTextData(inputMessage);
            if (text != null) {
                transformer.setParameter("_TIF_BODY_TEXT_", text);
            }

            {
                byte[] bytesData = MessageUtil.getBytesData(inputMessage);
                if (bytesData != null) {
                    transformer.setParameter("_TIF_BODY_BYTE_", (new BASE64Encoder()).encodeBuffer(bytesData));
                }
            }

            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.PARAMETER_LOAD_SUCCESFUL, new Object[]{"Content"}));
            }

            Map<String, byte[]> attachments = MessageUtil.getAttachments(inputMessage);

            if (attachments != null) {
                for (Map.Entry<String, byte[]> attachment : attachments.entrySet()) {
                    String name = attachment.getKey();
                    byte[] bytesData = attachment.getValue();
                    if (bytesData != null) {
                        transformer.setParameter("_TIF_ATTACH_" + name, (new BASE64Encoder()).encodeBuffer(bytesData));
                    }
                }
            }
            String context = getApplicationContext(inputMessage, false);
            if (context != null) {
                transformer.setParameter("_TIF_APP_CONTEXT_", context);
            }

            transformer.setParameter("_TIF_MESSAGE_", inputMessage);

            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, RBUtil.getMessage(Bundle.class, Bundle.PARAMETER_LOAD_SUCCESFUL, new Object[]{"Message"}));
            }

        } catch (JMSException ex) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.PARAMETERS_LOAD_FAILED);
            logger.log(Level.SEVERE, message, ex);
            throw new ServiceExecutionException(message, ex, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }

    }

    private void setApplicationContext(Message message, String context) throws JMSException {
        CarryForwardContext cfc = (CarryForwardContext) message.getObjectProperty(MessagePropertyNames.CARRY_FORWARD_CONTEXT);
        if (cfc == null && context == null) {
            return;
        } else if (cfc == null) {
            message.setObjectProperty(MessagePropertyNames.CARRY_FORWARD_CONTEXT, cfc = new CarryForwardContext());
        }
        cfc.setAppContext(context);
        message.setObjectProperty(MessagePropertyNames.CARRY_FORWARD_CONTEXT, cfc);
    }

    private String getApplicationContext(Message message) throws JMSException {
        CarryForwardContext cfc = (CarryForwardContext) message.getObjectProperty(MessagePropertyNames.CARRY_FORWARD_CONTEXT);
        return cfc != null ? cfc.getAppContext() : null;
    }

    private String getApplicationContext(Message message, boolean getDefault) throws ServiceExecutionException {
        String context = null;
        try {
            context = getApplicationContext(message);
        } catch (JMSException ex) {
            if (isContextRequired()) {
                String error = RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_FETCH_CONTEXT);
                logger.log(Level.SEVERE, error, ex);
                throw new ServiceExecutionException(error, ex, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            } else {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_FETCH_CONTEXT), ex);
            }
        }
        if (getDefault) {
            if (context == null || context.length() == 0) {
                // Don't Log if the context is not required.
                if (isContextRequired()) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EMPTY_CONTEXT));
                    }
                }
                context = "<Context></Context>";
            }
        }
        return context;
    }

    private boolean isContextRequired() {
        return xsltConfiguration.getInputStructures().contains(XsltPM.CONTEXT)
                || xsltConfiguration.getOutputStructure().equals(XsltPM.CONTEXT);
    }


    /**
     * <p><strong> </strong> represents </p>
     *
     * @author FSIPL
     * @version 1.0
     * @created July 19, 2005
     */
    class XSLURIResolver implements URIResolver {
        String input = null;

        /**
         * Constructor for the XSLURIResolver object
         *
         * @param input Description of the Parameter
         */
        public XSLURIResolver(String input) {
            this.input = input;
        }

        /**
         * Description of the Method
         *
         * @param href Description of the Parameter
         * @param base Description of the Parameter
         * @return Description of the Return Value
         * @throws TransformerException Description of the Exception
         */
        public Source resolve(String href, String base)
                throws TransformerException {
            if (XsltPM.CONTEXT.equals(xsltConfiguration.getInputStructures()) || XsltPM.BODY.equals(xsltConfiguration.getInputStructures())) {
                return null;
            }
            if (xsltConfiguration.getInputStructureName() == null || Util.equals(xsltConfiguration.getInputStructureName(), href))
                return new SAXSource(new InputSource(new StringReader(input)));
            else  // Return null, if the href is other than IN_PORT. TransformerImpl class will resolve the
                //  href's if those are filepaths/files, if you return null here. 
                return null;
        }
    }

}
