/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.jms;

import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.fw.error.FrameWorkException;
import com.fiorano.xml.sax.XMLCreator;
import fiorano.esb.util.MessageUtil;
import org.xml.sax.InputSource;

import javax.jms.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This class can be used to raiseEvent and send errorMessage.
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created August 24, 2005
 */
public class EventGenerator extends fiorano.esb.util.EventGenerator {
    /**
     * @param jmsProducer
     * @param jmsErrorDestination
     * @param jmsEventDestination
     */
    public EventGenerator(MessageProducer jmsProducer, Destination jmsErrorDestination, Destination jmsEventDestination) {
        super(jmsProducer, jmsErrorDestination, jmsEventDestination);
    }

    /**
     */
    public EventGenerator() {
        this(null, null, null);
    }

    /**
     * @param jmsProducer
     */
    public EventGenerator(MessageProducer jmsProducer) {
        this(jmsProducer, null, null);
    }

    /**
     * @param strErrorCode
     * @param strErrorMessage
     * @param th
     * @param inputMessage
     * @param errorMessage
     * @throws FrameWorkException
     */
    public void sendError(String strErrorCode, String strErrorMessage, Throwable th, Message inputMessage, Message errorMessage)
            throws FrameWorkException {

        String strMessage = null;

        try {

            StringWriter writer = new StringWriter();
            XMLCreator xmlCreator = new XMLCreator(new StreamResult(writer), true, false);

            String targetNameSpace = getFaultTargetNameSpace();

            xmlCreator.setOuputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                    "errorDetail inputMessage");

            xmlCreator.startDocument();
            xmlCreator.startElement(targetNameSpace, ERROR_XML_ERROR);

            xmlCreator.addElement(ERROR_XML_ERRORCODE, strErrorCode);
            xmlCreator.addElement(ERROR_XML_ERRORMESSAGE, strErrorMessage);
            xmlCreator.startElement(ERROR_XML_ERRORDETAIL);

            if (th instanceof ServiceExecutionException) {
                ServiceExecutionException serviceExecutionException = (ServiceExecutionException) th;
                if (serviceExecutionException.errorDetailAsCData()) {
                    xmlCreator.addCDATA(serviceExecutionException.getErrorDetail());
                } else {
                    InputSource source = new InputSource(new StringReader(serviceExecutionException.getErrorDetail()));
                    xmlCreator.addXML(source, false);
                }
            }

            xmlCreator.endElement();

            String strInputMessage = getData(inputMessage);

            if (strInputMessage != null) {
                xmlCreator.startElement(ERROR_XML_DATA);
                xmlCreator.startElement("inputMessage");
                xmlCreator.addCDATA(strInputMessage);
                xmlCreator.endElement();
                xmlCreator.endElement();
            }

            xmlCreator.endElement();
            xmlCreator.endDocument();

            strMessage = writer.getBuffer().toString();

            writer.close();

        } catch (Exception ex) {
            if (ex instanceof JMSException)
                throw new FrameWorkException(Bundle.class, Bundle.ERROR_FETCHING_DATA, ex);
            else
                throw new FrameWorkException(Bundle.class, Bundle.ERROR_GENERATING_ERROR_XML, ex);
        }

        try {

            MessageUtil.cloneMessage(inputMessage, errorMessage);
            ((TextMessage) errorMessage).setText(strMessage);
            sendMessage(errorMessage, BOOL_ERROR);

        } catch (Exception ex1) {

            if (ex1 instanceof FrameWorkException)
                throw (FrameWorkException) ex1;
            else {

                if (ex1 instanceof JMSException)
                    throw new FrameWorkException(Bundle.class, Bundle.ERROR_SETTING_MESSAGE, strMessage, ex1);
                else
                    throw new FrameWorkException(Bundle.class, Bundle.ERROR_SENDING_MESSAGE, strMessage, ex1);
            }

        }

    }
}
