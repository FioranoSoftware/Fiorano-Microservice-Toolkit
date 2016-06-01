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
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.trans.IndependentContext;
import net.sf.saxon.trans.XPathException;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * Date: Aug 25, 2007
 * Time: 11:25:55 PM
 *
 * @author Venkat
 * @author Abhinay Dubey
 * @version 1.3, 3 October 2008
 */
public class XPathProcessor implements ICBRProcessor {
    private XPathEvaluator evaluator;
    private IndependentContext defaultContext;
    private IndependentContext backwardCompatibleContext;
    private Map<XPathExpression, Evaluator> expressions = new HashMap<>(3);
    private Logger logger;

    public XPathProcessor(Map<String, String> namespaces, Logger logger) {
        evaluator = new XPathEvaluator();
        defaultContext = evaluator.getStaticContext();
        this.logger = logger;
        backwardCompatibleContext = new BackwardCompatibleContext();
        if (namespaces != null && !namespaces.isEmpty()) {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                defaultContext.declareNamespace(entry.getKey(), entry.getValue());
                backwardCompatibleContext.declareNamespace(entry.getKey(), entry.getValue());
            }
        }
    }

    public void initialize() throws ServiceExecutionException {
    }

    public List<Evaluator> evaluate(TextMessage message) throws ServiceExecutionException {
        List<Evaluator> successList = new ArrayList<>(2);
        for (Map.Entry<XPathExpression, Evaluator> entry : expressions.entrySet()) {
            try {
                if ((entry.getKey()).evaluate(message)) {
                    successList.add(entry.getValue());
                }
            } catch (JMSException e) {
                String exceptionMessage = RBUtil.getMessage(Bundle.class, Bundle.EVALUATE_FAILED);
                throw new ServiceExecutionException(exceptionMessage, e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            } catch (XPathException e) {
                String exceptionMessage = RBUtil.getMessage(Bundle.class, Bundle.EVALUATE_FAILED);
                throw new ServiceExecutionException(exceptionMessage, e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        }
        return successList;
    }

    public void addEvaluator(Evaluator evaluator) throws ServiceExecutionException {
        CBRConfiguration configuration = evaluator.getConfiguration();
        this.evaluator.setStaticContext(configuration.useXpath1_0() ? backwardCompatibleContext : defaultContext);
        if (configuration.useXpath1_0()) {
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.USING_XPATH1_0));
        }

        XPathExpression expression;
        try {
            expression = new XPathExpression(this.evaluator.createExpression(configuration.getCondition()), configuration.getField(), logger);
            logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.XPATH_EVALUATOR_ADDED, new Object[]{configuration.getCondition()}));
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.MESSAGES_SATISFYING_XPATH, new Object[]{configuration.getCondition(), configuration.getDestination()}));
        } catch (XPathException e) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.EVALUATOR_ADDITION_FAILED,
                    new Object[]{evaluator.getConfiguration().getDestination(), evaluator.getConfiguration().getCondition()});
            throw new ServiceExecutionException(message, e, ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
        expressions.put(expression, evaluator);
    }

    private static class BackwardCompatibleContext extends IndependentContext {
        public boolean isInBackwardsCompatibleMode() {
            return true;
        }
    }
}
