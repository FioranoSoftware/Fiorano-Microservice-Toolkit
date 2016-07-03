/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.peer.PeerCommunicationsManager;
import com.fiorano.edbc.framework.service.internal.transport.ITransportProvider;
import com.fiorano.edbc.framework.service.internal.transport.jms.JMSTransportProvider;
import com.fiorano.esb.util.CommandLineParameters;
import com.fiorano.microservice.common.port.JNDILookupHelper;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.JavaUtil;
import fiorano.esb.util.ESBConstants;

import javax.naming.NamingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Nov 11, 2010
 * Time: 11:09:30 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ServiceLauncher implements IServiceLauncher {

    protected IService service;
    protected JNDILookupHelper lookupHelper;
    protected CommandLineParameters launchConfiguration;
    protected ITransportProvider transportProvider;
    protected PeerCommunicationsManager peerCommunicationsManager;

    //-----------------------------------[Launcher API]-------------------------------
    public final void launch(String[] args) throws ServiceException {
        Logger logger = null;
        try {
            launchConfiguration = new CommandLineParameters(args);
            lookupHelper = createLookupHelper(launchConfiguration);
            service = createService();
            transportProvider = createTransportProvider();
            peerCommunicationsManager = createPeerCommunicationsManager((JMSTransportProvider) transportProvider);//this should checked if transport provider is not JMS
            service.initialize(launchConfiguration, transportProvider, peerCommunicationsManager);
            logger = service.getLogger();
            transportProvider.setLogger(logger);
            peerCommunicationsManager.setLogger(logger);

            transportProvider.create();
            peerCommunicationsManager.create();
            transportProvider.start();
            peerCommunicationsManager.start();
            service.configure();
            if (!launchConfiguration.isInmemoryLaunchable()) {
                try {
                    logger.log(Level.INFO, RBUtil.getMessage(Bundle.class, Bundle.PROCESS_ID, new Object[]{JavaUtil.getPID()}));
                } catch (Exception e) {
                    logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_LOGGING_PROCESS_ID, new Object[]{e.getMessage()}));
                }
            }
            for (int i = 0; i < args.length; i++) {
                if (ESBConstants.PASSWORD.equals(args[i])) {
                    i++;
                } else {
                    logger.info(RBUtil.getMessage(Bundle.class, Bundle.RT_ARGS, new Object[]{i, args[i]}));
                }
            }

            service.create();
            service.start();
        } catch (ServiceException e) {
            if (logger != null) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } else if (service != null && service.getLogger() != null) {
                service.getLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            terminate();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private JNDILookupHelper createLookupHelper(CommandLineParameters launchConfiguration) {
        return new JNDILookupHelper(launchConfiguration);
    }

    public final void terminate() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // not this method may get called twice in case of timeouts so proper null
                    // checks have to be added in case they are made eligible for GC(nullified).
                    try {
                        if (lookupHelper != null) {
                            lookupHelper.close();
                        }
                    } catch (NamingException e) {
                        service.getLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), e);
                    } finally {
                        lookupHelper = null;
                    }
                    try {
                        service.stop();
                        peerCommunicationsManager.stop();
                        transportProvider.stop();
                        service.destroy();
                        peerCommunicationsManager.destroy();
                        transportProvider.destroy();
                        service.destroyLogHandlers();
                    } catch (ServiceExecutionException e) {
                        service.getLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), e);
                    } finally {
                        exit(-2);
                    }
                } catch (Throwable ex) {
                    if(service.getLogger() != null)
                        service.getLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.EXCEPTION_ON_STOP), ex);
                }
            }
        }).start();
    }

    protected void exit(int value) {
        System.exit(value);
    }

    protected ITransportProvider createTransportProvider() {
        return new JMSTransportProvider(launchConfiguration, lookupHelper);
    }

    protected PeerCommunicationsManager createPeerCommunicationsManager(JMSTransportProvider transportProvider) {
        return new PeerCommunicationsManager(transportProvider, service);
    }

    protected abstract IService createService();
}
