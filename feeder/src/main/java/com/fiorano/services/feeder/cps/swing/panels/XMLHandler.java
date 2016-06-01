/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps.swing.panels;

import com.fiorano.uif.xml.util.XMLSampleDialog2;
import com.fiorano.uif.xml.util.XMLSampleDialog3;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.xsd.XSDUtil;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CPSUtil;
import fiorano.esb.utils.RBUtil;
import fiorano.esb.utils.XSDValidator;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Handling XML generation
 *
 * @author Venkat
 *         Date: Nov 10, 2006
 *         Time: 12:38:10 AM
 */
public class XMLHandler {
    public static String generateXML(Window owner, ESBRecordDefinition schema)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String str = null;
        if (schema == null || StringUtil.isEmpty(schema.getStructure())) {
            return str;
        }
        if (schema.getDefinitionType() == ESBRecordDefinition.DTD) {
            StringReader reader = new StringReader(schema.getStructure());
            DTDParser parser = new DTDParser(reader);
            DTD dtd = parser.parse(true);

            if (owner instanceof Dialog) {
                str = XMLSampleDialog3.generateSampleXML((Dialog) owner, dtd, schema.getRootElementName());
            } else {
                str = XMLSampleDialog3.generateSampleXML((Frame) owner, dtd, schema.getRootElementName());
            }
        } else {
            XSLoader xsLoader = XSDUtil.createXSLoader(schema, null);
            XSModel model = xsLoader.load(new DOMInputImpl(null, null, null, schema.getStructure(), null));
            if (model == null) {
                CPSUtil.getAnonymousLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.MODEL_NULL));
            }

            if (owner instanceof Dialog) {
                str = XMLSampleDialog2.generateSampleXML((Dialog) owner, model, schema.getRootElementName(), schema.getTargetNamespace());
            } else {
                str = XMLSampleDialog2.generateSampleXML((Frame) owner, model, schema.getRootElementName(), schema.getTargetNamespace());
            }
        }
        return str;
    }

    public static boolean validateXML(String xml, ESBRecordDefinition schema) throws Exception {
        if (schema == null || schema.getStructure() == null) {
            return true;
        }
        if (schema.getDefinitionType() == ESBRecordDefinition.DTD) {
            XSDValidator.validateXMLwithDTD(xml, schema.getStructure(), schema.getRootElementName());
        } else {
            XSDValidator.validateXMLwithSchema(xml, schema.getStructure(), getImportedStructures(schema));
        }
        return true;
    }

    private static Map getImportedStructures(ESBRecordDefinition schema) {
        Map imports = schema.getImportedStructures();
        //use this object to set to the resource
        Hashtable importedXSDs = new Hashtable();
        if (imports != null) {
            Set properties = imports.entrySet();
            Iterator iter = properties.iterator();
            if (iter != null) {
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof java.util.List) {
                        importedXSDs.put(key, ((java.util.List) value).get(0));
                    } else if (value instanceof String[]) {
                        importedXSDs.put(key, ((String[]) value)[0]);
                    } else if (value instanceof String) {
                        importedXSDs.put(key, value);
                    }
                }
            }
        }
        return importedXSDs;
    }
}