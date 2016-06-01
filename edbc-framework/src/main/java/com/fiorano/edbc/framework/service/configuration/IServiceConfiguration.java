/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.services.common.configuration.EncryptDecryptElements;
import com.fiorano.services.common.transaction.config.TransactionConfiguration;
import fiorano.esb.util.SchemaRef;

import java.net.URL;
import java.util.Collection;

/**
 * <code>IServiceConfiguration</code> should be implemented by class which holds configuration of the service.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @see ConnectionlessServiceConfiguration
 * @see ServiceConfiguration
 */
public interface IServiceConfiguration extends IServiceConfigurationDetail {

    /**
     * Returns the actions to be taken when an exception occurs.
     *
     * @return Configuration for handling exceptions
     */
    AbstractErrorHandlingConfiguration getErrorHandlingConfiguration();

    /**
     * Set ErrorHandlingConfiguration which contains actions to be taken when an exception occurs
     *
     * @param errorHandlingConfiguration Configuration for handling exceptions
     */
    void setErrorHandlingConfiguration(AbstractErrorHandlingConfiguration errorHandlingConfiguration);

    /**
     * Returns a copy of service configuration
     *
     * @return a copy of service configuration
     * @throws CloneNotSupportedException
     */
    Object clone() throws CloneNotSupportedException;

    /**
     * Returns whether the validation of input is enabled or not. If set to true the input should be validated against the schema, if any, provided.
     * In Fiorano SOA the schema is provided on the input port of the component
     *
     * @see com.fiorano.edbc.framework.service.engine.AbstractRequestProcessor
     */
    boolean isInputValidationEnabled();

    /**
     * Sets whether the validation of input is enabled or not. If this property is set to true and schema is provided to
     * {@link com.fiorano.edbc.framework.service.engine.AbstractRequestProcessor} input message will be validated against the schema provided
     */
    void setInputValidationEnabled(boolean inputValidationEnabled);

    /**
     * Returns whether response should be sent in a single message or not.
     */
    boolean isBatchMode();

    /**
     * Sets whether response should be sent in a single message or not
     */
    void setBatchMode(boolean singleBatchMode);

    /**
     * Returns the number of output nodes in each message. This is applicable usually when schema has repeating elements.
     */
    int getBatchSize();

    /**
     * Sets number of repeating elements to be sent in a single output message
     */
    void setBatchSize(int batchSize);

    /**
     * Returns URL of helpset containing help for the component
     */
    URL fetchHelpSetURL();

    /**
     * Returns the helpset name
     *
     * @return name of help set.
     */
    String getHelpSetName();

    TransactionConfiguration getTransactionConfiguration();

    void setTransactionConfiguration(TransactionConfiguration transactionConfiguration);

    Collection<SchemaRef> getSchemaReferences();

    boolean isStoreImportedSchemas();

    void setStoreImportedSchemas(boolean storeImportedSchemas);

    EncryptDecryptElements getElementsToDecrypt();

    void setElementsToDecrypt(EncryptDecryptElements elementsToDecrypt);

    EncryptDecryptElements getElementsToEncrypt();

    void setElementsToEncrypt(EncryptDecryptElements elementsToEncrypt);

    void encryptPasswords();

    void decryptPasswords();

    void applyPasswordEncLogger(String cfName, String guid);

    Integer getVersion();

    Integer getConfiguredVersion();

    void setConfiguredVersion(Integer version);

    void setVersionInfo();

    boolean isEnableThreadPool();

    void setEnableThreadPool(boolean enableThreadPool);

    int getPoolSize();

    void setPoolSize(int poolSize);

    long getBatchEvictionInterval();

    void setBatchEvictionInterval(long batchEvictionInterval);
}
