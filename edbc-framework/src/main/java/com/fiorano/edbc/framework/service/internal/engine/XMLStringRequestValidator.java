/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.engine;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.Namespaces;
import com.fiorano.xml.dom.DOMUtil;
import com.fiorano.xml.sax.SAXUtil;
import com.fiorano.xml.xsd.XSDUtil;
import fiorano.esb.record.ESBRecordDefinition;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XMLStringRequestValidator implements IRequestValidator<String> {
    private ESBRecordDefinition schema;
    private SAXParser parser;
    private Logger logger;

    public XMLStringRequestValidator(ESBRecordDefinition schema, Logger logger) {
        this.schema = schema;
        this.logger = logger;
    }

    public void validate(String request) throws ServiceExecutionException {
        if (schema == null || schema.getStructure() == null) {
            logger.log(Level.WARNING, RBUtil.getMessage(com.fiorano.edbc.framework.service.engine.Bundle.class, com.fiorano.edbc.framework.service.engine.Bundle.NO_SCHEMA_TO_VALIDATE));
            return;
        }
        if (request == null) {

            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_REQUEST), ServiceErrorID.INVALID_REQUEST_ERROR);
        }
        if (parser == null) {
            initialize();
        }
        int schemaType = schema.getDefinitionType();
        File tempFile = null;
        try {
            if (ISchema.XSD == schemaType) {
                try {
                    if (StringUtil.isEmpty(schema.getTargetNamespace())) {
                        String fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + System.currentTimeMillis() + ".xsd";
                        BufferedWriter out = null;
                        try {
                            tempFile = new File(fileName);
                            tempFile.createNewFile();
                            out = new BufferedWriter(new FileWriter(tempFile));
                            out.write(schema.getStructure());
                        } catch (IOException e) {
                            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_VALIDATE,
                                    new String[]{e.getMessage()}), e, ServiceErrorID.INVALID_REQUEST_ERROR);
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    // do nothing
                                }
                            }
                        }
                        SAXUtil.enableSchemaValidation(parser, tempFile.toURI().toString(), true);
                    }
                } catch (SAXException e) {
                    throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_VALIDATE,
                            new String[]{e.getMessage()}), e, ServiceErrorID.INVALID_REQUEST_ERROR);
                }
            } else if (ISchema.DTD == schemaType) {
                try {
                    org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(request)));
                    StringWriter writer = new StringWriter();
                    DOMUtil.serialize(doc, writer, true, true);
                    request = writer.toString();
                    request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!DOCTYPE " + schema.getRootElementName() + " [\n" + schema.getStructure() + "]>\n" +
                            request;
                } catch (Exception e) {
                    throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_VALIDATE,
                            new String[]{e.getMessage()}), e, ServiceErrorID.INVALID_REQUEST_ERROR);
                }
            } else {
                String message = RBUtil.getMessage(Bundle.class, Bundle.XML_DEFINITION_TYPE_UNKOWN, new Integer[]{schemaType});
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.UNABLE_TO_VALIDATE,
                        new String[]{message}), ServiceErrorID.INVALID_REQUEST_ERROR);
            }

            try {
                if (schemaType == ISchema.XSD) {
                    parser.getXMLReader().parse(new InputSource(new StringReader(request)));
                } else {
                    parser.parse(new InputSource(new StringReader(request)), new CustomErrorHandler());
                }
            } catch (Exception e) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_REQUEST), e, ServiceErrorID.INVALID_REQUEST_ERROR);
            }
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    private void initialize() throws ServiceExecutionException {
        int schemaType = schema.getDefinitionType();
        if (ISchema.XSD == schemaType) {
            try {
                XSLoader xsLoader = XSDUtil.createXSLoader(schema, null);
                XSModel xsModel = xsLoader.load(new DOMInputImpl(null, null, null, schema.getStructure(), null));
                StringList nsList = xsModel.getNamespaces();
                if (StringUtil.isEmpty(schema.getTargetNamespace())) {
                    parser = SAXUtil.createSAXParser(true, true, true);
                    parser.getXMLReader().setEntityResolver(schema);
                    parser.getXMLReader().setErrorHandler(new CustomErrorHandler());
                } else {
                    Properties schemaNamespaces = new Properties();
                    for (int i = 0; i < nsList.getLength(); i++) {
                        String ns = nsList.item(i);
                        if (ns == null || Namespaces.URI_XSD.equals(ns)) {
                            continue;
                        }
                        if (!ns.equals(schema.getTargetNamespace())) {
                            String[] schemas = schema.getImportedStructures(ns);
                            if (schemas == null || schemas.length == 0) {
                                continue;
                            }
                        }
                        schemaNamespaces.setProperty(ns, ns);
                    }
                    parser = SAXUtil.createSAXParser(true, true, true);
                    parser.getXMLReader().setEntityResolver(schema);
                    SAXUtil.enableSchemaValidation(parser, schemaNamespaces, true);
                    parser.getXMLReader().setErrorHandler(new CustomErrorHandler());
                }
            } catch (Exception e) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.PARSER_CREATION_FAILED_FOR_VALIDATION,
                        new String[]{e.getMessage()}), e, ServiceErrorID.INVALID_REQUEST_ERROR);
            }
        } else if (ISchema.DTD == schemaType) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setValidating(true);
                parser = factory.newSAXParser();
            } catch (Exception e) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.PARSER_CREATION_FAILED_FOR_VALIDATION,
                        new String[]{e.getMessage()}), e, ServiceErrorID.INVALID_REQUEST_ERROR);
            }
        } else {
            String message = RBUtil.getMessage(Bundle.class, Bundle.XML_DEFINITION_TYPE_UNKOWN, new Integer[]{schemaType});
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.PARSER_CREATION_FAILED_FOR_VALIDATION,
                    new String[]{message}), ServiceErrorID.INVALID_REQUEST_ERROR);
        }
    }

    /**
     * <p><strong> </strong> represents </p>
     *
     * @author FSIPL
     * @version 1.0
     * @created March 21, 2005
     */
    private static class CustomErrorHandler extends DefaultHandler {
        /**
         * @param e
         * @throws org.xml.sax.SAXException
         */
        public void warning(SAXParseException e)
                throws SAXException {
            throw e;
        }

        /**
         * @param e
         * @throws SAXException
         */
        public void error(SAXParseException e)
                throws SAXException {
            throw e;
        }

        /**
         * @param e
         * @throws SAXException
         */
        public void fatalError(SAXParseException e)
                throws SAXException {
            throw e;
        }
    }

}
