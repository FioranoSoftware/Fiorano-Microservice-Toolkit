/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.cps.swing.panels;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.esb.wrapper.QPortName;
import com.fiorano.services.cbr.cps.swing.Bundle;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.swing.ConfigurationPanel;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.swing.table.XTable;
import com.fiorano.swing.table.XTableHeader;
import com.fiorano.uif.wizard.ValidationException;
import com.fiorano.util.ErrorListener;
import com.fiorano.xml.xsd.XSDUtil;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CPSUtil;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.IndependentContext;
import net.sf.saxon.trans.XPathException;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.openide.ErrorManager;
import org.w3c.dom.ls.LSResourceResolver;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @bundle $class.title=Routing
 * @bundle $class.summary=Routing Rules
 */
public class XPathConfigurationPanel extends ConfigurationPanel<CBRPropertyModel> {
    public static final String XSLT_TYPE_STR = "XSLT";
    public static final String XPATH_TYPE_STR = "XPATH";
    JCheckBox m_useXPath1_0 = new JCheckBox();
    JLabel dummy = new JLabel();
    JCheckBox chkOnAppCtx = new JCheckBox();
    JLabel processorLabel = new JLabel(RBUtil.getMessage(Bundle.class, Bundle.PROCESSOR));
    private ArrayList xpaths = new ArrayList();
    private ArrayList portNames = new ArrayList();
    private JPanel tableButtonsPanel = new JPanel();
    private JPanel bottomPanel;
    private JScrollPane tableScrollPane = new JScrollPane();
    private JButton deleteButton = new JButton();
    private JButton deleteAllButton = new JButton();
    private CBRPropertyModel model = null;
    private CPSESBUtil cpsesbUtil;
    private LSResourceResolver resolverResolver_;
    private String[] processors = {XPATH_TYPE_STR, XSLT_TYPE_STR};
    private JComboBox processorsCombo = new JComboBox(processors);
    private Logger logger = CPSUtil.getAnonymousLogger();
    private AbstractTableModel tmodel =
            new AbstractTableModel() {
                public int getRowCount() {
                    if (xpaths.size() >= portNames.size()) {
                        return xpaths.size() + 1;
                    } else {
                        return portNames.size() + 1;
                    }
                }

                public int getColumnCount() {
                    return 3;
                }

                public String getColumnName(int col) {
                    switch (col) {
                        case 0:
                            return "#";
                        case 1:
                            return RBUtil.getMessage(Bundle.class, Bundle.PORT_NAME);
                        case 2:
                            return "XPath";
                        default:
                            return "";
                    }
                }

                public Class getColumnClass(int col) {
                    switch (col) {
                        case 0:
                            return Integer.class;
                        case 1:
                            return String.class;
                        case 2:
                            return String.class;
                        default:
                            return String.class;
                    }
                }

                public boolean isCellEditable(int row, int col) {
                    return col != 0;
                }

                public Object getValueAt(int row, int col) {
                    if (col == 0) {
                        return new Integer(row + 1);
                    }
                    if (col == 1) {
                        if (row < portNames.size()) {
                            return portNames.get(row);
                        } else {
                            return null;
                        }
                    }
                    if (row < xpaths.size()) {
                        return xpaths.get(row);
                    } else {
                        return null;
                    }
                }

                public void setValueAt(Object obj, int row, int col) {
                    if (obj != null) {
                        if (col == 2) {
                            if (row < xpaths.size()) {
                                xpaths.set(row, obj);
                            } else {
                                xpaths.add(obj);
                                if (portNames.size() == xpaths.size() - 1) {
                                    portNames.add("OUT_XPATH" + (row + 1));
                                }
                                super.fireTableRowsInserted(row + 1, row + 1);
                            }
                        } else if (col == 1) {
                            if (row < portNames.size()) {
                                portNames.set(row, obj);
                            } else {
                                portNames.add(obj);
                                xpaths.add(new String());
                                super.fireTableRowsInserted(row + 1, row + 1);
                            }
                        }
                    }

                }

            };
    private XTable table = new XTable(tmodel);


    public XPathConfigurationPanel() {
        super(new BorderLayout());
    }

    /**
     * Returns input XS ds for object
     */
    public Map<String, Map<QPortName, ESBRecordDefinition>> getInputXSDs()
            throws Exception {

        return cpsesbUtil.fetchConnectedOutputPortSchemas();

    }

    public String getContextXSD() {
        ESBRecordDefinition context = cpsesbUtil.fetchApplicationContextSchema();
        if (context != null) {
            return context.getStructure();
        }
        return null;
    }

    public ESBRecordDefinition getContextXSDRecord() {
        ESBRecordDefinition context = cpsesbUtil.fetchApplicationContextSchema();
        return context != null ? context : null;
    }

    /**
     * Returns description for object
     *
     * @return String
     */
    public String getDescription() {
        return "specify the xpaths against which messages are validated";
    }


