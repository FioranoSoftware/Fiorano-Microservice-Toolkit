/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal.transport.jms;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.Module;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.edbc.framework.service.internal.engine.IRequestValidator;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.common.xmlsecurity.encryption.XMLEncrypter;

import java.util.logging.Level;

public class AbstractSyncIORequestProcessor extends Module implements IRequestProcessor<String, String> {

    private IServiceConfiguration configuration;
    private XMLEncrypter encrypter;

    protected AbstractSyncIORequestProcessor(IModule parent, IServiceConfiguration configuration) {
        super(parent);
        this.configuration = configuration;
    }

    public String process(String request) throws ServiceExecutionException {
        return null;
    }

    public IRequestValidator<String> getRequestValidator() {
        return null;
    }

    protected String decryptMessage(String inputMessage) {

        if (configuration.getElementsToDecrypt() != null) {

            if (encrypter == null) {
                encrypter = new XMLEncrypter();
                encrypter.setLogger(logger);
            }

            try {
                inputMessage = encrypter.decrypt(XMLEncrypter.encryptionConfiguration, inputMessage, configuration.getElementsToDecrypt().getElements(),
                        configuration.getElementsToDecrypt().getNamespaces());
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.DECRYPTION_FAILED));
            }
        }

        return inputMessage;
    }

    protected String encryptMessage(String outputMessage) {

        if (configuration.getElementsToEncrypt() != null) {

            if (encrypter == null) {
                encrypter = new XMLEncrypter();
                encrypter.setLogger(logger);
            }

            try {
                outputMessage = encrypter.encrypt(XMLEncrypter.encryptionConfiguration, outputMessage, configuration.getElementsToEncrypt().getElements(),
                        configuration.getElementsToEncrypt().getNamespaces());
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.DECRYPTION_FAILED));
            }
        }

        return outputMessage;
    }
}
