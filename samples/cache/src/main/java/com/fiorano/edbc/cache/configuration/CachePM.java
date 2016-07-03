/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.cache.configuration;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.annotations.NamedConfiguration;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.ErrorListener;
import com.fiorano.util.lang.ClassUtil;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PropertyModel class captures the configuration information from the
 * Custom Property Sheet (CPS) of the service.
 * set methods set the property to the values provided in CPS and get methods
 * can be used to get those values.
 * JMX Naming conventions should be followed for these method names.
 *
 * @fiorano.xmbean
 * @jmx.mbean
 * @jboss.xmbean
 */
public class CachePM extends ConnectionlessServiceConfiguration {

    public static final int LEAST_RECENTLY_ADDED = 0;
    public static final int LEAST_RECENTLY_UPDATED = 1;
    public static final int LEAST_RECENTLY_ACCESSED = 2;
    // This is the field definition table which contains the fields to be part of key and value in the Cache.
    private FieldDefinitions fieldDefinitions;
    // The limit to which the table will hold the data before purging
    private int thresholdSize = -1;
    private int entryRemovalCriteria = LEAST_RECENTLY_ADDED;
    private int initialCapaity = 100;
    private boolean inputValidationEnabled = true;

    public CachePM() {
        for (Object o : errorHandlingConfiguration.getErrorActionsMap().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Set actions = (Set) entry.getValue();
            boolean isJMSError = ServiceErrorID.TRANSPORT_ERROR.equals(entry.getKey());
            for (Object action : actions) {
                ErrorHandlingAction errorHandlingAction = (ErrorHandlingAction) action;
                if (errorHandlingAction.getId() == ErrorHandlingAction.LOG || errorHandlingAction.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                    errorHandlingAction.setEnabled(true);
                }
                if (isJMSError && errorHandlingAction.getId() == ErrorHandlingAction.STOP_SERVICE) {
                    errorHandlingAction.setEnabled(true);
                }
            }
        }
    }

    /**
     * Returns the value of fieldDefinitions
     *
     * @jmx.managed-attribute access="read-write" description="field_definitions_desc"
     * @jmx.descriptor name="displayName" value="field_definitions_name"
     * @jmx.descriptor name="index" value="0"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.services.cache.cps.swing.editors.FieldDefinitionEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cache.cps.editors.FieldDefinitionTablePropertyEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.cache.configuration.Bundle"
     */
    @NamedConfiguration
    public FieldDefinitions getFieldDefinitions() {
        return fieldDefinitions;
    }

    /**
     * Sets the value of fieldDefinitions
     *
     * @jmx.managed-attribute
     */
    public void setFieldDefinitions(FieldDefinitions fieldDefinitions) {
        this.fieldDefinitions = fieldDefinitions;
    }

    /**
     * Returns the value of threshold
     *
     * @jmx.managed-attribute access="read-write" description="threshold_size_desc"
     * @jmx.descriptor name="displayName" value="threshold_size_name"
     * @jmx.descriptor name="defaultValue" value="-1"
     * @jmx.descriptor name="index" value="1"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="hidesProperties" value="true"
     * @jmx.descriptor name="unit" value="entries"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.cache.configuration.Bundle"
     */
    public int getThresholdSize() {
        return thresholdSize;
    }

    /**
     * Sets the value of threshold
     *
     * @jmx.managed-attribute
     */
    public void setThresholdSize(int thresholdSize) {
        this.thresholdSize = thresholdSize;
    }

    /**
     * Returns the criteria for removing the entries when the threshold is reached
     *
     * @jmx.managed-attribute access="read-write" description="entry_removal_criteria_desc"
     * @jmx.descriptor name="displayName" value="entry_removal_criteria_name"
     * @jmx.descriptor name="legalValues" value="Least recently added,Least recently updated,Least recently accessed"
     * @jmx.descriptor name="defaultValue" value="Least recently added"
     * @jmx.descriptor name="index" value="2"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.cache.configuration.Bundle"
     */
    public String getEntryRemovalCriteria() {
        return EntryRemovalCriteriaUtil.getCriteriaAsString(entryRemovalCriteria);
    }

