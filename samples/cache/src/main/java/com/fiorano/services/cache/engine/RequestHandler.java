/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.edbc.cache.configuration.FieldDefinition;
import com.fiorano.services.cache.CacheConstants;
import com.fiorano.services.cache.engine.dmi.FieldFactory;
import com.fiorano.services.cache.engine.dmi.FieldList;
import com.fiorano.services.cache.engine.dmi.IField;
import com.fiorano.services.cache.engine.dmi.KeyField;
import com.fiorano.services.cache.engine.storage.CacheEntry;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.xml.ClarkName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * SAX event handler which converts the xml to appropriate objects during parsing
 *
 * @author Venkat
 */
public class RequestHandler extends DefaultHandler implements CacheConstants {

    static final Object ALL_ENTRIES = new Object();
    private static final int KEYS_STATE = 1;
    private static final int DATA_STATE = 2;
    private static final int UNUSED_STATE = 0;
    private FieldList keys;
    private Map<String, IField> data;
    private int state = UNUSED_STATE;
    private Notifier notifier = new Notifier();
    private boolean xmlField = false;
    private boolean elementStarted = false;

    private CachePM configuration;
    private Logger logger = Logger.getAnonymousLogger();

    private String valueContent = null;

    public RequestHandler(CachePM configuration, Logger logger) {
        this.configuration = configuration;
        if (logger != null) {
            this.logger = logger;
        }
    }

