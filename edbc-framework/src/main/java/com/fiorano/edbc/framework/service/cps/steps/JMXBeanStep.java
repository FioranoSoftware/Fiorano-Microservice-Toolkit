/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps.steps;

import com.fiorano.edbc.framework.service.cps.Bundle;
import com.fiorano.edbc.framework.service.cps.Logging;
import com.fiorano.edbc.framework.service.cps.ValidationErrorListener;
import com.fiorano.jmx.api.JMXBean;
import com.fiorano.jmx.api.JMXOperation;
import com.fiorano.jmx.api.JMXOperations;
import com.fiorano.jmx.nodes.JMXBeanNode;
import com.fiorano.openide.explorer.propertysheet.XPropertySheetView;
import com.fiorano.openide.windows.Explorer;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.swing.layout.EqualsLayout;
import com.fiorano.util.ExceptionUtil;
import fiorano.tifosi.dmi.application.ServiceInstance;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

import javax.help.DefaultHelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.logging.Level;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 * @bundle $class.title=Configure Component
 * @bundle $class.summary=Configuration options for Component.
 */
public class JMXBeanStep extends WizardStep implements WizardDescriptor.FinishablePanel, Logging {
    public static final Object[] OBJECT = new Object[0];
    public static final ValidationErrorListener ERROR_LISTENER = new ValidationErrorListener();
    protected JMXBean jmxBean = null;
    protected Component panel;
    private boolean readOnly = false;
    private String instanceName = null;
    private ServiceInstance serviceInstance;

    public JMXBeanStep(JMXBean configBean, boolean readOnly) {
        this(configBean, readOnly, null, null);
    }

    public JMXBeanStep(JMXBean configBean, boolean readOnly, String instanceName, ServiceInstance serviceInstance) {
        this.jmxBean = configBean;
        this.readOnly = readOnly;
        this.instanceName = instanceName;
        this.serviceInstance = serviceInstance;
    }

    public boolean isFinishPanel() {
        return true;
    }

    public JMXBean getJmxBean() {
        return jmxBean;
    }

