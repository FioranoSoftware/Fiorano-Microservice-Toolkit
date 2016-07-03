/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.transform.TransformerUtil;
import fiorano.esb.util.CarryForwardContext;
import fiorano.esb.util.MessageUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by IntelliJ IDEA.
 * Date: Mar 25, 2008
 * Time: 1:58:32 PM
 * To change this template use File | Settings | File Templates.
 *
 * @author Venkat
 * @author Abhinay Dubey
 * @version 1.1, 19 January 2008
 */
public class XSLTProcessor implements ICBRProcessor {

    private HashMap<String, String> bodyExpressions = new HashMap<>();
    private HashMap<String, String> contextExpressions = new HashMap<>();
    private HashMap<String, Evaluator> destinations = new HashMap<>();
    private HashMap<String, String> namespaces = new HashMap<>();
    private Logger logger;
    private XSLTConfiguration contextConfiguration;
    private XSLTConfiguration bodyConfiguration;

    public XSLTProcessor(HashMap<String, String> namespaces, Logger logger) {
        this.logger = logger;
        this.namespaces = namespaces;

    }

    public List<Evaluator> evaluate(TextMessage message) throws ServiceExecutionException {
        try {
            List<Evaluator> successList = new ArrayList<>();
            if (contextConfiguration != null) {
                successList.addAll(evaluate(message, contextConfiguration, false));
            }
            if (bodyConfiguration != null) {
                successList.addAll(evaluate(message, bodyConfiguration, true));
            }
            return successList;
        } catch (Exception e) {
            String exceptionMessage = RBUtil.getMessage(Bundle.class, Bundle.EVALUATE_FAILED);
            throw new ServiceExecutionException(exceptionMessage, e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }
    }

    private List<Evaluator> evaluate(TextMessage message, XSLTConfiguration configuration, boolean onBody) throws TransformerException, JMSException, IOException {
        List<Evaluator> successList = new ArrayList<>();
        String xml = null;
        if (onBody) {
            xml = message.getText();
        } else {
            CarryForwardContext context = (CarryForwardContext) MessageUtil.getCarryForwardContext(message);
            if (context != null) {
                xml = context.getAppContext();
            }
        }
        if (xml == null) {
            return successList;
        }
        StringWriter writer = new StringWriter();
        configuration.transformer.transform(new SAXSource(new InputSource(new StringReader(xml))), new StreamResult(writer));
        String resultValue = writer.toString().trim();
        writer.close();
        if (!StringUtil.isEmpty(resultValue)) {
            String[] results = resultValue.split(",");
            for (String result : results) {
                successList.add(destinations.get(result));
            }
        }
        return successList;
    }

    public void addEvaluator(Evaluator evaluator) throws ServiceExecutionException {
        CBRConfiguration configuration = evaluator.getConfiguration();
        HashMap<String, String> expressions = CBRConstants.CONTEXT.equals(configuration.getField()) ? contextExpressions : bodyExpressions;
        expressions.put(configuration.getCondition(), configuration.getDestination());
        destinations.put(configuration.getDestination(), evaluator);
        logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.MESSAGES_SATISFYING_XPATH, new Object[]{configuration.getCondition(), configuration.getDestination()}));
    }

    public void initialize() throws ServiceExecutionException {
        if (contextExpressions != null && !contextExpressions.isEmpty()) {
            contextConfiguration = createXSLTConfiguration(contextExpressions);
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.XSLT_EVALUATOR_ADDED, new Object[]{CBRConstants.CONTEXT, getExpressionsAsCSV(contextExpressions.keySet())}));
        }
        if (bodyExpressions != null && !bodyExpressions.isEmpty()) {
            bodyConfiguration = createXSLTConfiguration(bodyExpressions);
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.XSLT_EVALUATOR_ADDED, new Object[]{CBRConstants.CONTEXT, getExpressionsAsCSV(bodyExpressions.keySet())}));
        }
    }

    private String getExpressionsAsCSV(Set conditions) {
        String expressionsString = "";
        for (Object condition : conditions) {
            expressionsString += (condition + ",");
        }
        return expressionsString;
    }

    private XSLTConfiguration createXSLTConfiguration(HashMap<String, String> expressions) throws ServiceExecutionException {
        CBRXSLCreator xslCreator = new CBRXSLCreator(expressions, namespaces);
        XSLTConfiguration configuration = new XSLTConfiguration();
        try {
            configuration.xsl = xslCreator.createXSL();
            configuration.factory = TransformerUtil.createFactory(TransformerUtil.SAXON_TRANSFORMER_FACTORY);
            configuration.templates = configuration.factory.newTemplates(new SAXSource(new InputSource(new StringReader(configuration.xsl))));
            configuration.transformer = configuration.templates.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.TRANSFORMER_CONFIGURATION_EXCEPTION), e, ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        } catch (SAXException e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.SAX_EXCEPTION), e, ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        } catch (Exception e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.TRANSFORMER_CREATION_EXCEPTION), e, ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
        return configuration;
    }

    private static class XSLTConfiguration {
        private String xsl;
        private Templates templates;
        private Transformer transformer;
        private TransformerFactory factory;
    }
}
