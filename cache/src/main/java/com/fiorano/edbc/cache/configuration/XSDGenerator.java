/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.cache.configuration;

import com.fiorano.services.cache.CacheConstants;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.ClarkName;
import com.fiorano.xml.Namespaces;
import com.fiorano.xml.sax.XMLCreator;
import com.fiorano.xml.xsd.XSDCreator;
import fiorano.esb.record.ESBRecordDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSModelGroup;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * A helper class to create Schema Definition for ports
 *
 * @author FSTPL
 */
public class XSDGenerator {
    public static ESBRecordDefinition getSchema(FieldDefinitions fieldDefinitions, String portName)
            throws TransformerConfigurationException, SAXException {
        if (fieldDefinitions != null) {
            Hashtable<String, String> imports = new Hashtable<>();
            Hashtable<String, String> imports1 = new Hashtable<>();
            Hashtable fields = fieldDefinitions.getFieldDefinitiontable();
            Set<Map.Entry> entries = fields.entrySet();
            for (Map.Entry entry : entries) {
                FieldDefinition def = (FieldDefinition) entry.getValue();
                if (def != null) {
                    ESBRecordDefinition xsd = def.getXsd();
                    if (xsd != null) {
                        if (xsd.getTargetNamespace() != null)
                            imports.put(xsd.getTargetNamespace(), xsd.getStructure());
                        if (xsd.getImportedStructures() != null)
                            imports1.putAll(xsd.getImportedStructures());
                    }
                }
            }


            StringWriter writer = new StringWriter();
            XMLCreator xmlWriter = new XMLCreator(new StreamResult(writer), true, false);
            XSDCreator xsdCreator = new XSDCreator(xmlWriter);
            xsdCreator.startDocument();

            xmlWriter.startPrefixMapping("tns", CacheConstants.TNS);
            xmlWriter.endPrefixMapping("tns");
            xmlWriter.startPrefixMapping("xsd", Namespaces.URI_XSD);
            xmlWriter.endPrefixMapping("xsd");
            Set<Map.Entry<String, String>> keys = imports.entrySet();
            int i = 1;
            for (Map.Entry key : keys) {
                xmlWriter.startPrefixMapping("ns" + i, (String) key.getKey());
                xmlWriter.endPrefixMapping("ns" + i);
                i++;
            }

            xmlWriter.addAttribute("elementFormDefault", "qualified");
            xsdCreator.startSchema(CacheConstants.TNS);

            keys = imports.entrySet();
            for (Map.Entry key : keys) {
                xsdCreator.addImport((String) key.getKey(), null);
            }
            xsdCreator.startElement(CacheConstants.CACHE, null);
            xsdCreator.startComplexType(null, (short) 0);

            if (CacheConstants.OUT_PORT.equals(portName)) {
                xsdCreator.startModelGroup(XSModelGroup.COMPOSITOR_SEQUENCE);
            } else {
                xsdCreator.startModelGroup(XSModelGroup.COMPOSITOR_CHOICE);
                xsdCreator.startElement(CacheConstants.ALL, null);
                xsdCreator.startComplexType(null, (short) 0);
                xsdCreator.endComplexType();
                xsdCreator.endElement();
            }

            xsdCreator.addParticle(1, -1);
            xsdCreator.startElementRef(ClarkName.toClarkName(CacheConstants.TNS, CacheConstants.CACHE_ENTRY));
            xsdCreator.endElementRef();

            xsdCreator.endModelGroup();
            xsdCreator.endComplexType();
            xsdCreator.endElement();

            computeCacheEntrySchema(xsdCreator, portName, fieldDefinitions);

            xsdCreator.endSchema();
            xsdCreator.endDocument();
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ESBRecordDefinition recordDefinition = new ESBRecordDefinition();
            recordDefinition.setStructure(writer.toString());
            recordDefinition.setRootElementName(ClarkName.toClarkName(CacheConstants.TNS, CacheConstants.CACHE));
            imports.putAll(imports1);
            recordDefinition.setImportedStructures(imports);
            return recordDefinition;
        }
        return null;
    }