    public Component createComponent() {
        try {
            panel = createPanel();
        } catch (PropertyVetoException ex) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.FAILED_TO_CREATE_CPS_UI), ex);
        }

        return panel;
    }

    public void model2Component() throws PropertyVetoException {
        ExplorerManager manager = ExplorerManager.find(panel);
        Node node = new CustomJMXBeanNode();
        manager.setRootContext(node);
        manager.setExploredContextAndSelection(node, new Node[]{node});
    }

    public void fastValidate() throws WizardValidationException {
        if (readOnly) {
            throw new WizardValidationException(null, RBUtil.getMessage(Bundle.class, Bundle.READ_ONLY_WIZARD), null);
        }
    }

    public void lazyValidate() throws WizardValidationException {
        try {
            validateData();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            throw new WizardValidationException(null, RBUtil.getMessage(Bundle.class, Bundle.CONFIG_INVALID), null);
        }
    }

    public void testConnection() throws Exception {
        JMXOperation testMethod = findOperation("test"); //NOI18N
        if (testMethod != null) {
            testMethod.invoke(OBJECT);
        }
    }

    public void validateData() throws Exception {
        JMXOperation validateMethod = findOperation("validate"); //NOI18N
        if (validateMethod != null) {
            ERROR_LISTENER.reset();
            validateMethod.invoke(new Object[]{ERROR_LISTENER});
            if (ERROR_LISTENER.isErrorOccured()) {
                if (ERROR_LISTENER.getException() != null) {
                    throw ERROR_LISTENER.getException();
                } else {
                    throw new Exception(RBUtil.getMessage(Bundle.class, Bundle.CONFIG_INVALID));
                }
            }
        }
    }

    protected Component createPanel() throws PropertyVetoException {
        WizardPanel panel = new WizardPanel();
        panel.setNode(new CustomJMXBeanNode());
        return panel;
    }

    protected JMXOperation findOperation(String name) {
        JMXOperations ops = (JMXOperations) jmxBean.getChildren().get(1);

        for (Object o : ops.getChildren()) {
            JMXOperation operation = (JMXOperation) o;
            if (operation.getName().equals(name)) {
                return operation;
            }
        }
        return null;
    }

    class WizardPanel extends JPanel {
        Explorer explorer = new Explorer();
        JPanel validatePanel;

        public WizardPanel() {
            super(new BorderLayout(0, 5));

            XPropertySheetView psheet = new XPropertySheetView();

            explorer.add(psheet);

            validatePanel = new JPanel(new EqualsLayout(2));
            JButton helpBtn = new JButton("Help");
            initHelp(helpBtn);
            validatePanel.add(helpBtn);
            // show validate always.
            validatePanel.add(new JButton(new ValidateAction()));
            if (findOperation("test") != null) {
                validatePanel.add(new JButton(new TestAction()));
            }
            explorer.add(validatePanel, BorderLayout.SOUTH);

            add(explorer, BorderLayout.CENTER);
        }

        private void initHelp(JButton helpBtn) {
            JMXOperation helpOperation = findOperation("fetchHelpSetURL");
            boolean isHelpAvailable = false;
            if (helpOperation != null) {
                try {
                    URL helpUrl = (URL) helpOperation.invoke(OBJECT);
                    if (helpUrl != null) {
                        ClassLoader classLoader = getClass().getClassLoader();
                        helpUrl = HelpSet.findHelpSet(classLoader, getHelpSetName(helpUrl));
                        HelpSet helpSet = new HelpSet(classLoader, helpUrl);
                        if (serviceInstance != null) {
                            URL usageUrl = getClass().getResource("/help/" + serviceInstance.getGUID() + "_Usage.hs");

                            String usageUrlName = getHelpSetName(usageUrl);
                            if (usageUrlName != null) {
                                usageUrl = HelpSet.findHelpSet(classLoader, usageUrlName);
                            }
                            // For components with only static help
                            if (usageUrl != null && !(helpUrl.equals(usageUrl))) {
                                helpSet.add(new HelpSet(getClass().getClassLoader(), usageUrl));
                            }
                        }
                        DefaultHelpBroker helpBroker = new DefaultHelpBroker();
                        helpBroker.setHelpSet(helpSet);
                        if (helpSet.getHomeID() != null) {
                            helpBroker.enableHelpOnButton(helpBtn, helpSet.getHomeID().getIDString(), helpSet);
                            isHelpAvailable = true;
                        }
                    }
                } catch (Exception e) {
                }
            }
            if (!isHelpAvailable) {
                helpBtn.setAction(
                        new AbstractAction("Help") {
                            public void actionPerformed(ActionEvent e) {
                                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "No help available");
                            }
                        }
                );
            }
        }

        private String getHelpSetName(URL url) {

            if (url == null) {
                return null;
            }
            String path = url.getPath();
            return path.substring(path.indexOf('!') + 2);
        }

        public void setNode(Node node) throws PropertyVetoException {
            explorer.getExplorerManager().setRootContext(node);
            try {
                explorer.getExplorerManager().setExploredContextAndSelection(node, new Node[]{node});
            } catch (PropertyVetoException ex) {
                ExceptionUtil.printStackTrace(ex);
            }
        }
    }

    class ValidateAction extends AbstractAction {
        public ValidateAction() {
            super(RBUtil.getMessage(Bundle.class, Bundle.VALIDATE));
        }

        public void actionPerformed(ActionEvent ae) {
            try {
                validateData();
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), RBUtil.getMessage(Bundle.class, Bundle.VALIDATION_SUCCESSFUL));
            } catch (Exception e) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.CONFIG_INVALID));
            }
        }
    }

    class TestAction extends AbstractAction {
        public TestAction() {
            super(RBUtil.getMessage(Bundle.class, Bundle.TEST));
        }

        public void actionPerformed(ActionEvent ae) {
            try {
                testConnection();
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), RBUtil.getMessage(Bundle.class, Bundle.TEST_SUCCESSFUL));
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
    }

    private class CustomJMXBeanNode extends JMXBeanNode {
        public CustomJMXBeanNode() {
            super(jmxBean);
        }

        public String getDisplayName() {
            return instanceName != null ? instanceName : super.getDisplayName();
        }
    }
}
