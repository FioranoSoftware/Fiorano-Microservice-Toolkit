/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.feeder.model;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.feeder.cps.swing.panels.Bundle;
import com.fiorano.uif.xml.util.XMLValidator;
import com.fiorano.util.ErrorListener;
import fiorano.esb.record.ESBRecordDefinition;

import java.util.Hashtable;
import java.util.Map;


/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created April 18, 2005
 * @fiorano.xmbean
 * @jmx.mbean
 */
public class FeederPM extends ConnectionlessServiceConfiguration{
    public static final int TEXT = 0;
    public static final int XML = 1;

    private String defaultMessage = "Input Text";
    private int messageFormat = XML; //default
    private Map header;
    private Map attachment;
    private int historySize = 10;
    private ESBRecordDefinition schema;

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public int getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(int messageFormat) {
        this.messageFormat = messageFormat;
    }

    public Map getHeader() {
        return header;
    }

    public void setHeader(Map header) {
       this. header = header;
    }

    public Map getAttachment() {
        return attachment;
    }

    public void setAttachment(Map attachment) {
        this.attachment = attachment;
    }

    public int getHistorySize() {
        return historySize;
    }

    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }

    public ESBRecordDefinition getSchema() {
        return schema;
    }

    public void setSchema(ESBRecordDefinition schema) {
        this.schema = schema;
    }

    public static int convertFormat(String property) {
        return "XML".equals(property) ? XML : TEXT;
    }

    @Override
    public void test() throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAsFormattedString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Validates the configuration parameters in the panel.
     *
     * @param listener error listener used to notify errors
     * @throws ServiceConfigurationException if there is any exception in validation
     */
    public void validate(ErrorListener listener) throws ServiceConfigurationException {
        //Validation not required during runtime.
        //if(messageFormat == TEXT)
        //    return;
        //validateSchema(getSchema());
    }

    public void validateSchema(ESBRecordDefinition recordDef) throws ServiceConfigurationException {

        if (recordDef == null) {
            return;
        }

        try {
            String schema = recordDef.getStructure();

            if (recordDef.getDefinitionType() == ESBRecordDefinition.DTD) {
                XMLValidator.validateDTD(schema);
            } else {
                Map imports = recordDef.getImportedStructures();
                if (imports != null) {
                    //use this object to set to the resource
                    Hashtable importedXSDs = new Hashtable();
                    for (Object entryObj : imports.entrySet()) {
                        Map.Entry entry = (Map.Entry) entryObj;
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
                    XMLValidator.validateSchema1(schema, importedXSDs);
                } else {
                    XMLValidator.validateSchema1(schema);
                }
            }
        }
        catch (Exception e) {
            throw new ServiceConfigurationException(RBUtil.getMessage(
                    Bundle.class, Bundle.SCHEMA_VALIDATION_FAILED, new Object[] {e.getMessage()}),
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }
}