/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xmlverification.engine;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.ServiceUtil;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.internal.engine.IRequestValidator;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.ClarkName;
import com.fiorano.xml.sax.XMLRootElementFinder;
import com.fiorano.xmlverification.model.Bundle;
import com.fiorano.xmlverification.model.XmlVerificationPM;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CarryForwardContext;
import fiorano.esb.util.MessageUtil;
import fiorano.esb.utils.XSDValidator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;

public class XmlVerificationRequestProcessor extends Module implements IRequestProcessor<Message, Message> {
    private XmlVerificationPM model;

    public XmlVerificationRequestProcessor(IModule parent, XmlVerificationPM xmlVerificationPM) {
        super(parent);
        model = xmlVerificationPM;
        logger = ServiceUtil.getLogger(this, getClass().getPackage().getName());
    }

    public Message process(Message message) throws ServiceExecutionException {
        logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.RECEIVED_MESSAGE));
        TextMessage textMessage;
        try {
            textMessage = (TextMessage) message;

            String xmlInput = textMessage.getText();
            CarryForwardContext carryForwardContext = ((CarryForwardContext) MessageUtil.getCarryForwardContext(textMessage));
            String xmlContext = carryForwardContext != null ? carryForwardContext.getAppContext() : "";

            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.INPUT_MESSAGE, new Object[]{xmlInput}));
            if (model.getXSD() != null && StringUtil.isEmpty(xmlInput)) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.EMPTY_INPUT_XML), ServiceErrorID.INVALID_REQUEST_ERROR);
            }
            if (model.getContextXSD() != null && StringUtil.isEmpty(xmlContext)) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.EMPTY_CONTEXT_XML), ServiceErrorID.INVALID_REQUEST_ERROR);
            }
            try {
                if (model.getXSD() == null) {
                    if (model.getContextXSD() != null) {
                        checkXSDValidity(model.getContext(), xmlContext);
                    }
                } else {
                    if (model.getContextXSD() == null) {
                        checkXSDValidity(model.getBody(), xmlInput);
                    } else {
                        checkXSDValidity(model.getBody(), xmlInput);
                        checkXSDValidity(model.getContext(), xmlContext);
                    }
                }

                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.VALIDATED_MESSAGE));
            } catch (Exception e) {
                // raise event if not valid XML
                throw new ServiceExecutionException(e, ServiceErrorID.INVALID_REQUEST_ERROR);
            }
        } catch (JMSException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(com.fiorano.services.xmlverification.Bundle.class,
                    com.fiorano.services.xmlverification.Bundle.EXCEP_PROCESSING), e, ServiceErrorID.REQUEST_EXECUTION_ERROR);

        }
        return textMessage;
    }

    public IRequestValidator<Message> getRequestValidator() {
        return null;
    }

    /**
     * Checks the validity of the input xml with the schema provided at the configuration time.
     *
     * @param esbRec object which contains the context/body
     * @param xml    input xml
     * @throws Exception If xml cannot be validated
     */
    private void checkXSDValidity(ESBRecordDefinition esbRec, String xml) throws Exception {
        String strXsd;
        String rootElementName;
        Map foreignSchemas;
        int defType;

        if (esbRec != null) {
            strXsd = esbRec.getStructure();
            rootElementName = esbRec.getRootElementName() != null
                    ? ClarkName.toClarkName(esbRec.getTargetNamespace(), esbRec.getRootElementName())
                    : null;
            foreignSchemas = getForeignSchemas(esbRec.getImportedStructures());
            defType = esbRec.getDefinitionType();
        } else {
            throw new RuntimeException(RBUtil.getMessage(com.fiorano.services.xmlverification.Bundle.class,
                    com.fiorano.services.xmlverification.Bundle.INVALID_CONFIG));
        }

        logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.VALIDATING_INPUT_XML));

        // validate the message
        logger.log(Level.FINEST, RBUtil.getMessage(Bundle.class, Bundle.VERIFYING_XSD, new Object[]{model.getXSD()}));
        logger.log(Level.FINEST, RBUtil.getMessage(Bundle.class, Bundle.VERIFYING_XML, new Object[]{xml}));

        if (defType != ESBRecordDefinition.DTD) {
            XSDValidator.validateXMLwithSchema(xml, strXsd, foreignSchemas);
            if (rootElementName != null) {
                String rootClarkName = null;
                try {
                    rootClarkName = XMLRootElementFinder.findRootElement(new InputSource(new StringReader(xml)));
                } catch (SAXException e) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_IN_FINDING_ROOT_ELEMENT),
                            e);
                } catch (ParserConfigurationException e) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_IN_FINDING_ROOT_ELEMENT),
                            e);
                } catch (IOException e) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ERROR_IN_FINDING_ROOT_ELEMENT),
                            e);
                }
                if (rootElementName.equals(rootClarkName)) {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INPUT_XML_IS_VALID));
                } else {
                    logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.INVALID_ROOT_ELEMENT, new Object[]{rootElementName}));
                    throw new SAXException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_ROOT_ELEMENT));
                }
            } else {   // if rootName is null , then it is not set. As it is optional->if the xsd is valid valid is true
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.INPUT_XML_IS_VALID));
            }
        } else {
            XSDValidator.validateXMLwithDTD(xml, strXsd, rootElementName);
        }
    }

    /**
     * Returns the Map in key<String>,value<String> format
     *
     * @param importedStructures imported xsd structures
     * @return Map Map containing foreign schemas
     */
    private Map getForeignSchemas(Map importedStructures) {
        Map<Object, Object> foreignSchemas = new HashMap<Object, Object>();
        Iterator iterator;
        if (importedStructures == null) {
            return foreignSchemas;
        }
        Set entries = importedStructures.entrySet();
        if (entries != null && (iterator = entries.iterator()) != null) {
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object name = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof List) {
                    foreignSchemas.put(name, ((List) value).get(0));
                } else if (value instanceof String[]) {
                    foreignSchemas.put(name, ((String[]) value)[0]);
                } else if (value instanceof String) {
                    foreignSchemas.put(name, value);
                }
            }
        }
        return foreignSchemas;
    }
}
