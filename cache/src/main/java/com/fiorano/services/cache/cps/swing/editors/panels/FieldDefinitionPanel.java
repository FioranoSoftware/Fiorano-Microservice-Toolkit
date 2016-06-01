/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.swing.editors.panels;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.edbc.cache.configuration.FieldDefinition;
import com.fiorano.edbc.cache.configuration.FieldDefinitions;
import com.fiorano.services.cache.engine.dmi.XML;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.swing.ConfigurationEditor;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.swing.layout.EqualsLayout;
import com.fiorano.swing.table.XTable;
import com.fiorano.swing.table.XTableHeader;
import com.fiorano.util.Util;
import fiorano.esb.record.ESBRecordDefinition;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * The custom panel showing the table to add new key and data fields
 *
 * @author FSTPL
 */
public class FieldDefinitionPanel extends JPanel implements EnhancedCustomPropertyEditor {
    private static final String FIELD_NAME_PATTERN = "[a-zA-Z0-9_]+";
    private XTable fieldTable = new XTable(new FieldDefinitionTableModel());
    private JButton addButton = new JButton();
    private JButton removeButton = new JButton();
    private JScrollPane scrollPane = new JScrollPane(fieldTable);
    private BorderLayout borderLayout = new BorderLayout();
    private JPanel buttonPanel = new JPanel();
    private JLabel label = new JLabel("Field Definition Table");
    private ConfigurationEditor<ESBRecordDefinition> xsdSchemaEditor;

    public FieldDefinitionPanel() {
        createUI();
    }

    public FieldDefinitionPanel(Collection fieldDefinitions) {
        createUI();
        load(fieldDefinitions);
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println(e);
        }
        FieldDefinitions fieldDefinitions = new FieldDefinitions();

        FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.setClazz(int.class);
        fieldDefinition.setName("ProductID");
        fieldDefinition.setKey(true);

        fieldDefinitions.addFieldDefinition(fieldDefinition);

        fieldDefinition = new FieldDefinition();
        fieldDefinition.setClazz(int.class);
        fieldDefinition.setName("ItemCode");
        fieldDefinition.setKey(true);

        fieldDefinitions.addFieldDefinition(fieldDefinition);

        fieldDefinition = new FieldDefinition();
        fieldDefinition.setClazz(String.class);
        fieldDefinition.setName("ProductName");
        fieldDefinition.setKey(false);

        fieldDefinitions.addFieldDefinition(fieldDefinition);

        fieldDefinition = new FieldDefinition();
        fieldDefinition.setClazz(float.class);
        fieldDefinition.setName("Price");
        fieldDefinition.setKey(false);

        fieldDefinitions.addFieldDefinition(fieldDefinition);
        CachePM config = new CachePM();
        System.out.println(fieldDefinitions);

