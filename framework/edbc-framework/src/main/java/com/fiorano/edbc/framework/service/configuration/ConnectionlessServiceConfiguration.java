/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.services.common.annotations.NamedConfiguration;
import com.fiorano.services.common.configuration.ConnectionPoolConfiguration;
import com.fiorano.services.common.configuration.EncryptDecryptElements;
import com.fiorano.services.common.configuration.ThreadPoolConfiguration;
import com.fiorano.services.common.configuration.XSLConfiguration;
import com.fiorano.services.common.monitor.configuration.MonitoringConfiguration;
import com.fiorano.services.common.schema.SchemaUtil;
import com.fiorano.services.common.transaction.config.TransactionConfiguration;
import com.fiorano.util.ErrorListener;
import com.fiorano.util.lang.ClassUtil;
import fiorano.esb.common.ESBException;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.LoggerUtil;
import fiorano.esb.util.SchemaRef;
import fiorano.esb.utils.BeanUtils;
import fiorano.tifosi.util.schemarepo.SchemaRepositoryException;
import fiorano.tifosi.util.schemarepo.SchemaRepositoryHandler;

import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * <code>ConnectionlessServiceConfiguration</code> should be extended by services which do not
 * connect to an EIS. This class provided some common properties which might be of use in a service
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @fiorano.xmbean
 */
public abstract class ConnectionlessServiceConfiguration implements IServiceConfiguration {

    protected boolean inputValidationEnabled = false;
    protected boolean batchMode = false;
    protected int batchSize = 1;
    protected AbstractErrorHandlingConfiguration errorHandlingConfiguration;
    protected int version = 1033;
    protected int configuredVersion = -1;
    protected Logger passwordEncLogger;
    protected TransactionConfiguration transactionConfiguration = new TransactionConfiguration();
    protected boolean processMessageBasedOnProperty = false;
    protected String propertyName = "PROCESS_MESSAGE_PROPERTY";
    protected String propertyValue;

    protected XSLConfiguration inputXSLConfiguration = new XSLConfiguration();
    protected XSLConfiguration outputXSLConfiguration = new XSLConfiguration();
    protected ThreadPoolConfiguration threadPoolConfiguration = new ThreadPoolConfiguration();
    protected ConnectionPoolConfiguration connectionPoolConfig = new ConnectionPoolConfiguration();
    protected Collection<SchemaRef> schemaRefs;
    private boolean storeImportedSchemas;
    private EncryptDecryptElements inputElementsToEncrypt;
    private EncryptDecryptElements outputElementsToEncrypt;
    protected MonitoringConfiguration monitoringConfiguration = new MonitoringConfiguration();


    protected ConnectionlessServiceConfiguration() {
        errorHandlingConfiguration = new ConnectionlessErrorHandlingConfiguration();
    }

    /**
     * @jmx.managed-attribute access="read-write" description="input_xsl_desc"
     * @jmx.descriptor name="displayName" value="input_xsl_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.services.common.editors.xsl.XSLConfigurationEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.editors.XSLConfigurationEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public XSLConfiguration getInputXSLConfiguration() {
        return inputXSLConfiguration;
    }

    /**
     * set Input XSLConfiguration. This xsl configuration is applied on the incoming request before processing
     *
     * @jmx.managed-attribute
     */
    public void setInputXSLConfiguration(XSLConfiguration inputXSLConfiguration) {
        this.inputXSLConfiguration = inputXSLConfiguration;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="output_xsl_desc"
     * @jmx.descriptor name="displayName" value="output_xsl_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.services.common.editors.xsl.XSLConfigurationEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.editors.XSLConfigurationEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public XSLConfiguration getOutputXSLConfiguration() {
        return outputXSLConfiguration;
    }