    /**
     * Sets the criteria for removing the entries when the threshold is reached
     *
     * @jmx.managed-attribute
     */
    public void setEntryRemovalCriteria(String entryRemovalCriteria) {
        this.entryRemovalCriteria = EntryRemovalCriteriaUtil.getCriteria(entryRemovalCriteria);
    }

    public int fetchEntryRemovalCriteria() {
        return entryRemovalCriteria;
    }

    /**
     * Returns the size with which the cache storage has to be intialized
     *
     * @jmx.managed-attribute access="read-write" description="initial_capacity_desc"
     * @jmx.descriptor name="displayName" value="initial_capacity_name"
     * @jmx.descriptor name="defaultValue" value="100"
     * @jmx.descriptor name="index" value="3"
     * @jmx.descriptor name="unit" value="entries"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.cache.configuration.Bundle"
     */
    public int getInitialCapaity() {
        return initialCapaity;
    }

    /**
     * Sets the size with which the cache storage has to be intialized
     *
     * @jmx.managed-attribute
     */
    public void setInitialCapaity(int initialCapaity) {
        this.initialCapaity = initialCapaity;
    }

    /**
     * Returns whether the validation of input is enabled or not
     *
     * @jmx.managed-attribute access="read-write" description="input_validation_enabled_desc"
     * @jmx.descriptor name="displayName" value="input_validation_enabled_name"
     * @jmx.descriptor name="defaultValue" value="true"
     * @jmx.descriptor name="index" value="4"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     * @jmx.descriptor name="hidden" value="false"
     */
    public boolean isInputValidationEnabled() {
        return inputValidationEnabled;
    }

    /**
     * sets whether the validation of input is enabled or not
     *
     * @jmx.managed-attribute
     */
    public void setInputValidationEnabled(boolean inputValidationEnabled) {
        this.inputValidationEnabled = inputValidationEnabled;
    }

    /**
     * @jmx.managed-operation descriptor="get Hidden Properties"
     */
    public List<String> fetchHiddenProperties() {
        List<String> listHiddenProps = super.fetchHiddenProperties();

        if (getThresholdSize() <= 0) {
            listHiddenProps.add("EntryRemovalCriteria");
        }

        return listHiddenProps;
    }

    /**
     * Validates the configuration parameters in the panel.
     *
     * @param Listener error listener used to notify errors
     * @throws ServiceConfigurationException if there is any exception in validation
     */
    public void validate(ErrorListener Listener) throws ServiceConfigurationException {
        if (thresholdSize != -1 && thresholdSize <= 0) {
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_THRESHOLD_SIZE,
                    new Object[]{thresholdSize + ""}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
        if (initialCapaity <= 0) {
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_CAPACITY_VALUE,
                    new Object[]{initialCapaity + ""}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
        if (thresholdSize > 0) {
            int entryRemovalCriteria = fetchEntryRemovalCriteria();
            if (entryRemovalCriteria != LEAST_RECENTLY_ADDED
                    && entryRemovalCriteria != LEAST_RECENTLY_ACCESSED
                    && entryRemovalCriteria != LEAST_RECENTLY_UPDATED) {
                throw new ServiceConfigurationException(
                        RBUtil.getMessage(Bundle.class, Bundle.INVALID_ENTRY_REMOVAL_CRITERIA, new Object[]{entryRemovalCriteria + ""}),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
            if (initialCapaity > thresholdSize) {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_CAPACITY_VALUE,
                        new Object[]{initialCapaity + ""}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
        }
        if (fieldDefinitions == null) {
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.NO_FIELDS), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
        try {
            fieldDefinitions.validate();
        } catch (Exceptions serviceConfigurationExceptions) {
            Iterator iterator = serviceConfigurationExceptions.iterator();
            if (iterator.hasNext()) {
                throw (ServiceConfigurationException) iterator.next();
            } else {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_CONFIGURATION),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
        }
    }

    public String getAsFormattedString() {
        return null;
    }

    /**
     * @return help URL
     * @jmx.managed-operation description="Help set URL"
     */
    public URL fetchHelpSetURL() {
        return getClass().getResource(getHelpSetName());
    }

    /**
     * @return name of help set.
     */
    public String getHelpSetName() {
        return ClassUtil.getShortClassName(this.getClass()) + ".hs";
    }

    public void test() {
        throw new UnsupportedOperationException();
    }
}