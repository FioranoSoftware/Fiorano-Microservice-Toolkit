/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

import com.fiorano.xml.sax.XMLCreator;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Date: 2 Apr, 2008
 * Time: 1:39:22 PM
 * To change this template use File | Settings | File Templates.
 *
 * @author Venkat
 * @version 1.0, 3 April 2008
 */
public class CBRXSLCreator {
    private static final String XSL_PREFIX = "xsl";
    private static final String XSL_NAMESPACE = "http://www.w3.org/1999/XSL/Transform";
    private static final String ELEM_STYLESHEET = "stylesheet";
    private static final String ELEM_OUTPUT = "output";
    private static final String ELEM_TEMPLATE = "template";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_METHOD = "method";
    private static final String ATTR_ENCODING = "encoding";
    private static final String ATTR_MATCH = "match";
    private static final String ELEM_IF = "if";
    private static final String ATTR_TEST = "test";
    private HashMap<String, String> prefixNamespaceMap;
    private HashMap<String, String> conditionValueMap;

    public CBRXSLCreator(HashMap<String, String> conditionValueMap, HashMap<String, String> prefixNamespaceMap) {
        this.conditionValueMap = conditionValueMap;
        this.prefixNamespaceMap = prefixNamespaceMap;
        if (this.prefixNamespaceMap == null) {
            this.prefixNamespaceMap = new HashMap<>();
        }
        this.prefixNamespaceMap.put(XSL_PREFIX, XSL_NAMESPACE);
    }

    public String createXSL() throws TransformerConfigurationException, SAXException {

        StringWriter writer = new StringWriter();
        XMLCreator xmlCreator = new XMLCreator(new StreamResult(writer), true, true);
        xmlCreator.startDocument();
        for (Map.Entry<String, String> entry : prefixNamespaceMap.entrySet()) {
            xmlCreator.startPrefixMapping(entry.getKey(), entry.getValue());
        }
        xmlCreator.addAttribute(ATTR_VERSION, "1.0");
        xmlCreator.startElement(XSL_NAMESPACE, ELEM_STYLESHEET);
        xmlCreator.addAttribute(ATTR_METHOD, "text");
        xmlCreator.addAttribute(ATTR_ENCODING, "UTF-8");
        xmlCreator.startElement(XSL_NAMESPACE, ELEM_OUTPUT);
        xmlCreator.endElement();//ouput
        xmlCreator.addAttribute(ATTR_MATCH, "/");

        xmlCreator.startElement(XSL_NAMESPACE, ELEM_TEMPLATE);
        for (Map.Entry<String, String> entry : conditionValueMap.entrySet()) {
            xmlCreator.addAttribute(ATTR_TEST, entry.getKey());
            xmlCreator.addElement(XSL_NAMESPACE, ELEM_IF, entry.getValue() + ",");
        }

        xmlCreator.endElement();   //template
        xmlCreator.endElement(); //stylesheet
        xmlCreator.endDocument();
        return writer.toString();
    }
}