    /**
     * set Output XSLConfiguration. This xsl configuration is applied on response after processing.
     *
     * @jmx.managed-attribute
     */
    public void setOutputXSLConfiguration(XSLConfiguration outputXSLConfiguration) {
        this.outputXSLConfiguration = outputXSLConfiguration;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="process_message_desc"
     * @jmx.descriptor name="displayName" value="process_message_name"
     * @jmx.descriptor name="defaultValue" value="false"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidesProperties" value="true"
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public boolean isProcessMessageBasedOnProperty() {
        return processMessageBasedOnProperty;
    }

    /**
     * sets whether to ignore a message if it contains a specific property
     *
     * @jmx.managed-attribute
     */
    public void setProcessMessageBasedOnProperty(boolean processMessageBasedOnProperty) {
        this.processMessageBasedOnProperty = processMessageBasedOnProperty;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="process_property_name_desc"
     * @jmx.descriptor name="displayName" value="process_property_name_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * sets property name which needs to be checked to ignore messages
     *
     * @jmx.managed-attribute
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="process_property_value_desc"
     * @jmx.descriptor name="displayName" value="process_property_value_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * sets property name which needs to be checked to ignore messages
     *
     * @jmx.managed-attribute
     */
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * Returns whether the validation of input is enabled or not. If set to true the input should be validated against the schema, if any, provided.
     * In Fiorano SOA the schema is provided on the input port of the component
     *
     * @jmx.managed-attribute access="read-write" description="input_validation_enabled_desc"
     * @jmx.descriptor name="displayName" value="input_validation_enabled_name"
     * @jmx.descriptor name="defaultValue" value="true"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     * @see com.fiorano.edbc.framework.service.engine.AbstractRequestProcessor
     */
    public boolean isInputValidationEnabled() {
        return inputValidationEnabled;
    }

    /**
     * sets whether the validation of input is enabled or not. If this property is set to true and schema is provided to
     * {@link com.fiorano.edbc.framework.service.engine.AbstractRequestProcessor} input message will be validated against the schema provided
     *
     * @jmx.managed-attribute
     */
    public void setInputValidationEnabled(boolean inputValidationEnabled) {
        this.inputValidationEnabled = inputValidationEnabled;
    }

    /**
     * Returns whether response should be sent in a single message or not.
     *
     * @jmx.managed-attribute access="read-write" description="Send response messages in batches"
     * @jmx.descriptor name="displayName" value="Batch Mode"
     * @jmx.descriptor name="defaultValue" value="false"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="true"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="refresh" value="true"
     */
    public boolean isBatchMode() {
        return batchMode;
    }

    /**
     * sets whether response should be sent in a single message or not
     *
     * @jmx.managed-attribute
     */
    public void setBatchMode(boolean singleBatchMode) {
        this.batchMode = singleBatchMode;
    }

    /**
     * Returns the number of output nodes in each message. This is applicable usually when schema has repeating elements.
     *
     * @jmx.managed-attribute access="read-write" description="Batch size of responses"
     * @jmx.descriptor name="displayName" value="Batch Size"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="true"
     * @jmx.descriptor name="expert" value="true"
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * sets number of repeating elements to be sent in a single output message
     *
     * @jmx.managed-attribute
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="Create parallel request processors in a single session"
     * @jmx.descriptor name="index" value="22"
     * @jmx.descriptor name="displayName" value="Enable Thread Pool"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="hidden" value="true"
     * @jmx.descriptor name="hidesProperties" value="true"
     */
    public boolean isEnableThreadPool() {
        return threadPoolConfiguration.isEnableThreadPool();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setEnableThreadPool(boolean enableThreadPool) {
        threadPoolConfiguration.setEnableThreadPool(enableThreadPool);
    }

    /**
     * @return list of elements in output xml to encrypt
     * @jmx.managed-attribute access="read-write" description="threadpool_configuration_desc"
     * @jmx.descriptor name="displayName" value="threadpool_configuration"
     * @jmx.descriptor name="index" value="23"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.editors.ThreadPoolEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     * @jmx.descriptor name="refresh" value="true"
     */
    @NamedConfiguration
    public ThreadPoolConfiguration getThreadPoolConfiguration() {
        return threadPoolConfiguration;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setThreadPoolConfiguration(ThreadPoolConfiguration threadPoolConfiguration) {
        this.threadPoolConfiguration = threadPoolConfiguration;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="Number of parallel request processors in a single session"
     * @jmx.descriptor name="displayName" value="Pool Size"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="expert" value="true"
     */
    @Override
    public int getPoolSize() {
        return threadPoolConfiguration.getPoolSize();
    }

    /**
     * @jmx.managed-attribute
     */
    @Override
    public void setPoolSize(int poolSize) {
        threadPoolConfiguration.setPoolSize(poolSize);
    }

    /**
     * @jmx.managed-attribute access="read-write" description="Interval at which responses are sent even if batch size is not reached"
     * @jmx.descriptor name="displayName" value="Batch Eviction Interval (in ms)"
     * @jmx.descriptor name="index" value="0"
     * @jmx.descriptor name="expert" value="true"
     */
    @Override
    public long getBatchEvictionInterval() {
        return threadPoolConfiguration.getBatchEvictionInterval();
    }

    /**
     * @jmx.managed-attribute
     */
    @Override
    public void setBatchEvictionInterval(long batchEvictionInterval) {
        threadPoolConfiguration.setBatchEvictionInterval(batchEvictionInterval);
    }

    /**
     * @return connectionPoolConfig
     * @jmx.managed-attribute access="read-write" description="conn_pool_config_desc"
     * @jmx.descriptor name="displayName" value="conn_pool_config_name"
     * @jmx.descriptor name="index" value="25"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.services.common.editors.pool.ConnectionPoolConfigEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.swt.editor.ConnectionPoolConfigEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     * @jmx.descriptor name="hidden" value="true"
     */
    public ConnectionPoolConfiguration getConnectionPoolConfiguration() {
        return connectionPoolConfig;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setConnectionPoolConfiguration(ConnectionPoolConfiguration connectionPoolConfig) {
        this.connectionPoolConfig = connectionPoolConfig;
    }

    /**
     * Returns the actions to be taken when an exception occurs.
     *
     * @jmx.managed-attribute access="read-write" description="error_handling_configuration_desc"
     * @jmx.descriptor name="displayName" value="error_handling_configuration_name"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.edbc.framework.service.cps.ErrorHandlingActionsEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.editors.ErrorActionsEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public AbstractErrorHandlingConfiguration getErrorHandlingConfiguration() {
        return errorHandlingConfiguration;
    }

    /**
     * set ErrorHandlingConfiguration which contains actions to be taken when an exception occurs
     *
     * @jmx.managed-attribute
     */
    public void setErrorHandlingConfiguration(AbstractErrorHandlingConfiguration errorHandlingConfiguration) {
        this.errorHandlingConfiguration = errorHandlingConfiguration;
    }

    /**
     * Monitoring configuration containing details of how monitoring is enabled.
     *
     * @return
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="true"
     */
    public MonitoringConfiguration getMonitoringConfiguration() {
        return monitoringConfiguration;
    }

    public void setMonitoringConfiguration(MonitoringConfiguration monitoringConfiguration) {
        this.monitoringConfiguration = monitoringConfiguration;
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

    /**
     * @jmx.managed-operation descriptor="get Hidden Properties"
     */
    public List<String> fetchHiddenProperties() {
        List<String> propertiesToHide = new ArrayList<>();
        if (!isBatchMode()) {
            propertiesToHide.add("BatchSize");
        }

        if (!isProcessMessageBasedOnProperty()) {
            propertiesToHide.add("PropertyName");
            propertiesToHide.add("PropertyValue");
        }

        if (!("ESTUDIO".equals(System.getProperty("LAUNCHER")) && !"true".equals(System.getProperty("IS_EXTERNAL_CPS")))) {
            propertiesToHide.add("ElementsToDecrypt");
            propertiesToHide.add("ElementsToEncrypt");
            propertiesToHide.add("InputElementsToEncrypt");
            propertiesToHide.add("OutputElementsToEncrypt");
            propertiesToHide.add("ThreadPoolConfiguration");
            if (!isEnableThreadPool()) {
                propertiesToHide.add("PoolSize");
                propertiesToHide.add("BatchEvictionInterval");
            }
        } else {
            propertiesToHide.add("EnableThreadPool");
            propertiesToHide.add("PoolSize");
            propertiesToHide.add("BatchEvictionInterval");
        }

        return propertiesToHide;
    }

    /**
     * service connfiguration should not override this.
     *
     * @return a string representation of ConnectionlessServiceConfiguration.
     */
    public final String toString() {
        return super.toString();
    }

    /**
     * @jmx.managed-operation description="Validates Configuration Properties"
     * @jmx.managed-parameter name="listener" type="com.fiorano.util.ErrorListener" description="Listens for errors occured during validation"
     */
    public void validate(ErrorListener listener) throws ServiceConfigurationException {
        if (errorHandlingConfiguration != null) {
            errorHandlingConfiguration.validate(listener);
        }
    }

    public abstract void test() throws ServiceException;

    /**
     * @jmx.managed-attribute access="read-write" description="vesrion"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="true"
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @return Integer
     * @jmx.managed-attribute access="read-write" description="Configured version"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="true"
     */
    public Integer getConfiguredVersion() {
        return configuredVersion;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setConfiguredVersion(Integer version) {
        this.configuredVersion = version;
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            String xml = BeanUtils.serialiseBean(this);
            return BeanUtils.deserialiseBean(xml);
        } catch (ESBException e) {
            Throwable linkedException = e.getLinkedException();
            if (linkedException == null) {
                linkedException = e;
            }
            throw new CloneNotSupportedException(linkedException.getMessage());
        }
    }

    public Collection<SchemaRef> getSchemaReferences() {
        Set<SchemaRef> set = new HashSet<>();
        if (schemaRefs == null) return set;
        for (SchemaRef schemaRef : schemaRefs)
            set.add(schemaRef);
        return set;
    }

    //port schema - schema generated on ports
    //model schema - schema provided in the cps
    public String addImportedSchemasToRepo(ESBRecordDefinition portSchema, String serviceInstanceName, String modelSchema) {

        if (!portSchema.hasImportedStructures()) {
            return null;
        }
        if (schemaRefs == null) {
            schemaRefs = new ArrayList<>();
        }
        Map<String, String> newLoc;
        try {
            newLoc = SchemaRepositoryHandler.getInstance().addServiceSchemas(portSchema, serviceInstanceName);
            updateSchemaRefs(newLoc);

            portSchema.setStructure(SchemaUtil.updateImports(portSchema.getStructure(), newLoc));
            if (modelSchema != null) {
                modelSchema = SchemaUtil.updateImports(modelSchema, newLoc);
            }
        } catch (SchemaRepositoryException e) {
            e.printStackTrace();
        }
        return modelSchema;
    }

    protected void updateSchemaRefs(Map<String, String> newLoc) {
        for (Map.Entry<String, String> newLocationEntry : newLoc.entrySet()) {
            SchemaRef schemaRef = new SchemaRef();
            schemaRef.setNamespace(newLocationEntry.getKey());
            schemaRef.setLocation(newLocationEntry.getValue());
            schemaRef.setRequiredAtRuntime(isInputValidationEnabled());
            schemaRefs.add(schemaRef);
        }
    }

    /**
     * @return Store Imported Schemas
     * @jmx.managed-attribute access="read-write" description="store_imported_schemas_desc"
     * @jmx.descriptor name="displayName" value="store_imported_schemas_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     * @jmx.descriptor name="hidden" value="true"
     */
    public boolean isStoreImportedSchemas() {
        return storeImportedSchemas;
    }

    /**
     * @param storeImportedSchemas Store Imported Schemas
     * @jmx.managed-attribute
     */
    public void setStoreImportedSchemas(boolean storeImportedSchemas) {
        this.storeImportedSchemas = storeImportedSchemas;
    }

    /**
     * @return list of elements in input xml to decrypt
     * @jmx.managed-attribute access="read-write" description="elems_to_decrypt_desc"
     * @jmx.descriptor name="displayName" value="elems_to_decrypt_name"
     * @jmx.descriptor name="index" value="234"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="true"
     * @jmx.descriptor name="legalValues" value="decrypt"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.swt.editor.XSDStructureEditor"
     * @jmx.descriptor name="bundleName" value="fiorano.esb.adapter.jca.cci.Bundle"
     */
    public EncryptDecryptElements getElementsToDecrypt() {
        return inputElementsToEncrypt;
    }

    /**
     * @param elementsToDecrypt list of elements in input xml to decrypt
     * @jmx.managed-attribute
     */
    public void setElementsToDecrypt(EncryptDecryptElements elementsToDecrypt) {
        this.inputElementsToEncrypt = elementsToDecrypt;
    }

    /**
     * @return list of elements in input xml to decrypt
     * @jmx.managed-attribute access="read-write" description="input_elems_to_encrypt_desc"
     * @jmx.descriptor name="displayName" value="input_elems_to_encrypt_name"
     * @jmx.descriptor name="index" value="236"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="true"
     * @jmx.descriptor name="legalValues" value="input"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.swt.editor.XSDStructureEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public EncryptDecryptElements getInputElementsToEncrypt() {
        return inputElementsToEncrypt;
    }

    /**
     * @param elementsToDecrypt list of elements in input xml to decrypt
     * @jmx.managed-attribute
     */
    public void setInputElementsToEncrypt(EncryptDecryptElements elementsToDecrypt) {
        this.inputElementsToEncrypt = elementsToDecrypt;
    }

    /**
     * @return list of elements in output xml to encrypt
     * @jmx.managed-attribute access="read-write" description="elems_to_encrypt_desc"
     * @jmx.descriptor name="displayName" value="elems_to_encrypt_name"
     * @jmx.descriptor name="index" value="235"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="true"
     * @jmx.descriptor name="legalValues" value="encrypt"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.swt.editor.XSDStructureEditor"
     * @jmx.descriptor name="bundleName" value="fiorano.esb.adapter.jca.cci.Bundle"
     */
    public EncryptDecryptElements getElementsToEncrypt() {
        return outputElementsToEncrypt;
    }

    /**
     * @param elementsToEncrypt list of elements in output xml to encrypt
     * @jmx.managed-attribute
     */
    public void setElementsToEncrypt(EncryptDecryptElements elementsToEncrypt) {
        this.outputElementsToEncrypt = elementsToEncrypt;
    }

    /**
     * @return list of elements in output xml to encrypt
     * @jmx.managed-attribute access="read-write" description="output_elems_to_encrypt_desc"
     * @jmx.descriptor name="displayName" value="output_elems_to_encrypt_name"
     * @jmx.descriptor name="index" value="237"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="hidden" value="true"
     * @jmx.descriptor name="legalValues" value="output"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.swt.editor.XSDStructureEditor"
     * @jmx.descriptor name="bundleName" value="com.fiorano.edbc.framework.service.configuration.Bundle"
     */
    public EncryptDecryptElements getOutputElementsToEncrypt() {
        return outputElementsToEncrypt;
    }

    /**
     * @param elementsToEncrypt list of elements in output xml to encrypt
     * @jmx.managed-attribute
     */
    public void setOutputElementsToEncrypt(EncryptDecryptElements elementsToEncrypt) {
        this.outputElementsToEncrypt = elementsToEncrypt;
    }

    public TransactionConfiguration getTransactionConfiguration() {
        return transactionConfiguration;
    }

    public void setTransactionConfiguration(TransactionConfiguration transactionConfiguration) {
        this.transactionConfiguration = transactionConfiguration;
    }


    public void encryptPasswords() {
    }

    public void decryptPasswords() {
    }

    public void setVersionInfo() {
        setConfiguredVersion(getVersion());
    }

    public void applyPasswordEncLogger(String cfName, String guid) {
        passwordEncLogger = LoggerUtil.getServiceLogger("COM.FIORANO.SERVICES.CUSTOMENCRYPTION", cfName, guid);
    }
}
