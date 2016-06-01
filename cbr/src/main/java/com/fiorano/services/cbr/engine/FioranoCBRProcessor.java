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
import fiorano.jms.cbr.cbr1.IMatchingSetInfo;
import fiorano.jms.cbr.cbr1.def.SelectorSet;
import fiorano.jms.runtime.common.FioranoSession;
import fiorano.jms.services.msg.def.FioranoTextMessage;
import fiorano.jms.services.msg.def.FioranoXMLMessage;

import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * Date: Aug 26, 2007
 * Time: 1:07:48 AM
 *
 * @author Venkat
 * @author Abhinay Dubey
 * @version 1.2, 3 October 2008
 */
public class FioranoCBRProcessor implements ICBRProcessor {

    private SelectorSet selectorSet;
    private FioranoSession session;
    private Logger logger;

    public FioranoCBRProcessor(FioranoSession session, Logger logger) {
        this.selectorSet = new SelectorSet();
        this.session = session;
        this.logger = logger;
    }

    public void initialize() throws ServiceExecutionException {
    }

    public void addEvaluator(Evaluator evaluator) throws ServiceExecutionException {
        try {
            selectorSet.addMember(evaluator);
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.XPATH_EVALUATOR_ADDED, new Object[]{evaluator.getConfiguration().getCondition()}));
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.MESSAGES_SATISFYING_XPATH, new Object[]{evaluator.getConfiguration().getCondition(), evaluator.getConfiguration().getDestination()}));
        } catch (InvalidSelectorException e) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.EVALUATOR_ADDITION_FAILED,
                    new Object[]{evaluator.getConfiguration().getDestination(), evaluator.getConfiguration().getCondition()});
            throw new ServiceExecutionException(message, e, ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }

    public List<Evaluator> evaluate(TextMessage message) throws ServiceExecutionException {
        FioranoXMLMessage xmlMessage;
        try {
            if (message instanceof FioranoXMLMessage) {
                xmlMessage = (FioranoXMLMessage) message;
            } else {
                xmlMessage = session.createXMLMessage();
                if (message instanceof FioranoTextMessage) {
                    //this can be removed when FMQ bug dependent on 11463 is fixed
                    FioranoTextMessage fioranoTextMessage = (FioranoTextMessage) message;
                    xmlMessage.setEncoding(fioranoTextMessage.getEncoding());
                    xmlMessage.setData(fioranoTextMessage._encode(fioranoTextMessage.getText()));
                } else {

                    xmlMessage.setText(message.getText());
                }
            }
            IMatchingSetInfo matchingSet = selectorSet.getMatching(xmlMessage);
            return matchingSet.getMatchingConsumers();
        } catch (JMSException e) {
            String exceptionMessage = RBUtil.getMessage(Bundle.class, Bundle.EVALUATE_FAILED);
            throw new ServiceExecutionException(exceptionMessage, e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        } catch (Exception e) {
            String exceptionMessage = RBUtil.getMessage(Bundle.class, Bundle.EVALUATE_FAILED);
            throw new ServiceExecutionException(exceptionMessage, e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }
    }
}