        frame.getContentPane().setLayout(new FlowLayout());
        final FieldDefinitionPanel comp = new FieldDefinitionPanel(config.getFieldDefinitions());
        frame.getContentPane().add(comp);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                frame.dispose();
                System.out.println("-----------------");
                System.out.println(comp.getPropertyValue());
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible(true);

    }

    private void load(Collection fieldDefinitions) {
        if (fieldDefinitions == null) {
            return;
        }
        Iterator itr = fieldDefinitions.iterator();
        DefaultTableModel tableModel = (DefaultTableModel) fieldTable.getModel();
        while (itr.hasNext()) {
            FieldDefinition fieldDefinition = (FieldDefinition) itr.next();
            tableModel.addRow(new Object[]{fieldDefinition.getName(), fieldDefinition.getClazz(), fieldDefinition.isKey(), fieldDefinition.getXsd()});
        }
    }

    public Object getPropertyValue() throws IllegalStateException {
        FieldDefinitions fieldDefinitions = new FieldDefinitions();
        DefaultTableModel tableModel = (DefaultTableModel) fieldTable.getModel();

        if (tableModel.getRowCount() > 0) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                FieldDefinition fieldDef = new FieldDefinition();
                fieldDef.setName((String) tableModel.getValueAt(i, 0));
                fieldDef.setKey((Boolean) tableModel.getValueAt(i, 2));
                fieldDef.setClazz((Class) tableModel.getValueAt(i, 1));
                fieldDef.setXsd((ESBRecordDefinition) tableModel.getValueAt(i, 3));
                fieldDefinitions.addFieldDefinition(fieldDef);
            }
        }
        try {
            fieldDefinitions.validate();
        } catch (Exceptions serviceConfigurationExceptions) {
            for (Object serviceConfigurationException : serviceConfigurationExceptions) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, (Throwable) serviceConfigurationException);
            }
            throw new IllegalStateException();
        }
        return fieldDefinitions;
    }

    private void createUI() {
        setLayout(borderLayout);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
        add(label, BorderLayout.NORTH);

        buttonPanel.setLayout(new EqualsLayout(EqualsLayout.VERTICAL, EqualsLayout.NORTH, 2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        removeXTableHeaderMouseListener();
        addButton.setText(RBUtil.getMessage(Bundle.class, Bundle.ADD));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel tableModel = (DefaultTableModel) fieldTable.getModel();
                tableModel.addRow(new Object[]{getUniqueName(), String.class, Boolean.FALSE, new ESBRecordDefinition()});
            }

            private String getUniqueName() {
                String uniqueNamePrefix = "name";
                for (int index = 0; ; ++index) {
                    if (!fieldNameExists(uniqueNamePrefix + index)) {
                        return uniqueNamePrefix + index;
                    }
                }
            }
        });

        removeButton.setText(RBUtil.getMessage(Bundle.class, Bundle.REMOVE));
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel tableModel = (DefaultTableModel) fieldTable.getModel();
                int[] selectedIndices = fieldTable.getSelectedRows();
                if (selectedIndices != null && selectedIndices.length > 0) {
                    for (int i = selectedIndices.length - 1; i >= 0; i--) {
                        tableModel.removeRow(selectedIndices[i]);
                    }
                }
            }
        });

        final Class[] typeValues = new Class[]{int.class, long.class, short.class, float.class, double.class, boolean.class, String.class, Date.class, XML.class};

        TableColumn tableCol = fieldTable.getColumnModel().getColumn(2);
        tableCol.setCellRenderer(new BooleanRenderer());

        tableCol = fieldTable.getColumnModel().getColumn(1);
        final JComboBox comboBox = new JComboBox(typeValues);
        comboBox.setRenderer(new ClassListCellRenderer());
        tableCol.setCellEditor(new DefaultCellEditor(comboBox));
        tableCol.setCellRenderer(new ClassCellRenderer());

        xsdSchemaEditor = new ConfigurationEditor<ESBRecordDefinition>(new JTextField("Click ... to edit"), new XSDSchemaPanel());
        fieldTable.getColumnModel().getColumn(FieldDefinitionTableModel.XSD_INDEX).setCellEditor(xsdSchemaEditor.getTableCellEditor());

        tableCol = fieldTable.getColumnModel().getColumn(0);
        JTextField nameField = new JTextField();
        nameField.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableCol.setCellEditor(new DefaultCellEditor(nameField) {
            private Object existingValue = null;

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                existingValue = value;
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            public boolean stopCellEditing() {
                String cellValue = (String) getCellEditorValue();
                if (cellValue == null || !cellValue.matches(FIELD_NAME_PATTERN)) {
                    JOptionPane.showMessageDialog(fieldTable, RBUtil.getMessage(Bundle.class, Bundle.INVALID_FIELD_NAME), "Error", JOptionPane.ERROR_MESSAGE);
                    cancelCellEditing();
                    return true;
                }
                if (!cellValue.equals(existingValue) && fieldNameExists(cellValue)) {
                    JOptionPane.showMessageDialog(fieldTable, RBUtil.getMessage(Bundle.class, Bundle.DUPLICATE_FIELD_NAME), "Error", JOptionPane.ERROR_MESSAGE);
                    cancelCellEditing();
                    return true;
                }
                return super.stopCellEditing();
            }
        });
    }

    private void removeXTableHeaderMouseListener() {
        MouseListener[] mouseListeners = fieldTable.getTableHeader().getMouseListeners();
        for (MouseListener mouseListener1 : mouseListeners) {
            if (mouseListener1 instanceof XTableHeader) {
                fieldTable.getTableHeader().removeMouseListener(mouseListener1);
            }
        }

    }

    private boolean fieldNameExists(final Object cellValue) {
        if (fieldTable.getRowCount() > 0) {
            for (int row = fieldTable.getRowCount() - 1; row >= 0; --row) {
                if (Util.equals(fieldTable.getModel().getValueAt(row, FieldDefinitionTableModel.NAME_INDEX), cellValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class BooleanRenderer extends JCheckBox implements TableCellRenderer {
        public BooleanRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected && !hasFocus) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelected((value != null && (Boolean) value));
            return this;
        }
    }

    private static class ClassListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Class && listCellRendererComponent instanceof JLabel) {
                ((JLabel) listCellRendererComponent).setText(FieldDefinition.getShortClassName((Class) value));
            }
            return listCellRendererComponent;
        }
    }

    private static class ClassCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value instanceof Class ? FieldDefinition.getShortClassName((Class) value) : value.toString();
            setText(text);
            return component;
        }
    }

}
