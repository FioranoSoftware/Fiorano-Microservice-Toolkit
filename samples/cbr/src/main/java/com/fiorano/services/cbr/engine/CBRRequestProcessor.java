/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.engine.IRequestValidator;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIORequestProcessor;
import com.fiorano.edbc.framework.service.internal.transport.jms.AbstractSyncIOTransportManager;
import com.fiorano.services.cbr.transport.jms.Bundle;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.MessageUtil;
import fiorano.jms.runtime.common.FioranoSession;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 27 Dec, 2010
 * Time: 3:21:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class CBRRequestProcessor extends AbstractSyncIORequestProcessor {

    private List<ICBRProcessor> cbrProcessors;
    private CBRPropertyModel configuration;
    private boolean initialized;
    private AbstractSyncIOTransportManager.TransportAssociation transportAssociation;

    public CBRRequestProcessor(IModule parent, IServiceConfiguration configuration,
                               AbstractSyncIOTransportManager.TransportAssociation transportAssociation) {
        super(parent, configuration);
        this.configuration = (CBRPropertyModel) configuration;
        this.transportAssociation = transportAssociation;
        this.logger = getParent().getLogger();
    }

    public String process(String request) throws ServiceExecutionException {
        return request;
    }

    public IRequestValidator<String> getRequestValidator() {
        return null;
    }

    public void initialize() throws ServiceExecutionException {
        if (initialized)
            return;

        ICBRProcessor processor;

        if (configuration.isFioranoCBR()) {
            processor = new FioranoCBRProcessor((FioranoSession) transportAssociation.getInputTransport().getSession(), logger);
            logger.log(Level.INFO, RBUtil.getMessage(com.fiorano.services.cbr.transport.jms.Bundle.class, com.fiorano.services.cbr.transport.jms.Bundle.USING_FIORANOMQ_CBR));
        } else {

            switch (configuration.getProcessorType()) {
                case CBRConstants.XSLT_TYPE:
                    processor = new XSLTProcessor(configuration.getNamespaces(), logger);
                    break;
                case CBRConstants.XPATH_TYPE:
                default:
                    processor = new XPathProcessor(configuration.getNamespaces(), logger);
                    break;
            }
            logger.log(Level.INFO, RBUtil.getMessage(com.fiorano.services.cbr.transport.jms.Bundle.class, com.fiorano.services.cbr.transport.jms.Bundle.USING_SAXON_XPATH_CBR));
        }
        List<String> xpaths = configuration.getXPaths();
        for (int i = 0; i < xpaths.size(); i++) {
            String xpath = xpaths.get(i);
            ArrayList outPortNames = configuration.getOutPortNames();
            Evaluator evaluator = new Evaluator(new CBRConfiguration(xpath, configuration.getApplyOnXPath() ? CBRConstants.CONTEXT : CBRConstants.BODY,
                    (String) outPortNames.get(i), configuration.isFioranoCBR(), configuration.getUseXPath1_0()));
            if (configuration.getApplyOnXPath())
                logger.log(Level.FINE, RBUtil.getMessage(com.fiorano.services.cbr.transport.jms.Bundle.class, com.fiorano.services.cbr.transport.jms.Bundle.XPATH_ON_CONTEXT, new Object[]{xpath}));
            else
                logger.log(Level.FINE, RBUtil.getMessage(com.fiorano.services.cbr.transport.jms.Bundle.class, com.fiorano.services.cbr.transport.jms.Bundle.XPATH_ON_BODY, new Object[]{xpath}));

            processor.addEvaluator(evaluator);

        }
        processor.initialize();
        cbrProcessors = new ArrayList<>(1);
        cbrProcessors.add(processor);
        initialized = true;
    }

    public Map<String, List<Message>> handleRequest(Message message)
            throws ServiceExecutionException {

        Map<String, List<Message>> responses = new HashMap<>();

        if (!initialized)
            initialize();
        try {
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.MESSAGE_RECEIVED));
            long startTime = System.currentTimeMillis();
            if (logger.isLoggable(Level.FINE)) {
                try {
                    logger.log(Level.FINE, ((TextMessage) message).getText());
                } catch (JMSException e) {
                    //
                }
            }
            boolean noMatch = true;
            for (ICBRProcessor icbrProcessor : cbrProcessors) {

                try {
                    MessageUtil.setTextData(message, decryptMessage(MessageUtil.getTextData(message)));
                } catch (JMSException e) {
                    logger.log(Level.WARNING, "Unable to decrypt request message. Reason : " + e.getMessage());
                }

                List<Evaluator> list = icbrProcessor.evaluate((TextMessage) message);
                for (Evaluator evaluator : list) {
                    noMatch = false;
                    String destinationName = evaluator.getConfiguration().getDestination();
                    for (String destinationNameOfPort : transportAssociation.getOutputTransports().keySet()) {
                        if (destinationName.equalsIgnoreCase(destinationNameOfPort)) {
                            destinationName = destinationNameOfPort;
                            break;
                        }
                    }

                    try {
                        MessageUtil.setTextData(message, encryptMessage(MessageUtil.getTextData(message)));
                    } catch (JMSException e) {
                        logger.log(Level.WARNING, "Unable to encrypt response message. Reason : " + e.getMessage());
                    }

                    List<Message> messages = new ArrayList<>();
                    messages.add(message);
                    responses.put(destinationName, messages);
                    String info = RBUtil.getMessage(Bundle.class, Bundle.CONDITION_SATISFIED,
                            new Object[]{destinationName, evaluator.getConfiguration().getCondition()});
                    logger.log(Level.INFO, info);
                    logger.log(Level.FINE, com.fiorano.services.common.util.RBUtil.getMessage(Bundle.class, Bundle.COMMIT_TRANSACTION_SUCCESSFUL));

                }
            }
            if (noMatch) {
                logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.ALL_CONDITION_FAILED));
                List<Message> messages = new ArrayList<>();
                messages.add(message);
                responses.put("OUT_FALSE", messages);
            }
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.TRANSACTION_TIME, new Object[]{"" + (System.currentTimeMillis() - startTime)}));
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.TRANSACTION_COMPLETED));

        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_PROCESSING_MESSAGE), e);
            throw new ServiceExecutionException(e.getMessage(), e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }

        return responses;
    }
}
