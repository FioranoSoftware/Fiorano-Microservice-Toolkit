/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.cps;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.cps.steps.NotifiableStep;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.internal.configuration.BeanXMLConfigurationSerializer;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.openide.wizard.StaticWizardIterator;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.openide.wizard.WizardUtil;
import com.fiorano.services.common.swing.Notifier;
import com.fiorano.services.common.util.RBUtil;
import fiorano.esb.util.CPSUtil;
import fiorano.tifosi.common.TifosiException;
import fiorano.tifosi.tps.rtl.TifosiCustomPropertySheet;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

public abstract class WizardPropertySheet extends TifosiCustomPropertySheet {
    protected Logger logger = CPSUtil.getAnonymousLogger();
    protected IServiceConfiguration configuration;
    protected CPSESBUtil cpsESBUtil;
    private WizardStep[] steps = null;
    private IConfigurationSerializer configurationSerializer;

    public void show(boolean isRunning, boolean isReadOnly) throws TifosiException {

        setConfigurationSerializer(createConfigurationSerializer());
        cpsESBUtil = getCPSESBUtil(isRunning, isReadOnly);
        try {
            initializeConfiguration();
        } catch (ServiceConfigurationException e) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.DESERIALIZATION_FAILED), e);
        }

        String title = getWindowTitle(isRunning, isReadOnly);
        String cfName = getApplicationPropertySheet().getGUID() + "__" + String.valueOf(getApplicationPropertySheet().getVersion()).replace('.', '_') + "__" + getServiceInstanceName();
        configuration.applyPasswordEncLogger(cfName, getServiceInstance().getGUID());
        if (getPsValue() != null) {
            configuration.decryptPasswords();
        }
        DialogDescriptor wizrdDescriptor = createUI(title, isReadOnly);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizrdDescriptor);
        Notifier notifier = new Notifier(dialog);
        for (WizardStep step : steps) {
            if (step instanceof NotifiableStep) {
                ((NotifiableStep) step).setNotifier(notifier);
            }
        }

        showDialog(dialog, title);

        Object result = wizrdDescriptor.getValue();
        boolean finished = (result == WizardDescriptor.FINISH_OPTION || (result.getClass().getName().indexOf("FinishAction") != -1));
        onClose(finished, cpsESBUtil);
        try {
            // if no changes in cps data, dont write service.tmp file.
            if (finished) {
                saveConfiguration();
            } else {
                setPsValue(null);
            }
        } catch (ServiceConfigurationException ex) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.SERIALIZATION_FAILED), ex);
        } finally {
            configuration = null;
            cpsESBUtil = null;
        }

    }

    protected WizardDescriptor createUI(String title, boolean readOnly) {
        steps = createSteps(readOnly);
        WizardDescriptor.Iterator iterator = new StaticWizardIterator(steps);
        return getWizardDescriptor(iterator, title);
    }

    protected Object getSettings() {
        return configuration;
    }

    protected WizardStep[] createSteps(boolean readOnly) {
        return new WizardStep[]{};
    }

    /**
     * Returns default configuration that will be shown when Custom Property Sheet is launched for
     * first time
     *
     * @return default configuration for service
     */
    protected abstract IServiceConfiguration getDefaultConfiguration();

    /**
     * Services willing to take some action before CPS is closed should override this. Typcically operations
     * like creating ports or setting schemas on ports can be done here.
     *
     * @param finished   indicates whether CPS is finished or cancelled
     * @param cpsesbUtil {@link CPSESBUtil}
     */
    protected void onClose(boolean finished, CPSESBUtil cpsesbUtil) {
    }

    protected void initializeConfiguration() throws ServiceConfigurationException {
        String serializedConfiguration = getPsValue();
        if ((serializedConfiguration != null) && (serializedConfiguration.trim().length() != 0)) {
            configuration = getConfigurationSerializer().deserializeFromString(serializedConfiguration);
        } else {
            configuration = getDefaultConfiguration();
        }
        applyManageableProperties(configuration);
    }

    protected void saveConfiguration() throws ServiceConfigurationException {
        configuration.encryptPasswords();
        populateManageableProperties(configuration);
        populateSchemaRefs(configuration);
        populateResources(configuration);
        configuration.setConfiguredVersion(configuration.getVersion());
        setPsValue(getConfigurationSerializer().serializeToString(configuration));
    }

    protected final IConfigurationSerializer getConfigurationSerializer() {
        return configurationSerializer;
    }

    protected final void setConfigurationSerializer(IConfigurationSerializer configurationSerializer) {
        this.configurationSerializer = configurationSerializer;
    }

    protected IConfigurationSerializer createConfigurationSerializer() {
        return new BeanXMLConfigurationSerializer();
    }

    protected String getWindowTitle(boolean isRunning, boolean isReadOnly) {
        String title = isReadOnly ? Bundle.READONLY_CPS_WINDOW_TITLE
                : (isRunning ? Bundle.RUNNING_CPS_WINDOW_TITLE : Bundle.CPS_WINDOW_TITLE);
        return RBUtil.getMessage(Bundle.class, title, new Object[]{
                getServiceInstanceName(),
                getServiceInstance().getGUID(),
                new Float(getServiceInstance().getVersion())
        });
    }

    protected CPSESBUtil getCPSESBUtil(boolean isRunning, boolean isReadOnly) {

        CPSESBUtil cpsesbUtil = new CPSESBUtil();

        cpsesbUtil.setApplication(getApplicationPropertySheet());
        cpsesbUtil.setServiceInstance(getServiceInstance());
        cpsesbUtil.setSerializedConfiguration(getPsValue());
        cpsesbUtil.setReadOnly(isReadOnly);
        if (cpsesbUtil.getServiceInstanceAdapter() != null) {
            cpsesbUtil.getServiceInstanceAdapter().setRunning(isRunning);
        }
        String iconFileName = getIconFileName();
        cpsesbUtil.setIconLocation(iconFileName != null ? new File(iconFileName) : null);
        cpsesbUtil.setCpsLaunchedInMemory(isInFEPOJVM());

        return cpsesbUtil;
    }

    protected WizardDescriptor getWizardDescriptor(WizardDescriptor.Iterator iterator, String title) {

        WizardDescriptor wizardDescriptor = WizardUtil.creatorWizardDescriptor(getSettings(), iterator, title, true);
        wizardDescriptor.setModal(true);
        URL resource = getClass().getResource("defaultWizard_studio.gif");
        if (resource != null) {
            WizardUtil.setImage(wizardDescriptor, new ImageIcon(resource).getImage());
        }
        WizardUtil.setContentBackground(wizardDescriptor, new Color(196, 221, 249));

        return wizardDescriptor;
    }

    protected void showDialog(final Dialog dialog, String title) {

        final JFrame frame;

        if (dialog.getOwner() instanceof JFrame) {
            frame = (JFrame) dialog.getOwner();
        } else {
            frame = new JFrame();
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    frame.dispose();
                }
            });
        }

        frame.setTitle(title);
        frame.setLocation(-1000, -1000);
        frame.addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent e) {
                dialog.toFront();
            }

            public void windowLostFocus(WindowEvent e) {
            }
        });

        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            frame.setVisible(true);
        }

        File file = new File(System.getProperty("user.dir") + File.separatorChar + getIconFileName());
        if (file.exists()) {
            Image img = new ImageIcon(file.toString()).getImage();
            frame.setIconImage(img);
            if (dialog.getOwner() != null) {
                ((Frame) dialog.getOwner()).setIconImage(img);
            }
        }

        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

}
