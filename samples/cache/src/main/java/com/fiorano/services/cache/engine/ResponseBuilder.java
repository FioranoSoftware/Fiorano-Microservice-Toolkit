/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine;

import com.fiorano.services.cache.CacheConstants;
import com.fiorano.services.cache.engine.dmi.IField;
import com.fiorano.services.cache.engine.dmi.XML;
import com.fiorano.services.cache.engine.storage.CacheEntry;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.xml.sax.XMLCreator;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * builds the response XML
 *
 * @author Venkat
 */
public class ResponseBuilder {

    private XMLCreator xmlWriter;
    private StringWriter resultWriter;
    private boolean initialized = false;
    private Logger logger = Logger.getAnonymousLogger();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ResponseBuilder(Logger logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    /**
     * adds entry to the response xml
     *
     * @param cacheEntry
     */
    public void addCacheEntry(CacheEntry cacheEntry) {
        if (cacheEntry == null || cacheEntry.getKey() == null) {
            return;
        }
        if (!initialized) {
            throw new IllegalStateException(RBUtil.getMessage(Bundle.class, Bundle.BUILDER_NOT_INITIALIZED));
        }
        try {
            xmlWriter.startElement(CacheConstants.TNS, CacheConstants.CACHE_ENTRY);
            IField key = cacheEntry.getKey();
            xmlWriter.startElement(CacheConstants.TNS, CacheConstants.KEY);
            Object value = key.getValue();
            if (value instanceof XML) {
                xmlWriter.startElement(CacheConstants.TNS, key.getName());
                ((XML) value).write(xmlWriter);
                xmlWriter.endElement();
            } else if (value instanceof Date) {
                xmlWriter.addElement(CacheConstants.TNS, key.getName(), simpleDateFormat.format(value));
            } else {
                xmlWriter.addElement(CacheConstants.TNS, key.getName(), key.getValue().toString());
            }
            xmlWriter.endElement();

            Map<String, IField> data = cacheEntry.getValue();
            if (data != null && !data.isEmpty()) {
                xmlWriter.startElement(CacheConstants.TNS, CacheConstants.DATA);
                for (Object o : data.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    IField field = (IField) entry.getValue();
                    Object val = field.getValue();
                    if (val instanceof XML) {
                        xmlWriter.startElement(CacheConstants.TNS, field.getName());
                        ((XML) val).write(xmlWriter);
                        xmlWriter.endElement();
                    } else if (val instanceof Date) {
                        xmlWriter.addElement(CacheConstants.TNS, field.getName(), simpleDateFormat.format(val));
                    } else {
                        xmlWriter.addElement(CacheConstants.TNS, field.getName(), val == null ? null : val.toString());
                    }
                }
                xmlWriter.endElement();
            }
            xmlWriter.endElement();
        } catch (SAXException e) {
            //no need to throw exception
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ADD_ENTRY_TO_RESPONSE_FAILED), e);
        }
    }

    /**
     * starts building the response xml
     */
    public void initialize() throws CacheException {
        if (initialized) {
            return;
        }
        resultWriter = new StringWriter();
        try {
            xmlWriter = new XMLCreator(new StreamResult(resultWriter), true, false);
        } catch (TransformerConfigurationException e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.XML_WRITER_CREATION_FAILED),
                    e);
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.XML_WRITER_CREATION_FAILED), e);
        }

        try {
            xmlWriter.startDocument();
            xmlWriter.startPrefixMapping("tns", CacheConstants.TNS);
        } catch (SAXException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.PREFIX_MAP_FAILED), e);
        }

        try {
            xmlWriter.startElement(CacheConstants.TNS, CacheConstants.CACHE);
        } catch (SAXException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.XML_CREATION_FAILED), e);
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.XML_WRITER_CREATION_FAILED), e);
        }
        initialized = true;
    }

    /**
     * stops building the response xml and returns the xml
     */
    public String close() {
        if (!initialized) {
            return null;
        }
        initialized = false;
        try {
            xmlWriter.endElement();
            xmlWriter.endDocument();
        } catch (SAXException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.XML_CREATION_FAILED), e);
        } finally {
            xmlWriter = null;
        }
        String responseXML = resultWriter.toString();
        try {
            resultWriter.close();
        } catch (IOException e) {
            //do not throw runtime exception
        } finally {
            resultWriter = null;
        }
        return responseXML;
    }
}
