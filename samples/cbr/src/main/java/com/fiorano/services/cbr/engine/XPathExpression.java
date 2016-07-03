/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

import fiorano.esb.util.CarryForwardContext;
import fiorano.esb.util.MessageUtil;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.InputSource;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * Date: Aug 26, 2007
 * Time: 1:18:31 AM
 *
 * @author Venkat
 * @author Abhinay Dubey
 * @version 1.1, 3 October 2008
 */
public class XPathExpression {

    private net.sf.saxon.sxpath.XPathExpression expression;
    private String field;

    public XPathExpression(net.sf.saxon.sxpath.XPathExpression expression, String field, Logger logger) {
        this.expression = expression;
        this.field = field;
    }

    public boolean evaluate(TextMessage message) throws JMSException, XPathException {
        String xml;
        if (CBRConstants.CONTEXT.equals(field)) {
            CarryForwardContext context = (CarryForwardContext) MessageUtil.getCarryForwardContext(message);
            if (context == null) {
                return false;
            }
            xml = context.getAppContext();
        } else {
            xml = message.getText();
        }
        if (xml == null) {
            return false;
        }
        InputSource is = new InputSource();
        SAXSource ss = new SAXSource(is);
        ss.getInputSource().setCharacterStream(new StringReader(xml));
        Object result = expression.evaluateSingle(ss);
        return result != null && (!(result instanceof Boolean) || result.equals(Boolean.TRUE));
    }

}
