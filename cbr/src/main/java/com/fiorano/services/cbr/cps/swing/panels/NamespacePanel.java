/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps.swing.panels;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.cbr.cps.swing.Bundle;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.swing.dlg.AbstractCustomizer;
import com.fiorano.uif.ui.TifosiTable;
import com.fiorano.uif.wizard.ValidationException;
import com.fiorano.uif.wizard.WizardPanel;
import com.fiorano.xml.xsd.XSDResourceResolver;
import com.fiorano.xml.xsd.XSDTargetNamespaceFinder;
import com.fiorano.xml.xsd.XSDUtil;
import fiorano.esb.util.CPSUtil;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.openide.ErrorManager;
import org.openide.WizardValidationException;
import org.w3c.dom.ls.LSResourceResolver;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Title: CBR COMPONENT</p>
 * <p>Description: Panel to add Namespaces in CBR</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: Fiorano</p>
 *
 * @author Priya
 * @author Deepthi
 * @author Chaitanya
 * @version 1.36, 28 December 2009
 * @created May 24, 2005
 */

/**
 * @bundle $class.title=Namespaces
 * @bundle $class.summary=Enter Namespaces
 */
public class NamespacePanel
        extends WizardStep {
    //public NamespacePanel np;

    public String loadedXSD = null;
    JTable table = null;
    CBRPropertyModel m_model;
    JFrame psFrame = new JFrame();
    private boolean structurChanged_;
    private ArrayList prefixes = new ArrayList();
    private ArrayList uris = new ArrayList();
    private JPanel jPanel1 = new JPanel();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JButton deleteButton = new JButton();
    private CPSESBUtil cps;
    private JPanel btnPanel = new JPanel();
    private Logger logger = CPSUtil.getAnonymousLogger();
    private WizardPanel panel = new WizardPanel();
    /**
     * The tablemodel
     */
    private AbstractTableModel tmodel =
            new AbstractTableModel() {
                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                /**
                 * Number of rows in table
                 *
                 * @return int
                 */
                public int getRowCount() {
                    return (uris.size() >= prefixes.size()) ? uris.size() + 1 : prefixes.size() + 1;
                }

                /**
                 * Number of columns in table
                 *
                 * @return int
                 */
                public int getColumnCount() {
                    return 2;
                }

                /**
                 * @param col int
                 * @return String
                 */
                public String getColumnName(int col) {
                    return col == 0 ? RBUtil.getMessage(Bundle.class, Bundle.PREFIX) : "URI";
                }

                /**
                 * @param col int
                 * @return Class
                 */
                public Class getColumnClass(int col) {
                    return String.class;
                }

                /**
                 * @param rowIndex    int
                 * @param columnIndex int
                 * @return Object
                 */
                public Object getValueAt(int rowIndex, int columnIndex) {
                    if (columnIndex == 0)
                        return (rowIndex < prefixes.size()) ? prefixes.get(rowIndex) : null;
                    else
                        return (rowIndex < uris.size()) ? uris.get(rowIndex) : null;
                }

                /**
                 * @param aValue      Object
                 * @param rowIndex    int
                 * @param columnIndex int
                 */
                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    if (aValue != null) {
                        aValue = aValue.toString().trim();

                        switch (columnIndex) {
                            case 0:
                                if (rowIndex < prefixes.size()) {
                                    prefixes.set(rowIndex, aValue);
                                    fireTableCellUpdated(rowIndex, columnIndex);
                                } else {
                                    prefixes.add(aValue);
                                    uris.add("http://www.fiorano.com");
                                    super.fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
                                }
                                break;
                            case 1:
                                if (rowIndex < uris.size()) {
                                    uris.set(rowIndex, aValue);
                                    fireTableCellUpdated(rowIndex, columnIndex);
                                } else {
                                    uris.add(aValue);
                                    prefixes.add(new String("prefix" + rowIndex));
                                    super.fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
                                }
                                break;
                            default:
                                break;
                        }
                        //((WizardDialog)(panel.getDialog())).validatePanel(panel.getIndex());
                    }
                }
            };

    /**
     * Constructor
     *
     * @param model
     * @param cpsesbUtil
     */
    public NamespacePanel(CBRPropertyModel model, CPSESBUtil cpsesbUtil) {
        this.cps = cpsesbUtil;
        try {
            table = new TifosiTable(tmodel);
            this.m_model = model;
            jbInit();
        } catch (Exception e) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_NAMESPACE), e);
        }
    }

    @Override
    protected WizardPanel createComponent() {
        return panel;
    }

    /**
     * Title of panel
     *
     * @return String
     */

    public String getTitle() {
        return RBUtil.getMessage(Bundle.class, Bundle.NAMESPACES);
    }

    /**
     * Description of Panel
     *
     * @return String
     */
    public String getDescription() {
        return "Information corresponding to Namespace";
    }

    public String getHelpID() {
        return "cbr";
    }

    /**
     * Returns load info string for object
     *
     * @return String
     */
    public String getLoadInfoString() {
        return "Initializing";
    }

    /**
     * Returns significant for object
     *
     * @return boolean
     */
    public boolean isSignificant() {
        return false;
    }

    /**
     * @throws com.fiorano.uif.wizard.ValidationException
     */
    public void lazyValidate()
            throws WizardValidationException {
        try {
            m_model.validatePrefixes(prefixes);
            m_model.validateURIs(uris);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * @throws com.fiorano.uif.wizard.ValidationException
     */
    public void validatePrefix()
            throws ValidationException {

        try {
            m_model.validatePrefixes(prefixes);
        } catch (Exception e) {
            throw new ValidationException(RBUtil.getMessage(Bundle.class, Bundle.DUPLICATE_PREFIX), e.getMessage(),
                    table);
        }
    }

    /**
     * @throws Exception
     */
    public void model2Component() {

        prefixes.clear();
        uris.clear();
        HashMap m_namespaces = null;
        m_namespaces = m_model.getNamespaces();
        if (m_namespaces != null) {
            Iterator iter = m_namespaces.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();

                prefixes.add(entry.getKey());
                uris.add(entry.getValue());
            }
        }
        tmodel.fireTableDataChanged();
    }

    /**
     */
    public void initComponentFocus() {
        table.requestFocus();
    }

    /**
     * @throws Exception
     */
    public void component2Model() {
        if (true) {//((WizardDialog)(panel.getDialog())).validatePanel(panel.getIndex())) {
            try {
                saveNameSpace();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            try {
                m_model.validatePrefixes(prefixes);
                m_model.validateURIs(uris);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

    public void saveNameSpace()
            throws Exception {
        HashMap m_namespaces = new HashMap();
        for (int i = 0; i < prefixes.size(); i++) {
            m_namespaces.put(prefixes.get(i).toString(), uris.get(i).toString());
        }
        m_model.setNamespaces(m_namespaces);
    }

    /**
     * Action performed when delete is clicked
     *
     * @param e ActionEvent
     */
    void deleteButton_actionPerformed(ActionEvent e) {
        int row = table.getSelectedRow();

        if (row < 0 || row >= uris.size()) {
            return;
        }
        prefixes.remove(row);
        uris.remove(row);

        tmodel.fireTableDataChanged();
    }

    /**
     * @throws Exception
     */
    private void jbInit()
            throws Exception {
        panel.setLayout(borderLayout1);
        //((WizardDialog)(panel.getDialog())).validatePanel(panel.getIndex());
        table.setPreferredScrollableViewportSize(new Dimension(300, 250));
        borderLayout1.setHgap(5);
        jScrollPane1.getViewport().add(table, null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(jScrollPane1, BorderLayout.CENTER);

        jPanel1.setLayout(borderLayout2);
        deleteButton.setText(RBUtil.getMessage(Bundle.class, Bundle.DELETE));
        deleteButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteButton_actionPerformed(e);
                    }
                });
        jPanel1.add(deleteButton, BorderLayout.NORTH);
        btnPanel.setLayout(new GridBagLayout());

        panel.add(jPanel1, BorderLayout.EAST);
        panel.add(btnPanel, BorderLayout.SOUTH);
    }

    public void addNameSpace(String XSD, LSResourceResolver ls, boolean isExternal) {
        try {
            HashMap nameSpaces = m_model.getNamespaces();
            //if (!isExternal) {
            prefixes.clear();
            uris.clear();
            //}

            // Add previously saved namespaces if any
            addNameSpaces(nameSpaces);
            if (isStructureChanged()) {
                if (ls == null)
                    ls = new XSDResourceResolver(Collections.EMPTY_MAP);
                XSModel model = XSDUtil.createXSLoader(ls, null)
                        .load(new DOMInputImpl(null, null, null, XSD, null));
                XSNamespaceItemList list = model.getNamespaceItems();
                int count = 0;
                for (int i = 0; i < list.getLength(); i++) {
                    XSNamespaceItem item = list.item(i);
                    String nameSpace = item.getSchemaNamespace();

                    // Put a check so that duplicate NS are not added.
                    if (nameSpace != null && !uris.contains(nameSpace)) {
                        String prefix = "ns" + (++count);
                        while (prefixes.contains(prefix)) {
                            prefix = "ns" + (++count);
                        }
                        prefixes.add(prefix);
                        uris.add(nameSpace);
                    }
                }
            }
            tmodel.fireTableDataChanged();
        } catch (Exception e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.ERROR_ADDING), e);
        }
    }

    private void addNameSpaces(HashMap namespaces) {
        if (namespaces != null && !namespaces.isEmpty()) {
            Iterator iterator = namespaces.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = namespaces.get(key);
                prefixes.add(key);
                uris.add(value);
            }
        }
    }

    public boolean isStructureChanged() {
        return structurChanged_;
    }

    public void setStructureChanged(boolean changed) {
        structurChanged_ = changed;
    }

    @Override
    public void fastValidate() throws WizardValidationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * <p><strong> </strong> represents </p>
     *
     * @author FSIPL
     * @version 1.0
     * @created October 14, 2005
     */
    class XSDInput extends AbstractCustomizer {
        JEditorPane editor1 = new JEditorPane();

        /**
         */
        public XSDInput() {
            init();
        }

        protected void okPressed() {
            loadedXSD = editor1.getText();

            StringReader r = new StringReader(loadedXSD);
            String targetNamespace = null;

            try {
                targetNamespace = XSDTargetNamespaceFinder.findTargetNamespace(r);
                super.okPressed();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }

        protected void cancelPressed() {
            loadedXSD = null;
            super.cancelPressed();
        }

        private void init() {
            editor1.setContentType("text/xml");
            editor1.setPreferredSize(new Dimension(400, 400));

            JScrollPane scrollPane = new JScrollPane();

            scrollPane.getViewport().add(editor1);
            content.add(scrollPane);
        }
    }

}