    private static void computeDataFieldsSchema(XSDCreator xsdCreator, FieldDefinitions fieldDefTable) throws SAXException {
        xsdCreator.startElement(CacheConstants.DATA, null);
        xsdCreator.startComplexType(null, XSConstants.DERIVATION_NONE);
        xsdCreator.startModelGroup(XSModelGroup.COMPOSITOR_ALL);

        for (Object aFieldDefTable : fieldDefTable) {
            FieldDefinition fieldDef = (FieldDefinition) aFieldDefTable;
            if (!fieldDef.isKey()) {
                xsdCreator.addParticle(0, 1);
                if (!"XML".equalsIgnoreCase(fieldDef.getShortClassName())) {
                    xsdCreator.startElement(fieldDef.getName(), ClarkName.toClarkName(Namespaces.URI_XSD, fieldDef.getShortClassName()));
                    xsdCreator.endElement();
                } else {
                    xsdCreator.startElement(fieldDef.getName(), null);
                    xsdCreator.startComplexType(null, (short) 0);
                    xsdCreator.startModelGroup((short) 1);

                    ESBRecordDefinition recordDefinition = fieldDef.getXsd();
                    if (recordDefinition == null) {
                        xsdCreator.addAny();
                    } else {
                        xsdCreator.startElementRef(ClarkName.toClarkName(recordDefinition.getTargetNamespace(), recordDefinition.getRootElementName()));
                        xsdCreator.endElementRef();
                    }
                    xsdCreator.endModelGroup();
                    xsdCreator.endComplexType();
                    xsdCreator.endElement();
                }
            }
        }

        xsdCreator.endModelGroup();
        xsdCreator.endComplexType();
        xsdCreator.endElement();
    }

    private static void computeKeyFieldsSchema(XSDCreator xsdCreator, String portName, FieldDefinitions fieldDefTable) throws SAXException {
        boolean isOutPort = CacheConstants.OUT_PORT.equals(portName);
        xsdCreator.startElement(isOutPort ? CacheConstants.KEY : CacheConstants.KEYS, null);
        xsdCreator.startComplexType(null, XSConstants.DERIVATION_NONE);
        xsdCreator.startModelGroup(isOutPort ? XSModelGroup.COMPOSITOR_CHOICE : XSModelGroup.COMPOSITOR_ALL);

        for (Object aFieldDefTable : fieldDefTable) {
            FieldDefinition fieldDef = (FieldDefinition) aFieldDefTable;
            if (fieldDef.isKey()) {
                xsdCreator.addParticle(isOutPort ? 1 : 0, 1);
                if (!"XML".equalsIgnoreCase(fieldDef.getShortClassName())) {
                    xsdCreator.startElement(fieldDef.getName(), ClarkName.toClarkName(Namespaces.URI_XSD, fieldDef.getShortClassName()));
                    xsdCreator.endElement();
                } else {
                    xsdCreator.startElement(fieldDef.getName(), null);
                    xsdCreator.startComplexType(null, (short) 0);
                    xsdCreator.startModelGroup((short) 1);

                    ESBRecordDefinition recordDefinition = fieldDef.getXsd();
                    if (recordDefinition == null || StringUtil.isEmpty(recordDefinition.getRootElementName())) {
                        xsdCreator.addAny();
                    } else {
                        xsdCreator.startElementRef(ClarkName.toClarkName(recordDefinition.getTargetNamespace(), recordDefinition.getRootElementName()));
                        xsdCreator.endElementRef();
                    }
                    xsdCreator.endModelGroup();
                    xsdCreator.endComplexType();
                    xsdCreator.endElement();
                }
            }
        }

        xsdCreator.endModelGroup();
        xsdCreator.endComplexType();
        xsdCreator.endElement();
    }

    private static void computeCacheEntrySchema(XSDCreator xsdCreator, String portName, FieldDefinitions fieldDefinitions) throws SAXException {
        boolean isDelPort = CacheConstants.DEL_PORT.equals(portName);
        boolean isOutPort = CacheConstants.OUT_PORT.equals(portName);

        xsdCreator.startElement(CacheConstants.CACHE_ENTRY, null);
        xsdCreator.startComplexType(null, XSConstants.DERIVATION_NONE);
        xsdCreator.startModelGroup(XSModelGroup.COMPOSITOR_SEQUENCE);
        xsdCreator.startElementRef(ClarkName.toClarkName(CacheConstants.TNS, isOutPort ? CacheConstants.KEY : CacheConstants.KEYS));
        xsdCreator.endElementRef();
        if (!isDelPort) {
            xsdCreator.addParticle(0, 1);
            xsdCreator.startElementRef(ClarkName.toClarkName(CacheConstants.TNS, CacheConstants.DATA));
            xsdCreator.endElementRef();
        }
        xsdCreator.endModelGroup();
        xsdCreator.endComplexType();
        xsdCreator.endElement();
        computeKeyFieldsSchema(xsdCreator, portName, fieldDefinitions);
        if (!isDelPort) {
            computeDataFieldsSchema(xsdCreator, fieldDefinitions);
        }

    }
}
