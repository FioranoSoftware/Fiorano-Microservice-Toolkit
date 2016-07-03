/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import com.fiorano.edbc.framework.service.cps.steps.JMXBeanStep;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.jmx.api.JMXBean;
import com.fiorano.openide.module.OpenIDEModule;
import com.fiorano.openide.wizard.StaticWizardIterator;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.common.util.RBUtil;
import fiorano.management.FBean;
import fiorano.tifosi.common.TifosiException;
import org.netbeans.core.startup.Main;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

import javax.management.*;
import javax.naming.Context;
import java.awt.*;
import java.util.logging.Level;

/**
 * <code>JMXPropertySheet</code> provides implementation to launch Custom Property Sheet for configuring service.
 * Service should extend this class and override {@link #getDefaultConfiguration()} that will be shown when launched
 * for the first time.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class JMXPropertySheet extends WizardPropertySheet {

    public JMXPropertySheet() {
        System.setProperty("COMPONENT_CPS", "true");
    }

    /**
     * launches Custom Property Sheet of service
     *
     * @param isRunning  indicates whether the service is launched
     * @param isReadOnly indicates whether the user has sufficient privileges to modify configuration of service
     * @throws TifosiException
     */
    public void show(boolean isRunning, boolean isReadOnly) throws TifosiException {

        setConfigurationSerializer(createConfigurationSerializer());
        CPSESBUtil cpsesbUtil = getCPSESBUtil(isRunning, isReadOnly);

        boolean cpsesbUtilRegistered = false;
        String domainName = CPSESBUtil.DOMAIN_NAME;
        MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer(domainName);
        try {
            CPSESBUtil.registerCPSESBUtil(mbeanServer, cpsesbUtil);
            cpsesbUtilRegistered = true;
        } catch (Exception ex) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.CPS_REGISTER_FAILED), ex);
        }

        try {
            initializeConfiguration();
        } catch (ServiceConfigurationException e) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.DESERIALIZATION_FAILED), e);
        }

        ObjectInstance objectInstance;
        ObjectName objName;
        try {
            FBean fBean = new FBean(configuration);
            objName = new ObjectName(domainName, "name", configuration.toString()); //NOI18N
            objectInstance = mbeanServer.registerMBean(fBean, objName);
        } catch (NotCompliantMBeanException e) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.CPS_REGISTER_FAILED), e);
        } catch (MBeanRegistrationException e) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.CPS_REGISTER_FAILED), e);
        } catch (MalformedObjectNameException e) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.CPS_REGISTER_FAILED), e);
        } catch (InstanceAlreadyExistsException e) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.CPS_REGISTER_FAILED), e);
        } catch (Exception ex) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.CPS_REGISTER_FAILED), ex);
        }

        if (System.getProperty("HTMLAdapter") != null) {
            startHTMLAdapter(mbeanServer, domainName);
        }

        initializeNetBeans();
        initializeJNDI();

        JMXBean jmxBean = new JMXBean(mbeanServer, objectInstance);
        WizardDescriptor.Iterator iter = createIterator(jmxBean);

        String title = getWindowTitle(isRunning, isReadOnly);
        String cfName = getApplicationPropertySheet().getGUID() + "__" + String.valueOf(getApplicationPropertySheet().getVersion()).replace('.', '_') + "__" + getServiceInstanceName();
        configuration.applyPasswordEncLogger(cfName, getServiceInstance().getGUID());
        if (getPsValue() != null) {
            configuration.decryptPasswords();
        }

        WizardDescriptor wizrdDescriptor = getWizardDescriptor(iter, title);

        final Dialog dlg = DialogDisplayer.getDefault().createDialog(wizrdDescriptor);

        showDialog(dlg, title);

        Object result = wizrdDescriptor.getValue();
        boolean finished = (result == WizardDescriptor.FINISH_OPTION || (result.getClass().getName().indexOf("FinishAction") != -1));
        onClose(finished, configuration, cpsesbUtil);
        try {
            // if no changes in cps data, dont write service.tmp file.
            if (finished) {
                saveConfiguration();
            } else {
                setPsValue(null);
            }
        } catch (ServiceConfigurationException ex) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.SERIALIZATION_FAILED), ex);
        }

        try {
            mbeanServer.unregisterMBean(objName);
        } catch (InstanceNotFoundException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.CONFIG_OBJECT_UNREGISTER_FAILED), e);
        } catch (MBeanRegistrationException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.CONFIG_OBJECT_UNREGISTER_FAILED), e);
        }
        if (cpsesbUtilRegistered) {
            try {
                CPSESBUtil.unregisterCPSESBUtil(mbeanServer);
            } catch (Exception e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.CPS_UNREGISTER_FAILED), e);
            }
        }

        if (isInFEPOJVM()) {
            dlg.dispose();
        }

    }

    /**
     * Services willing to take some action before CPS is closed should override this. Typcically operations
     * like creating ports or setting schemas on ports can be done here.
     *
     * @param finished      indicates whether CPS is finished or cancelled
     * @param configuration configuration of service
     * @param cpsesbUtil    {@link CPSESBUtil}
     */
    protected void onClose(boolean finished, Object configuration, CPSESBUtil cpsesbUtil) {
        System.getProperties().remove("COMPONENT_CPS");
    }

    private void initializeNetBeans() {
        Main.registerPropertyEditors();
        OpenIDEModule.registerPropertyEditors();
    }

    private void initializeJNDI() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
    }

    public WizardDescriptor.Iterator createIterator(JMXBean jmxBean) {
        return new StaticWizardIterator(new WizardStep[]{
                new JMXBeanStep(jmxBean, getIsRunning(), getServiceInstanceName(), getServiceInstance())
        });
    }

    private void startHTMLAdapter(MBeanServer mbeanServer, String domain) {
        try {
            Object obj = Class.forName("com.sun.jdmk.comm.HtmlAdaptorServer").newInstance(); //NOI18N
            //com.sun.jdmk.comm.HtmlAdaptorServer hadaptor = new com.sun.jdmk.comm.HtmlAdaptorServer();
            ObjectName adaptorName = new ObjectName(domain + ":name=HtmlProtocolAdaptor,port=8082"); //NOI18N
            mbeanServer.registerMBean(obj, adaptorName);
            obj.getClass().getMethod("start", null).invoke(obj, null); //NOI18N
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

}