    /**
     * @throws com.fiorano.uif.wizard.ValidationException
     */
    public void validate(ErrorListener errorListener) {
        Exceptions exceptions = new Exceptions();
        try {
            model.validateXPaths(xpaths);
        } catch (ServiceConfigurationException e) {
            exceptions.add(new Exception(e.getMessage()));
        }

        try {
            model.validateReservedPortNames(portNames);
        } catch (ServiceConfigurationException e) {
            exceptions.add(new Exception(e.getMessage()));
        }

        try {
            model.validateDuplicatePortNames(portNames);
        } catch (ServiceConfigurationException e) {
            exceptions.add(new Exception(e.getMessage()));
        }

        try {
            model.validatePortNamesForXPaths(portNames, xpaths);
        } catch (ServiceConfigurationException e) {
            exceptions.add(new Exception(e.getMessage()));
        }

        try {
            model.validateXPATH(xpaths, (String) processorsCombo.getSelectedItem());
        } catch (ServiceConfigurationException e) {
            exceptions.add(new Exception(e.getMessage()));
        }

        try {
            for (Object e : exceptions) {
                errorListener.error((Exception) e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @throws com.fiorano.uif.wizard.ValidationException
     */
    public void validateXPATH(Exceptions exceptions) {
        ArrayList list = xpaths;

        XPathExpression xpaths[] = new XPathExpression[list.size()];
        XPathEvaluator evaluator = new XPathEvaluator();

        if (model.getUseXPath1_0()) {
            //In SAXON, for XPath 1.0 support context.isInBackwardsCompatibleMode
            // should return true.
            evaluator.setStaticContext(
                    new IndependentContext() {
                        public boolean isInBackwardsCompatibleMode() {
                            return true;
                        }
                    });
        }

        //added to resolve Bug #559 - CBR Namespace issue -Priya
        IndependentContext stx = (IndependentContext) evaluator.getStaticContext();
        HashMap namespaces = model.getNamespaces();

        if (namespaces != null) {
            Iterator it = namespaces.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();

                stx.declareNamespace((String) entry.getKey(),
                        (String) entry.getValue());
            }
        }
        //end of code - Bug #559
        for (int i = xpaths.length - 1; i >= 0; i--) {
            String str = (String) list.get(i);

            if (str != null && str.length() > 0) {
                try {
                    xpaths[i] = evaluator.createExpression(str);
                } catch (XPathException e) {
                    logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.INVALID_XPATH), e);
                    exceptions.add(new ValidationException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_XPATH),
                            RBUtil.getMessage(Bundle.class, Bundle.INVALID_XPATH_ROW, new Object[]{(i + 1)}), table));
                }
            }
        }
    }

    public void loadConfiguration(CBRPropertyModel model) {
        this.model = model;
        xpaths = (ArrayList) model.getXPaths().clone();
        portNames = (ArrayList) model.getOutPortNames().clone();
        TableColumn tableColumn = table.getColumnModel().getColumn(2);
        try {
            tableColumn.setCellEditor(new XPathCellEditor(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        m_useXPath1_0.setSelected(model.getUseXPath1_0());
        chkOnAppCtx.setSelected(model.getApplyOnXPath());
        processorsCombo.setSelectedItem(processors[model.getProcessorType()]);
        tmodel.fireTableDataChanged();
        addInputSchemaElementList();
//        table.pack(TablePacker.ALL_ROWS, true);
    }

    public CBRPropertyModel getConfiguration() {
        try {
            model.setXPaths(xpaths);
            model.setOutPortNames(portNames);
            model.setUseXPath1_0(m_useXPath1_0.isSelected());
            model.setApplyOnXPath(chkOnAppCtx.isSelected());
            model.setProcessorType(processorsCombo.getSelectedIndex());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_SAVING_CONFIG), ex);
        }
        return model;

    }


    public void jbInit(CPSESBUtil cpsesbUtil)
            throws Exception {
        this.cpsesbUtil = cpsesbUtil;
        deleteButton.setText(RBUtil.getMessage(Bundle.class, Bundle.DELETE));
        deleteButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteButton_actionPerformed(e);
                    }
                });
        deleteAllButton.setText(RBUtil.getMessage(Bundle.class, Bundle.DELETE_ALL));
        deleteAllButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteAllButton_actionPerformed(e);
                    }
                });

        Border border1 = BorderFactory.createEmptyBorder(0, 10, 0, 0);
        tableButtonsPanel.setBorder(border1);
        m_useXPath1_0.setText(RBUtil.getMessage(Bundle.class, Bundle.USE_XPATH_10));
        chkOnAppCtx.setText(RBUtil.getMessage(Bundle.class, Bundle.APPLY_ON_CTXT));

        tableButtonsPanel.setLayout(new GridBagLayout());
        tableButtonsPanel.add(deleteButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 0), 0, 0));
        tableButtonsPanel.add(deleteAllButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0
                , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

        tableScrollPane.getViewport().add(table, null);
//        panel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(RBUtil.getMessage(Bundle.class, Bundle.ROUTING_RULES)));
        topPanel.add(tableScrollPane, BorderLayout.CENTER/*new GridBagConstraints(0, 0, 3, 1, 0, 0, GridBagConstraints.NORTHWEST,
              GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
              0, 0)*/);
        topPanel.add(tableButtonsPanel, BorderLayout.EAST/*new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
              GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5),
              0, 0)*/);
        int topPanelWidth = (int) topPanel.getPreferredSize().getWidth();
        topPanel.setPreferredSize(new Dimension(topPanelWidth, 300));
        add(topPanel, BorderLayout.NORTH);

        addInputSchemaElementList();
        processorsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                list_actionPerformed(e);
            }
        });
        chkOnAppCtx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chkOnAppCtx_actionPerformed(e);
            }
        });
        bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.add(processorLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
                0, 0));
        bottomPanel.add(processorsCombo, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 2, 0, 10),
                0, 0));
        bottomPanel.add(m_useXPath1_0, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 10, 0, 10),
                0, 0));