    /**
     * called when a new element is started. if the element is KEYS set state to KEYS_STATE,
     * else if element is DATA set state to DATA_STATE
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (ClarkName.toClarkName(TNS, KEYS).equals(ClarkName.toClarkName(uri, localName))) {
            state = KEYS_STATE;
            return;
        } else if (ClarkName.toClarkName(TNS, DATA).equals(ClarkName.toClarkName(uri, localName))) {
            state = DATA_STATE;
            return;
        }
        if ((state == KEYS_STATE || state == DATA_STATE) && !xmlField) {
            valueContent = null;
            FieldDefinition def = configuration.getFieldDefinitions().getFieldDefinition(localName);
            if (def != null) {
                Class name = def.getClazz();
                if (name.getName().contains("XML")) {
                    xmlField = true;
                    return;
                }
            }
        }
        if ((state == KEYS_STATE || state == DATA_STATE) && xmlField) {
            elementStarted = true;
            String val = "{";
            if (uri != null && uri.trim().length() != 0)
                val = val + uri + CacheConstants.NS_ELEMENT_DELIMITER;
            val = val + localName;
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attName = attributes.getLocalName(i);
                    if (attName == null || attName.contains(":") || attName.trim().length() == 0) {
                        continue;
                    } else {
                        val = val + " " + attName + "=" + attributes.getValue(i);
                    }
                }
            }
            val = val + "}" + CacheConstants.FIORANO_XML_DELIMITER;

            valueContent = (valueContent == null) ? val : valueContent + val;
        }
    }

    /**
     * called when character content is found. store the content if the current state is KEYS_STATE or DATA_STATE
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (state != UNUSED_STATE) {
            // populate only if the data is either in KEYS_STATE or DATA_STATE,
            // no need to change the condition to state == KEYS_STATE || state == DATA_STATE
            String value = new String(ch, start, length);
            value = value.replaceAll("\\n", "");
            value = value.replaceAll("\\r", "");
            value = value.trim();
            if (xmlField && elementStarted) {
                if (value.length() != 0) {
                    value = value.replaceAll(Pattern.quote("\\{"), CacheConstants.CURLY_BRACES_START_REPLACER);
                    value = value.replaceAll(Pattern.quote("\\}"), CacheConstants.CURLY_BRACES_END_REPLACER);
                    valueContent = valueContent == null ? value : valueContent + value;
                }
            } else {
                valueContent = valueContent == null ? value : valueContent + value;
            }
        }
    }

    /**
     * handles the end element sax event.
     * When the element closed is KEYS or DATA, the state is unused
     * When the element closed is CACHE_ENTRY, notify the observers for each of the key fields along with entire data list
     * When the element closed is ALL, notify observers to perform action on all entries in storage
     * If the state is KEYS_STATE or DATA_STATE populate in appropriate list
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        elementStarted = false;
        if (ClarkName.toClarkName(TNS, KEYS).equals(ClarkName.toClarkName(uri, localName))
                || ClarkName.toClarkName(TNS, DATA).equals(ClarkName.toClarkName(uri, localName))) {
            state = UNUSED_STATE; // change to unused data needs to be popuated only if it is either in KEYS_STATE or DATA_STATE
        }
        if (ClarkName.toClarkName(TNS, CACHE_ENTRY).equals(ClarkName.toClarkName(uri, localName))) {
            for (int i = 0; i < keys.size(); i++) {
                KeyField keyField = (KeyField) keys.getField(i);
                notifier.notifyObservers(new CacheEntry(keyField, data));
            }
            keys.clear(); //keys list can be reused
            data = null; //do not reuse data list as it will be stored into the database
        }

        if (ClarkName.toClarkName(TNS, ALL).equals(ClarkName.toClarkName(uri, localName))) {
            notifier.notifyObservers(ALL_ENTRIES);
        }
        if ((state == KEYS_STATE || state == DATA_STATE) && xmlField) {
            FieldDefinition def = null;
            if (TNS.equals(uri)) {
                def = configuration.getFieldDefinitions().getFieldDefinition(localName);
            }
            if (def != null) {
                Class name = def.getClazz();
                if (name.getName().contains("XML")) {
                    xmlField = false;
                }
            } else {
                String val = CacheConstants.FIORANO_XML_DELIMITER + "{/";
                if (uri != null && uri.trim().length() != 0)
                    val = val + uri + ":";
                val = val + localName + "}" + CacheConstants.FIORANO_XML_DELIMITER;
                valueContent = (valueContent == null) ? val : valueContent + val;
                return;
            }
        }
        if (state == KEYS_STATE) {
            if (keys == null) { //initialize lazily
                keys = new FieldList();
            }
            try {
                IField keyField = FieldFactory.createField(configuration.getFieldDefinitions().getFieldDefinition(localName), valueContent);
                keys.add(keyField);
            } catch (CacheException e) {
                logger.log(Level.INFO, RBUtil.getMessage(com.fiorano.services.cache.engine.Bundle.class, com.fiorano.services.cache.engine.Bundle.PARSE_FAILED), e);
            }
        } else if (state == DATA_STATE) {
            if (keys != null && !keys.isEmpty()) {
                if (data == null) { //initialize lazily
                    data = new HashMap<String, IField>();
                }
                try {
                    IField dataField = FieldFactory.createField(configuration.getFieldDefinitions().getFieldDefinition(localName), valueContent);
                    data.put(localName, dataField);
                } catch (CacheException e) {
                    logger.log(Level.INFO, RBUtil.getMessage(com.fiorano.services.cache.engine.Bundle.class, com.fiorano.services.cache.engine.Bundle.PARSE_FAILED), e);
                }
            } else {
                logger.log(Level.INFO, RBUtil.getMessage(com.fiorano.services.cache.engine.Bundle.class, com.fiorano.services.cache.engine.Bundle.NO_KEYS_SKIP_DATA));
            }
        } else if (state != UNUSED_STATE) {
            valueContent = null;
        }
    }

    /**
     * Adds an observer to the set of observers for this object, provided
     * that it is not the same as some observer already in the set.
     * The order in which notifications will be delivered to multiple
     * observers is not specified. See the class comment.
     *
     * @param o an observer to be added.
     * @throws NullPointerException if the parameter o is null.
     */
    public synchronized void addObserver(Observer o) {
        notifier.addObserver(o);
    }

    /**
     * Deletes an observer from the set of observers of this object.
     *
     * @param o the observer to be deleted.
     */
    public synchronized void deleteObserver(Observer o) {
        notifier.deleteObserver(o);
    }

    /**
     * Clears the observer list so that this object no longer has any observers.
     */
    public synchronized void deleteObservers() {
        notifier.deleteObservers();
    }

    private static class Notifier extends Observable {
        public void notifyObservers(Object arg) {
            setChanged();
            super.notifyObservers(arg);
        }
    }

}
