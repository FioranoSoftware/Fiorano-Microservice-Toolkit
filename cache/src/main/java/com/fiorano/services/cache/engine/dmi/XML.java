/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

import com.fiorano.services.cache.CacheConstants;
import com.fiorano.xml.sax.XMLCreator;
import org.xml.sax.SAXException;

import java.util.jar.Attributes;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ramesh
 * Date: 9/13/12
 * Time: 8:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class XML {

    private String value;

    public XML(String value) {
        this.value = value;
    }

    public static String getSimpleName() {
        return "XML";
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XML xml = (XML) o;

        if (value != null ? !value.equals(xml.value) : xml.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return (String) value;
    }

    public void write(XMLCreator writer) throws SAXException {
        if (value == null || value.trim().length() == 0)
            return;
        int prefixCnt = 1;

        String[] elements = value.split(Pattern.quote(CacheConstants.FIORANO_XML_DELIMITER));

        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            if (element.startsWith("{/")) {
                writer.endElement();
            } else if (element.startsWith("{")) {
                element = element.replaceAll("\\{", "");
                element = element.replaceAll("\\}", "");
                String ns = null;
                if (element.contains(CacheConstants.NS_ELEMENT_DELIMITER)) {
                    ns = element.substring(0, element.indexOf(CacheConstants.NS_ELEMENT_DELIMITER));
                    element = element.replaceAll(Pattern.quote(ns + CacheConstants.NS_ELEMENT_DELIMITER), "");
                    if (ns != null && ns.trim().length() != 0) {
                        String prefix = writer.getPrefix(ns);
                        if (prefix == null) {
                            writer.startPrefixMapping("tns" + prefixCnt, ns);
                            writer.endPrefixMapping("tns" + prefixCnt);
                            prefixCnt++;
                        }
                    }
                }
                String localName = null;
                Attributes attrs = new Attributes();
                if (!element.contains(" ")) {
                    localName = element;
                } else {
                    localName = element.substring(0, element.indexOf(" "));
                    element = element.replaceAll(localName, "");
                    element = element.trim();
                    if (element.length() != 0 && element.contains("=")) {
                        String[] ats = element.split(" ");
                        for (String at : ats) {
                            String name = at.substring(0, at.indexOf("="));
                            String value = at.substring(at.indexOf("=") + 1);
                            writer.addAttribute(name, value);
                        }
                    }
                }
                writer.startElement(ns == null ? "" : ns, localName);
            } else {
                if (element.trim().length() == 0)
                    continue;
                element = element.replaceAll(Pattern.quote(CacheConstants.CURLY_BRACES_START_REPLACER), "{");
                element = element.replaceAll(Pattern.quote(CacheConstants.CURLY_BRACES_END_REPLACER), "}");
                writer.addText(element);
            }
        }


    }
}