//       bottomPanel.add(dummy, new GridBagConstraints(2, 0, 1, 1, 0.5, 0, GridBagConstraints.WEST,
//               GridBagConstraints.NONE, new Insets(5, 0, 5, 5),
//               0, 0));
        bottomPanel.add(chkOnAppCtx, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(5, 0, 5, 5),
                0, 0));
        bottomPanel.setBorder(BorderFactory.createTitledBorder(RBUtil.getMessage(Bundle.class, Bundle.PROCESSING_CONFIG)));
        add(bottomPanel, BorderLayout.CENTER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumn tableColumn = table.getColumnModel().getColumn(2);
        tableColumn.setPreferredWidth((int) (topPanelWidth * .8));
        tableColumn.setCellEditor(new XPathCellEditor(this));
        removeXTableHeaderMouseListener();
    }

    private void removeXTableHeaderMouseListener() {
        MouseListener[] mouseListeners = table.getTableHeader().getMouseListeners();
        for (int iCnt = 0; iCnt < mouseListeners.length; iCnt++) {
            if (mouseListeners[iCnt] instanceof XTableHeader) {
                table.getTableHeader().removeMouseListener(mouseListeners[iCnt]);
            }
        }
    }

    //The up and down buttons were removed as the TDAC does not honour Port Order.

    void deleteButton_actionPerformed(ActionEvent e) {
        int row = table.getSelectedRow();

        if (row < 0 || row >= xpaths.size()) {
            return;
        }
        xpaths.remove(row);
        portNames.remove(row);
        // ((WizardDialog)(panel.getDialog())).validatePanel(panel.getIndex());
        tmodel.fireTableDataChanged();
    }

    void deleteAllButton_actionPerformed(ActionEvent e) {
        xpaths.clear();
        portNames.clear();
        // ((WizardDialog)(panel.getDialog())).validatePanel(panel.getIndex());
        tmodel.fireTableDataChanged();
    }

    void list_actionPerformed(ActionEvent e) {
        String selected = (String) processorsCombo.getSelectedItem();
        final boolean xpathSelected = XPATH_TYPE_STR.equalsIgnoreCase(selected);
        m_useXPath1_0.setEnabled(xpathSelected);
//       dummy.setVisible(!xpathSelected);
    }

    void chkOnAppCtx_actionPerformed(ActionEvent e) {
        boolean selected = chkOnAppCtx.isSelected();
        model.setApplyOnXPath(selected);
//       dummy.setVisible(!xpathSelected);
    }

    private void addInputSchemaElementList() {
        String inputXsd = null;
        if ((model == null)) {

        } else {
            try {
                if (model.getApplyOnXPath()) {
                    inputXsd = getContextXSD();
                } else {
                    Map<String, Map<QPortName, ESBRecordDefinition>> inputXSDs = getInputXSDs();
                }
                if (inputXsd == null) {
                    return;
                }

                StringReader reader = new StringReader(inputXsd);
                XSLoader loader = XSDUtil.createXSLoader(null, null);
                XSModel model = loader.load(new DOMInputImpl(null, null, null, reader, null));
                if (model != null) {
                    XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);

                    if (map.getLength() > 0) {
                        for (int i = 0; i < map.getLength(); i++) {
                        }
                    }
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
    }

    public void load() throws Exception {
    }

    public CBRPropertyModel getCBRModel() {
        return model;
    }

    public void setResolverResolver_(LSResourceResolver resolverResolver_) {
        this.resolverResolver_ = resolverResolver_;
    }

    public LSResourceResolver getResourceResolver() {
        return resolverResolver_;
    }

}
