/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import com.fiorano.edbc.framework.service.configuration.ErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.RetryAction;
import com.fiorano.edbc.framework.service.exception.RetryConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.services.common.util.RBUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * Date: Mar 19, 2007
 * Time: 10:52:35 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ErrorHandlingActionsPanel extends JPanel {

    private ServiceErrorID errorID;
    private Collection checkBoxes = new ArrayList();
    private RetryPanel retryPanel = null;

    private ErrorHandlingActionsPanel() {
        this(null, null);
    }

    public ErrorHandlingActionsPanel(ServiceErrorID errorID, Set actions) {
        super(new GridBagLayout());
        this.errorID = errorID;
        if (errorID != null) {
            createUI(errorID, actions);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();

        }
        final JDialog dlg = new JDialog();
        dlg.getContentPane().setLayout(new FlowLayout());
        ErrorHandlingConfiguration configuration = new ErrorHandlingConfiguration();
        final Map errorHandlingActionsMap = configuration.getErrorActionsMap();
        if (errorHandlingActionsMap != null) {
            Iterator errorHandlingActionsIterator = errorHandlingActionsMap.entrySet().iterator();
            while (errorHandlingActionsIterator.hasNext()) {
                Map.Entry errorHandlingActionMapping = (Map.Entry) errorHandlingActionsIterator.next();
                dlg.getContentPane().add(
                        new ErrorHandlingActionsPanel((ServiceErrorID) errorHandlingActionMapping.getKey(), (Set) errorHandlingActionMapping.getValue()));
            }
        }

//        configuration.addOtherAction("abc", new Integer(2));
//        configuration.addOtherAction("abcd", new Integer(2));

        dlg.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        dlg.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                System.exit(-1);
            }

            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                dlg.dispose();
            }
        });
        dlg.pack();
        dlg.show();
    }

    public ServiceErrorID getErrorID() {
        return errorID;
    }

    public Set getActions() {
        Iterator iterator = checkBoxes.iterator();
        Set actions = new HashSet();
        while (iterator.hasNext()) {
            ErrorHandlingActionCheckBox checkBox = (ErrorHandlingActionCheckBox) iterator.next();
            ErrorHandlingAction action = checkBox.getErrorHandlingAction();
            action.setEnabled(checkBox.isSelected());
            if (action instanceof RetryAction) {
                ((RetryAction) action).setConfiguration(retryPanel.getConfiguration());
            }
            actions.add(action);
        }
        return actions;
    }

    private void createUI(ServiceErrorID errorID, Set actions) {
        setBorder(BorderFactory.createTitledBorder(RBUtil.getMessage(Bundle.class, Bundle.REMEDIAL_ACTIONS)));
        if (actions != null) {
            int i = 0;
            Iterator actionIterator = actions.iterator();
            while (actionIterator.hasNext()) {
                ErrorHandlingAction handlingAction = (ErrorHandlingAction) actionIterator.next();
                ErrorHandlingActionCheckBox actionCheckBox = new ErrorHandlingActionCheckBox(handlingAction);
                actionCheckBox.setSelected(!handlingAction.isEnabled());
                actionCheckBox.setSelected(handlingAction.isEnabled());
                checkBoxes.add(actionCheckBox);
                add(actionCheckBox, new GridBagConstraints(0, i++,
                        1, 1,
                        1, 0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0),
                        0, 0));
            }
            add(retryPanel == null ? new JPanel() : retryPanel, new GridBagConstraints(0, i++,
                    1, 1,
                    1, 1,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0),
                    0, 0));
            Iterator actionCheckBoxes = checkBoxes.iterator();
            if (retryPanel != null) {
                while (actionCheckBoxes.hasNext()) {
                    final ErrorHandlingActionCheckBox actionCheckBox = (ErrorHandlingActionCheckBox) actionCheckBoxes.next();
                    if (actionCheckBox.getErrorHandlingAction().getId() == ErrorHandlingAction.LOG
                            || actionCheckBox.getErrorHandlingAction().getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                        actionCheckBox.addItemListener(new ItemListener() {
                            public void itemStateChanged(ItemEvent e) {
                                retryPanel.enableAction(actionCheckBox.getErrorHandlingAction(), e.getStateChange() == ItemEvent.SELECTED);
                            }
                        });
                    }
                }
            }
        }
    }

    private RetryPanel createRetryPanel(RetryConfiguration retryConfiguration) {
        if (retryPanel == null) {
            retryPanel = new RetryPanel(retryConfiguration);
            retryPanel.setBorder(BorderFactory.createTitledBorder(RBUtil.getMessage(Bundle.class, Bundle.RETRY_CONFIGURATION)));
        }
        return retryPanel;
    }

    private class ErrorHandlingActionCheckBox extends JCheckBox {
        ErrorHandlingAction errorHandlingAction;

        public ErrorHandlingActionCheckBox(ErrorHandlingAction errorHandlingAction) {
            super(errorHandlingAction.getName(), errorHandlingAction.isEnabled());
            if (errorHandlingAction instanceof RetryAction) {
                RetryConfiguration retryConfiguration = ((RetryAction) errorHandlingAction).getConfiguration();
                createRetryPanel(retryConfiguration);
                addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        retryPanel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                    }
                });
            }
            setToolTipText(errorHandlingAction.getDescription());
            this.errorHandlingAction = errorHandlingAction;
        }

        public ErrorHandlingAction getErrorHandlingAction() {
            return errorHandlingAction;
        }
    }

}
