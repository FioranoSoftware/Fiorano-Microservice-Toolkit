/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps.swing.panels;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.esb.wrapper.QPortName;
import com.fiorano.services.cbr.cps.swing.Bundle;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.swing.SwingUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.ClarkName;
import com.fiorano.xml.XMLStructure;
import com.fiorano.xml.XNamespaceSupport;
import com.fiorano.xml.saxon.SaxonCompleteStringConvertor;
import com.fiorano.xml.xpath.editor.FuncletTransferHandler;
import com.fiorano.xml.xpath.editor.XPathEditor;
import com.fiorano.xml.xpath.editor.model.FuncletGraphModel;
import com.fiorano.xml.xsd.XSDResourceResolver;
import com.fiorano.xml.xsd.XSDUtil;
import com.fiorano.xml.xsd.XSDXMLStructure;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CPSUtil;
import lt.monarch.graph.GraphModelListener;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.*;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author Venkat
 * @author chaitanya
 * @author Kranthi
 * @version 1.27, 28 December 2009
 * @created September 15, 2005
 */
public class XPathCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener, LSResourceResolver, GraphModelListener {
    String inputXSD;
    String oldInputXSD;
    String xpath;
    XPathEditor xpathEditor;
    JDialog dialog;
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JFrame psFrame = new JFrame();
    private Hashtable<String, String> hashImportedStructures = new Hashtable<String, String>();
    private JPanel panel = new JPanel(new BorderLayout(0, 0));
    private JTextField tf = new JTextField();
    private JButton button = new JButton(SwingUtil.getImageIcon(this.getClass(), "dotdotdot"));
    private CBRPropertyModel model;
    private LSResourceResolver resourceResolver_;
    private XPathConfigurationPanel xPathConfigurationPanel = null;
    private ESBRecordDefinition schemaDefinition = null;

    /**
     * @throws javax.management.ReflectionException
     * @throws java.io.IOException
     * @throws javax.management.InstanceNotFoundException
     * @throws javax.management.MBeanException
     * @throws Exception
     */
    public XPathCellEditor(XPathConfigurationPanel xPathConfigurationPanel)
            throws Exception {
        this.xPathConfigurationPanel = xPathConfigurationPanel;
        jbInit();
        tf.setBorder(null);
        tf.addActionListener(this);
        button.addActionListener(this);
        button.setBorder(new MatteBorder(0, 1, 0, 0, Color.gray));
        button.setFocusPainted(false);
        this.panel.add(tf, BorderLayout.CENTER);
        this.panel.add(button, BorderLayout.EAST);

    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.

    /**
     * Returns cell editor value for object
     */
    public Object getCellEditorValue() {
        /**
         */
        return xpath;
    }

    //Implement the one method defined by TableCellEditor.

    /**
     * Returns table cell editor component for object
     */
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        xpath = (String) value;

        if (value != null) {
            tf.setText((String) xpath);
        } else {
            tf.setText("");
        }
        return panel;
    }

    /**
     * Returns the Schemas for a given namespace.
     * A definition may import one or more XSDs having the same target namespace.
     *
     * @param namespace
     */
    public String[] getImportedStructures(String namespace) {
        if (hashImportedStructures == null) {
            return new String[0];
        }
        Object schemas = hashImportedStructures.get(namespace);

        if (schemas == null) {
            return new String[0];
        }
        if (schemas instanceof String) {
            return new String[]{(String) schemas};
        }
        if (schemas instanceof String[]) {
            return (String[]) schemas;
        } else {
            java.util.List schemasList = (java.util.List) schemas;

            if (schemasList.size() == 0) {
                return new String[0];
            }
            String[] schemaStrs = (String[]) schemasList.toArray(new String[0]);

            return schemaStrs;
        }
    }

