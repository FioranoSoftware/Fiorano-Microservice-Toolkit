/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import com.fiorano.edbc.framework.service.configuration.AbstractErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.configuration.ConnectionlessErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.configuration.ErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.RetryAction;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.swing.tree.XTree;
import com.fiorano.tree.string.StringConvertor;
import com.fiorano.util.ErrorListener;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * Date: Mar 19, 2007
 * Time: 1:01:36 AM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ErrorHandlingPanel extends JPanel implements EnhancedCustomPropertyEditor {

    private static final String DEFAULT_DESCRIPTION = "Choose the error and its corresponding remedial actions";
    private static final String EMPTY_PANEL = "";
    private JSplitPane rootPane = new JSplitPane();
    //Error tree
    private DefaultMutableTreeNode errorsRootNode;
    private XTree errorsTree;
    private JScrollPane errosTreeScrollPane;
    //Description
    private JTextPane descriptionPane;
    private JPanel descriptionPanel;
    //Error handling actions
    private CardLayout errorHandlingActionsLayout;
    private JPanel errorHandlingActionsPanel;
    private Collection errorHandlingActionPanels;
    private JPanel rightPanel;
    private Class errorConfigurationClass;


    public ErrorHandlingPanel() {
        super(new GridBagLayout());
    }

    public ErrorHandlingPanel(AbstractErrorHandlingConfiguration configuration) {
        super(new GridBagLayout());
        initUI();
        if (configuration != null) {
            errorConfigurationClass = configuration.getClass();
        }
        createUI(configuration);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();

        }
        final JDialog dlg = new JDialog();
        dlg.getContentPane().setLayout(new BorderLayout());
        ErrorHandlingConfiguration configuration = new ErrorHandlingConfiguration();
        final ErrorHandlingPanel comp = new ErrorHandlingPanel(configuration);
        dlg.getContentPane().add(comp);
        dlg.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dlg.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
//                System.out.println(comp.getConfiguration());
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

    private void initUI() {
        //Error tree
        errorsRootNode = new DefaultMutableTreeNode(RBUtil.getMessage(Bundle.class, Bundle.ERRORS));
        errorsTree = new XTree(new DefaultTreeModel(errorsRootNode));
        errosTreeScrollPane = new JScrollPane(errorsTree);

        //Description
        descriptionPane = new JTextPane();
        descriptionPanel = new JPanel(new BorderLayout());

        //Error handling actions
        errorHandlingActionsLayout = new CardLayout();
        errorHandlingActionsPanel = new JPanel(errorHandlingActionsLayout);
        errorHandlingActionPanels = new ArrayList();

        rightPanel = new JPanel(new GridBagLayout());
        errorConfigurationClass = ConnectionlessErrorHandlingConfiguration.class;
    }

    public Object getPropertyValue() throws IllegalStateException {
        AbstractErrorHandlingConfiguration configuration = getConfiguration();
        try {
            configuration.validate(new ValidationErrorListener());
        } catch (ServiceConfigurationException exception) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exception);
            throw new IllegalStateException();
        }
        return configuration;
    }

    public AbstractErrorHandlingConfiguration getConfiguration() {
        AbstractErrorHandlingConfiguration configuration = null;
        try {
            configuration = (AbstractErrorHandlingConfiguration) errorConfigurationClass.newInstance();
        } catch (InstantiationException e) {
            configuration = new ConnectionlessErrorHandlingConfiguration();
        } catch (IllegalAccessException e) {
            configuration = new ConnectionlessErrorHandlingConfiguration();
        }
        Map errorActionsMap = configuration.getErrorActionsMap();
        Iterator iterator = errorHandlingActionPanels.iterator();
        while (iterator.hasNext()) {
            ErrorHandlingActionsPanel errorHandlingPanel = (ErrorHandlingActionsPanel) iterator.next();
            Collection newActions = errorHandlingPanel.getActions();
            Collection oldActions = (Collection) errorActionsMap.get(errorHandlingPanel.getErrorID());
            if (newActions != null) {
                for (Iterator iterator1 = newActions.iterator(); iterator1.hasNext(); ) {
                    ErrorHandlingAction newAction = (ErrorHandlingAction) iterator1.next();
                    if (oldActions != null) {
                        for (Iterator iterator2 = oldActions.iterator(); iterator2.hasNext(); ) {
                            ErrorHandlingAction oldAction = (ErrorHandlingAction) iterator2.next();
                            if (oldAction.equals(newAction)) {
                                oldAction.setEnabled(newAction.isEnabled());
                                if (oldAction instanceof RetryAction) {
                                    ((RetryAction) oldAction).setConfiguration(((RetryAction) newAction).getConfiguration());
                                }
                            }
                        }
                    } else {
                        errorActionsMap.put(errorHandlingPanel.getErrorID(), errorHandlingPanel.getActions());
                    }
                }
            }
        }
        return configuration;
    }

    public void setConfiguration(AbstractErrorHandlingConfiguration configuration) {
        initUI();
        if (configuration != null) {
            errorConfigurationClass = configuration.getClass();
        }
        createUI(configuration);
    }

    private void createUI(AbstractErrorHandlingConfiguration configuration) {
        createErrorTree(configuration);
        createDescriptionPanel();
        createErrorHandlingActionsPanel(configuration);
        rightPanel.add(descriptionPanel, new GridBagConstraints(0, 0,
                1, 1,
                1, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0),
                0, 0));
        rightPanel.add(errorHandlingActionsPanel, new GridBagConstraints(0, 1,
                1, 1,
                1, 1,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0, 0));
        rootPane.setForeground(Color.black);
        rootPane.setBorder(BorderFactory.createEtchedBorder());
        rootPane.setDividerSize(5);
        errosTreeScrollPane.setMinimumSize(new Dimension(140, 200));
        rootPane.add(errosTreeScrollPane, JSplitPane.LEFT);
        rootPane.add(rightPanel, JSplitPane.RIGHT);
        add(rootPane, new GridBagConstraints(0, 0,
                1, 1,
                1, 1,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0, 0));
    }

    private void createErrorHandlingActionsPanel(AbstractErrorHandlingConfiguration configuration) {
        if (configuration == null || configuration.getErrorActionsMap() == null) {
            return;
        }
        errorHandlingActionPanels.clear();
        final Map errorHandlingActionsMap = configuration.getErrorActionsMap();
        if (errorHandlingActionsMap != null) {
            Iterator errorHandlingActionsIterator = errorHandlingActionsMap.entrySet().iterator();
            while (errorHandlingActionsIterator.hasNext()) {
                Map.Entry errorHandlingActionMapping = (Map.Entry) errorHandlingActionsIterator.next();
                ServiceErrorID errorID = (ServiceErrorID) errorHandlingActionMapping.getKey();
                JPanel panel = new ErrorHandlingActionsPanel(errorID, (Set) errorHandlingActionMapping.getValue());
                errorHandlingActionsPanel.add(panel, errorID.getName());
                errorHandlingActionPanels.add(panel);
            }
        }
        errorHandlingActionsPanel.add(new JPanel(), EMPTY_PANEL);
        errorHandlingActionsLayout.show(errorHandlingActionsPanel, EMPTY_PANEL);
    }

    private void createDescriptionPanel() {
        descriptionPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
        descriptionPane.setOpaque(false);
        descriptionPane.setEditable(false);
        descriptionPane.setContentType("text/plain");
        descriptionPane.setText(DEFAULT_DESCRIPTION);
        descriptionPanel.setBackground(SystemColor.info);
        descriptionPanel.add(descriptionPane, BorderLayout.CENTER);
        descriptionPanel.add(new JSeparator(), BorderLayout.SOUTH);
    }

    private void createErrorTree(AbstractErrorHandlingConfiguration configuration) {
        errosTreeScrollPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        errosTreeScrollPane.setAutoscrolls(true);
        errorsTree.setOpaque(false);
        DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
        cellRenderer.setBackgroundNonSelectionColor(errosTreeScrollPane.getBackground());
        errorsTree.setCellRenderer(cellRenderer);
        errorsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        errorsTree.setRootVisible(true);
        errorsTree.putClientProperty(XTree.STRING_CONVERTOR, new StringConvertor() {
            public String toString(Object object) {
                if (object == null) {
                    return null;
                }
                Object userObject;
                if (!(object instanceof DefaultMutableTreeNode)) {
                    return object.toString();
                }
                userObject = ((DefaultMutableTreeNode) object).getUserObject();
                return userObject instanceof ServiceErrorID ? ((ServiceErrorID) userObject).getName() : userObject.toString();
            }
        });
        if (configuration == null || configuration.getErrorActionsMap() == null) {
            return;
        }
        Iterator errorIDsIterator = configuration.getErrors().iterator();
        while (errorIDsIterator.hasNext()) {
            ServiceErrorID errorID = (ServiceErrorID) errorIDsIterator.next();
            DefaultMutableTreeNode errorNode = new DefaultMutableTreeNode(errorID);
            errorsRootNode.add(errorNode);
        }
        errorsTree.expandAll();
        errorsTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath currentSelectionPath = e.getNewLeadSelectionPath();
                if (currentSelectionPath != null && currentSelectionPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
                    Object currentNode = ((DefaultMutableTreeNode) currentSelectionPath.getLastPathComponent()).getUserObject();
                    if (currentNode instanceof ServiceErrorID) {
                        descriptionPane.setText(((ServiceErrorID) currentNode).getDescription());
                        errorHandlingActionsLayout.show(errorHandlingActionsPanel, ((ServiceErrorID) currentNode).getName());
                        return;
                    }
                }
                descriptionPane.setText(DEFAULT_DESCRIPTION);
                errorHandlingActionsLayout.show(errorHandlingActionsPanel, EMPTY_PANEL);
            }
        });
    }

    private static class ValidationErrorListener implements ErrorListener {
        public void warning(Exception exception) throws Exception {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exception);
            throw new IllegalStateException();
        }

        public void error(Exception exception) throws Exception {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, exception);
            throw new IllegalStateException();
        }

        public void fatalError(Exception exception) throws Exception {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, exception);
            throw new IllegalStateException();
        }
    }
}
