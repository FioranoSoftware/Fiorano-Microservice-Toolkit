/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.engine;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.services.common.configuration.EncryptDecryptElements;
import com.fiorano.services.common.configuration.XSLConfiguration;
import com.fiorano.services.common.transformation.EDBCXslTransformer;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.common.xmlsecurity.encryption.XMLEncrypter;
import com.fiorano.util.StringUtil;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.MessageUtil;
import fiorano.esb.utils.XSDValidator;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>AbstractRequestProcessor</code> implements validation of request. Classes extending <code>AbstractRequestProcessor</code>
 * should provide implementation for {@link #process(String)} and {@link  #process(Object)}.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class AbstractRequestProcessor implements IRequestProcessor {
    protected boolean inputValidationEnabled = true;
    protected ESBRecordDefinition schema;
    protected IServiceConfiguration serviceConfiguration;
    protected Logger logger;
    private XMLEncrypter encrypter;

    private EDBCXslTransformer inputTransformer;
    private EDBCXslTransformer outputTransformer;


    /**
     * Creates a request processor with given <code>schema</code>, <code>logger</code> and <code>serviceConfiguration</code>
     *
     * @param schema               XML schema / dtd with which input should be validated
     * @param logger               logger used for logging, if this is <code>null</code> a logger with package name of implementing class will be used
     * @param serviceConfiguration configuration details of the service
     */
    public AbstractRequestProcessor(ESBRecordDefinition schema, Logger logger, IServiceConfiguration serviceConfiguration) {
        this.schema = schema;
        this.serviceConfiguration = serviceConfiguration;
        this.inputValidationEnabled = (serviceConfiguration != null) ? (serviceConfiguration.isInputValidationEnabled()) : (schema != null);
        if (logger != null) {
            this.logger = logger;
        } else {
            this.logger = Logger.getLogger(this.getClass().getPackage().getName());
        }
    }

    public final void validate(String request) throws ServiceExecutionException {
        if (!inputValidationEnabled) {
            return;
        }
        if (schema == null || schema.getStructure() == null) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.NO_SCHEMA_TO_VALIDATE));
            return;
        }
        try {
            if (request == null) {
                throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_REQUEST), ServiceErrorID.INVALID_REQUEST_ERROR);
            }
            XSDValidator.validateXML(schema, request);
        } catch (Exception e) {
            throw new ServiceExecutionException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_REQUEST), e, ServiceErrorID.INVALID_REQUEST_ERROR);
        }
    }

    public Message process(Message request) throws ServiceExecutionException {
        try {
            MessageUtil.makeMessageReadWrite(request);
            return request;
        } catch (JMSException e) {
            throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
        }
    }

    protected String decryptMessage(String inputMessage, EncryptDecryptElements decryptElements) {

        if (decryptElements != null) {

            if (encrypter == null) {
                encrypter = new XMLEncrypter();
                encrypter.setLogger(logger);
            }

            try {
                inputMessage = encrypter.decrypt(XMLEncrypter.encryptionConfiguration, inputMessage, decryptElements.getElements(),
                        decryptElements.getNamespaces());
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.DECRYPTION_FAILED));
            }
        }

        return inputMessage;
    }

    public boolean IsIgnoreRequest(Message request) throws JMSException {
        if (serviceConfiguration instanceof ConnectionlessServiceConfiguration) {
            ConnectionlessServiceConfiguration configuration = (ConnectionlessServiceConfiguration) serviceConfiguration;
            String configured = configuration.getPropertyValue();
            String received = request.getStringProperty(configuration.getPropertyName());
            return configuration.isProcessMessageBasedOnProperty() && !StringUtil.isEmpty(configured) && !configured.equals(received);
        } else {
            return false;
        }
    }

    public void transformRequest(Message requestMessage) throws ServiceExecutionException {

        if (inputTransformer != null) {
            try {
                inputTransformer.invokeTransformer(requestMessage);
                return;
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        }
        XSLConfiguration inpXslConfiguration = null;
        if (serviceConfiguration instanceof ConnectionlessServiceConfiguration)
            inpXslConfiguration = ((ConnectionlessServiceConfiguration) serviceConfiguration).getInputXSLConfiguration();
        if (inpXslConfiguration != null && !StringUtil.isEmpty(inpXslConfiguration.getXslValue())) {
            try {
                inputTransformer = new EDBCXslTransformer(inpXslConfiguration, logger);
                inputTransformer.invokeTransformer(requestMessage);
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        }
    }

    public void transformResponse(Message response) throws ServiceExecutionException {

        if (outputTransformer != null) {
            try {
                outputTransformer.invokeTransformer(response);
                return;
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        }
        XSLConfiguration outputXslConfiguration = null;
        if (serviceConfiguration instanceof ConnectionlessServiceConfiguration)
            outputXslConfiguration = ((ConnectionlessServiceConfiguration) serviceConfiguration).getOutputXSLConfiguration();
        if (outputXslConfiguration != null && !StringUtil.isEmpty(outputXslConfiguration.getXslValue())) {
            try {
                outputTransformer = new EDBCXslTransformer(outputXslConfiguration, logger);
                outputTransformer.invokeTransformer(response);
            } catch (Exception e) {
                throw new ServiceExecutionException(e, ServiceErrorID.REQUEST_EXECUTION_ERROR);
            }
        }
    }

    protected Message decryptMessage(Message inputMessage, EncryptDecryptElements decryptElements) {

        if (decryptElements != null) {

            if (encrypter == null) {
                encrypter = new XMLEncrypter();
                encrypter.setLogger(logger);
            }

            try {
                String inputMsgTxt = MessageUtil.getTextData(inputMessage);
                inputMsgTxt = encrypter.decrypt(XMLEncrypter.encryptionConfiguration, inputMsgTxt, decryptElements.getElements(),
                        decryptElements.getNamespaces());
                MessageUtil.setTextData(inputMessage, inputMsgTxt);
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.DECRYPTION_FAILED));
            }
        }

        return inputMessage;
    }

    protected String encryptMessage(String outputMessage, EncryptDecryptElements encryptElements) {

        if (encryptElements != null) {

            if (encrypter == null) {
                encrypter = new XMLEncrypter();
                encrypter.setLogger(logger);
            }

            try {
                outputMessage = encrypter.encrypt(XMLEncrypter.encryptionConfiguration, outputMessage, encryptElements.getElements(),
                        encryptElements.getNamespaces());
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ENCRYPTION_FAILED));
            }
        }

        return outputMessage;
    }

    protected Message encryptMessage(Message outputMessage, EncryptDecryptElements encryptElements) {

        if (encryptElements != null) {

            if (encrypter == null) {
                encrypter = new XMLEncrypter();
                encrypter.setLogger(logger);
            }

            try {
                String outputMsgTxt = MessageUtil.getTextData(outputMessage);
                outputMsgTxt = encrypter.encrypt(XMLEncrypter.encryptionConfiguration, outputMsgTxt, encryptElements.getElements(),
                        encryptElements.getNamespaces());
                MessageUtil.setTextData(outputMessage, outputMsgTxt);
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ENCRYPTION_FAILED));
            }
        }

        return outputMessage;
    }

    protected void updateNamedConfiguration(String namedConfig) {
    }
}