    /**
     */
    public void updateState() {
        FuncletGraphModel graphModel = (FuncletGraphModel) xpathEditor.getFunclet().getModel();
        ArrayList xpathList = graphModel.getRoots();

        // Rohit -> Fix for 8131, 8267
        if (xpathList.size() == 1) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

    /**
     * @param o
     */
    public void nodeAdded(Object o) {
        updateState();
    }

    /**
     * @param o
     */
    public void nodeRemoved(Object o) {
        updateState();
    }

    /**
     * @param o
     */
    public void nodeChanged(Object o) {
        updateState();
    }

    /**
     * @param o
     * @param o1
     */
    public void linkAdded(Object o, Object o1) {
        updateState();
    }

    /**
     * @param o
     * @param o1
     */
    public void linkRemoved(Object o, Object o1) {
        updateState();
    }

    /**
     */
    public void modelChanged() {
        updateState();
    }

    /**
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == tf) {
            xpath = tf.getText();
        } else {
            try {
                psFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(
                        this.getClass().getResource("CBRPsIcon.gif")));
            } catch (Exception e) {
                //ignore
            }
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                psFrame.setVisible(true);
            }

            dialog = new JDialog(psFrame, true);
            dialog.setTitle("Set XPath");

            try {
                xpathEditor = createXPathEditor();
            } catch (Throwable e) {

                JOptionPane.showMessageDialog(null, RBUtil.getMessage(Bundle.class, Bundle.EXCEP_CREATING_XPATH) + e.getStackTrace().toString(),
                        RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_XPATH), JOptionPane.ERROR_MESSAGE);
                CPSUtil.getAnonymousLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.EXCEP_CREATING_XPATH), e);
            }

            if (xpathEditor != null) {
                FuncletGraphModel graphModel = (FuncletGraphModel) xpathEditor.getFunclet().getModel();
                graphModel.addModelListener(this);
            }

            if (!StringUtil.isEmpty(xpath)) {
                if (xpathEditor != null) {
                    FuncletTransferHandler.importData(xpathEditor.getFunclet(), xpath);
                }
            }


            if (xpathEditor != null) {
                dialog.getContentPane().setLayout(new GridBagLayout());

                dialog.getContentPane().add(xpathEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                        , GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5, 0, 5, 0), 0, 0));

                dialog.getContentPane().add(buttonPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
                dialog.setResizable(true);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent e) {
                        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                            psFrame.setVisible(false);
                        }
                    }

                    public void windowClosing(WindowEvent e) {
                        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                            psFrame.setVisible(false);
                        }
                    }
                });
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.show();
            } else {
                JOptionPane.showMessageDialog(button, "Not able to Process XSD from the connected Components", "Invalid XSD",
                        JOptionPane.ERROR_MESSAGE);
            }

        }
        fireEditingStopped();
    }

    /**
     * @param type
     * @param namespaceURI
     * @param publicId
     * @param systemId
     * @param baseURI
     * @return
     */
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        if (hashImportedStructures == null) {
            return null;
        }

        String[] schemas = getImportedStructures(namespaceURI);
        String value = null;

        if (schemas != null && schemas.length > 0) {
            value = schemas[0];
        }
        if (value == null) {
            return XSDResourceResolver.getCatalogResolver().resolveResource(type, namespaceURI, publicId, systemId, baseURI);
        }

        LSInput input = new DOMInputImpl();

        input.setPublicId(publicId);
        input.setSystemId(systemId);
        input.setBaseURI(baseURI);
        input.setCharacterStream(new StringReader(value));
        return input;
    }

    private void jbInit() {
        button.setIcon(SwingUtil.getImageIcon(this.getClass(), "dotdotdot"));
        okButton.setText("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okButton_actionPerformed(e);
                    }
                });
        cancelButton.setText(RBUtil.getMessage(Bundle.class, Bundle.CANCEL));

        cancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancelButton_actionPerformed(e);
                    }
                });

        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        psFrame.setTitle(RBUtil.getMessage(Bundle.class, Bundle.XPATH_EDITOR));
        psFrame.setLocation(-1000, -1000);

    }

    private void okButton_actionPerformed(ActionEvent e) {
        if (xpathEditor != null) {
            FuncletGraphModel graphModel = (FuncletGraphModel) xpathEditor.getFunclet().getModel();
            ArrayList xpathList = graphModel.getRoots();

            if (xpathList != null) {
                Iterator xpathIterator = xpathList.iterator();

                if (xpathIterator.hasNext()) {
                    xpath = SaxonCompleteStringConvertor.getInstance().toString(xpathIterator.next());
                } else {
                    xpath = null;
                }
            }
        }
        dialog.dispose();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            psFrame.setVisible(false);
        }
    }

    private void cancelButton_actionPerformed(ActionEvent e) {
        dialog.dispose();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            psFrame.setVisible(false);
        }
    }

    private XPathEditor createXPathEditor()
            throws Throwable {
        model = xPathConfigurationPanel.getCBRModel();
        ESBRecordDefinition contextDefinition = null;
        ESBRecordDefinition definition = null;
        String rootClarkName = null;
        if (model != null && model.getApplyOnXPath()) {
            contextDefinition = xPathConfigurationPanel.getContextXSDRecord();
            inputXSD = contextDefinition != null ? contextDefinition.getStructure() : null;
            rootClarkName = contextDefinition != null ? ClarkName.toClarkName(contextDefinition.getTargetNamespace(), contextDefinition.getRootElementName()) : null;
        } else {
            Map<String, Map<QPortName, ESBRecordDefinition>> inputXSDs = xPathConfigurationPanel.getInputXSDs();

            if (!inputXSDs.isEmpty()) {
                Map<QPortName, ESBRecordDefinition> recordDefinitionMap = inputXSDs.get("IN_PORT");
                if (recordDefinitionMap != null && !recordDefinitionMap.isEmpty()) {
                    Collection<ESBRecordDefinition> esbRecordDefinitions = recordDefinitionMap.values();
                    definition = esbRecordDefinitions.iterator().next();
                    if ((definition != null)) {
                        oldInputXSD = definition.getStructure() != null ? definition.getStructure() : null;

                        Map importedStructures = definition.getImportedStructures();
                        if (importedStructures != null) {
                            for (Object obj : importedStructures.entrySet()) {
                                Map.Entry entry2 = (Map.Entry) obj;
                                String key = (String) entry2.getKey();
                                java.util.List extXsdList = (java.util.List) entry2.getValue();
                                if (extXsdList != null)
                                    hashImportedStructures.put(key, (String) extXsdList.get(0)); //No multiple xsds with same namespace support in mapper
                            }
                        }

                    }
                }
            }

            if (model != null) {
                schemaDefinition = model.getSchemaDefinition();
            }

            if (schemaDefinition != null) {
                inputXSD = schemaDefinition.getStructure();
                Map importedStructures = schemaDefinition.getImportedStructures();
                if (importedStructures != null) {
                    for (Object obj : importedStructures.entrySet()) {
                        Map.Entry entry2 = (Map.Entry) obj;
                        String key = (String) entry2.getKey();
                        java.util.List extXsdList = (java.util.List) entry2.getValue();
                        if (extXsdList != null)
                            hashImportedStructures.put(key, (String) extXsdList.get(0));
                    }
                }
            } else {
                inputXSD = null;
            }
            rootClarkName = model.getSchemaDefinition() != null
                    ? ClarkName.toClarkName(model.getSchemaDefinition().getTargetNamespace(),
                    model.getSchemaDefinition().getRootElementName())
                    : null;
        }

        XPathEditor editor = null;
        XNamespaceSupport namespaceSupport = new XNamespaceSupport();
        XMLStructure xmlStruct;
        HashMap map = null;
        resourceResolver_ = model.getApplyOnXPath() ? contextDefinition : xPathConfigurationPanel.getResourceResolver();

        if (model.getApplyOnXPath() && contextDefinition != null) {
            map = (HashMap) model.getNamespaces().clone();
            XSLoader xsLoader = XSDUtil.createXSLoader(contextDefinition, null);
            XSModel xsModel = xsLoader.load(new DOMInputImpl(null, null, null, inputXSD, null));
            if (xsModel != null) {
                XSNamespaceItemList list = xsModel.getNamespaceItems();

                for (int i = 0; i < list.getLength(); i++) {
                    XSNamespaceItem item = list.item(i);
                    String nameSpace = item.getSchemaNamespace();
                    if (!StringUtil.isEmpty(nameSpace))
                        addNamespacePrefix(map, nameSpace);
                }
                model.setNamespaces(map);
            }
        } else
            map = model.getNamespaces();

        if (map != null) {
            Properties namespaceProp = new Properties();
            Iterator i = map.keySet().iterator();

            while (i.hasNext()) {
                String prefix = (String) i.next();

                namespaceProp.setProperty(prefix, (String) map.get(prefix));
            }
            namespaceSupport.declarePrefixes(namespaceProp);
        }
        if (inputXSD != null) {

            XSLoader xsLoader = XSDUtil.createXSLoader(resourceResolver_, null);
            XSModel xsModel = xsLoader.load(new DOMInputImpl(null, null, null, inputXSD, null));

            // Rohit -> Fix for 7964
            if (rootClarkName == null) {
                JOptionPane.showMessageDialog(null, "Cannot find Root Element", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (xsModel == null && model.getApplyOnXPath()) {
                JOptionPane.showMessageDialog(null, "Invalid XSD for Application Context", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                xmlStruct = new XSDXMLStructure(xsModel, rootClarkName, namespaceSupport);
                editor = new XPathEditor(xmlStruct);
            }
        } else if (oldInputXSD != null) {
            XSLoader xsLoader = XSDUtil.createXSLoader(this, null);
            XSModel xsModel = xsLoader.load(new DOMInputImpl(null, null, null, oldInputXSD, null));

            XSNamedMap xmap = xsModel.getComponents(XSConstants.ELEMENT_DECLARATION);
            String rootElem = null;
            if (xmap.getLength() > 0) {
                rootElem = (xmap.item(0)).getName();
            }
            if (definition != null)
                rootClarkName = ClarkName.toClarkName(definition.getTargetNamespace(), rootElem);

            editor = new XPathEditor(new XSDXMLStructure(xsModel,
                    rootClarkName, namespaceSupport));

        } else {
            XSModel defaultModel = XSDUtil.getBuiltInSchema();
            XSNamedMap xmap = defaultModel.getComponents(XSConstants.ELEMENT_DECLARATION);
            String rootElem = null;
            if (xmap.getLength() > 0) {
                rootElem = ((XSElementDeclaration) xmap.item(0)).getName();
            }
            editor = new XPathEditor(new XSDXMLStructure(defaultModel, rootElem, namespaceSupport));

        }

        if (editor != null) {
            editor.setBorder(BorderFactory.createEtchedBorder());
        }
        return editor;
    }

    public XPathConfigurationPanel getXPathConfigurationPanel() {
        return xPathConfigurationPanel;
    }

    public void setXPathConfigurationPanel(XPathConfigurationPanel xPathConfigurationPanel) {
        this.xPathConfigurationPanel = xPathConfigurationPanel;
    }


    private void addNamespacePrefix(HashMap namespacePrefixMap, String nameSpace) {

        int count = 0;
        if (nameSpace != null && !namespacePrefixMap.containsValue(nameSpace)) {
            String prefix = "ns" + (++count);

            count = getNextPrefixCount(namespacePrefixMap, count, prefix);
            prefix = "ns" + count;
            namespacePrefixMap.put(prefix, nameSpace);
        }
    }

    private int getNextPrefixCount(HashMap table, int count, String prefix) {
        while (table.containsKey(prefix)) {
            prefix = "ns" + (++count);
        }
        return count;
    }
}