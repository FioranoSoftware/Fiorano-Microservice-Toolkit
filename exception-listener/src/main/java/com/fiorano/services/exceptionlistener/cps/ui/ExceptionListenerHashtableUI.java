/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener.cps.ui;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fiorano.esb.server.api.service.config.editorsupport.PropertyEnvironment;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.fiorano.esb.server.api.service.config.editorsupport.EditorUI;
import com.fiorano.esb.server.api.service.config.editorsupport.ValidationException;

import exceptionlistener.Messages_ExceptionListener;

/**
 * 
 *@author Deepthi
 */
public class ExceptionListenerHashtableUI extends Composite implements EditorUI {

    private static final String NAME_PROPERTY = Messages_ExceptionListener.ExceptionListenerHashtableUI_0;
    private static final String VALUE_PROPERTY = Messages_ExceptionListener.ExceptionListenerHashtableUI_1;
    private static final String[] MATCHES_VALUES = new String[] {
            Boolean.TRUE.toString(), Boolean.FALSE.toString() };

    private Table table;
    private TableViewer viewer;

    private Button addButton;
    private Button deleteButton;
    private Button deleteAllButton;
    private GridData addButtonData;
    private GridData delButtonData;
    private GridData delAllButtonData;

    private Map<String, String> linkedHashMap = new LinkedHashMap<String, String>();

    // private Hashtable<String, String> hashtable = new Hashtable<String, String>();

    public ExceptionListenerHashtableUI(Composite parent) {
        super(parent, SWT.NONE);
        buildControls();
    }

    private void buildControls() {

        GridLayout compositeLayout = new GridLayout(3, false);
        setLayout(compositeLayout);

        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 6);
        tableData.widthHint = 200;
        tableData.heightHint = 200;

        table = new Table(this, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
        table.setLayoutData(tableData);

        buildAndLayoutTable();
        attachButtons();
        attachContentProvider();
        attachLabelProvider();
        attachCellEditors();

        viewer.setInput(linkedHashMap);
    }

    private void buildAndLayoutTable() {

        viewer = new TableViewer(table);

        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(40, 80, true));
        layout.addColumnData(new ColumnWeightData(60, 120, true));

        table.setLayout(layout);

        TableColumn tagNameColumn = new TableColumn(table, SWT.LEFT);
        tagNameColumn.setText(NAME_PROPERTY);

        TableColumn dataTypeColumn = new TableColumn(table, SWT.LEFT);
        dataTypeColumn.setText(VALUE_PROPERTY);

        table.setHeaderVisible(true);
    }

    private void attachButtons() {

        addButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);

        addButton = new Button(this, SWT.PUSH);
        addButton.setText(Messages_ExceptionListener.ExceptionListenerHashtableUI_2);
        addButton.setLayoutData(addButtonData);

        delButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);

        deleteButton = new Button(this, SWT.PUSH);
        deleteButton.setText(Messages_ExceptionListener.ExceptionListenerHashtableUI_3);
        deleteButton.setLayoutData(delButtonData);
        deleteButton.setEnabled(false);

        delAllButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);

        deleteAllButton = new Button(this, SWT.PUSH);
        deleteAllButton.setText(Messages_ExceptionListener.ExceptionListenerHashtableUI_4);
        deleteAllButton.setLayoutData(delAllButtonData);
        deleteAllButton.setEnabled(false);

        attachButtonListeners();

    }

    private void attachButtonListeners() {

        addButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                linkedHashMap.put(generateName(), Boolean.FALSE.toString());
                deleteAllButton.setEnabled(true);
                viewer.refresh();
            }

            private String generateName() {
                int i = 0;
                while (linkedHashMap.containsKey("Name" + (i++))) //$NON-NLS-1$
                    ;
                return "Name" + (i - 1); //$NON-NLS-1$
            }
        });

        deleteButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {

                TableItem[] selection = table.getSelection();
                for (TableItem tableItem : selection) {

                    Entry entry = (Entry) tableItem.getData();
                    linkedHashMap.remove(entry.getKey());
                }

                viewer.refresh();

                if (table.getItemCount() == 0) {
                    deleteAllButton.setEnabled(false);
                }

                deleteButton.setEnabled(false);
            }
        });

        deleteAllButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {

                linkedHashMap.clear();
                viewer.refresh();
                deleteAllButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
        });

        table.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                tableActionPerformed();
            }

            public void widgetSelected(SelectionEvent e) {
                tableActionPerformed();
            }

            private void tableActionPerformed() {
                deleteButton.setEnabled(true);
                deleteAllButton.setEnabled(true);
            }
        });
    }

    private void attachContentProvider() {

        viewer.setContentProvider(new IStructuredContentProvider() {

            @SuppressWarnings("unchecked")
            public Object[] getElements(Object inputElement) {
                return ((LinkedHashMap<String, String>) inputElement)
                        .entrySet().toArray();
            }

            public void dispose() {}

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {}

        });
    }

    private void attachLabelProvider() {

        viewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            @SuppressWarnings("unchecked")
            public String getColumnText(Object element, int columnIndex) {

                switch (columnIndex) {
                case 0:
                    return ((Entry<String, String>) element).getKey();
                case 1:
                    return ((Entry<String, String>) element).getValue();
                default:
                    return Messages_ExceptionListener.ExceptionListenerHashtableUI_7 + columnIndex;
                }
            }

            public void addListener(ILabelProviderListener listener) {}

            public void dispose() {}

            public boolean isLabelProperty(Object element, String property) {
                return true;
            }

            public void removeListener(ILabelProviderListener lpl) {}

        });
    }

    private void attachCellEditors() {

        viewer.setCellModifier(new ICellModifier() {

            public boolean canModify(Object element, String property) {
                return true;
            }

            @SuppressWarnings("unchecked")
            public Object getValue(Object element, String property) {
                final Entry<String, String> entry = (Entry<String, String>) element;
                return NAME_PROPERTY.equals(property) ? entry.getKey() : entry
                        .getValue();
            }

            @SuppressWarnings("unchecked")
            public void modify(Object element, String property, Object value) {

                TableItem tableItem = (TableItem) element;
                Entry<String, String> data = (Entry<String, String>) tableItem
                        .getData();

                String dataKey = data.getKey();
                String dataValue = data.getValue();

                if (NAME_PROPERTY.equals(property)) {
                    if (value.toString().trim().length() != 0
                            && !linkedHashMap.containsKey(value.toString())) {
                        editKey(linkedHashMap, data.getKey().toString(), value
                                .toString());
                    }
                } else
                    data.setValue(value.toString());

                viewer.refresh();
            }

            private void editKey(Map<String, String> linkedHashMap,
                    String oldKey, String newKey) {
                Map<String, String> originalMap = new LinkedHashMap<String, String>(
                        linkedHashMap);
                linkedHashMap.clear();
                for (Entry<String, String> entry : originalMap.entrySet()) {
                    if (entry.getKey().equals(oldKey)) {
                        linkedHashMap.put(newKey, entry.getValue());
                    } else {
                        linkedHashMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        });

        viewer.setCellEditors(new CellEditor[] { new TextCellEditor(table),
                new ComboBoxCellEditor(table, MATCHES_VALUES, SWT.READ_ONLY) {

                    @Override
                    protected Object doGetValue() {
                        return getItems()[(Integer) super.doGetValue()];
                    }

                    @Override
                    protected void doSetValue(Object value) {
                        String[] items = getItems();
                        int i = 0;
                        for (; i < items.length; i++) {
                            if (items[i].equals(value)) {
                                break;
                            }
                        }
                        super.doSetValue(i);
                    }

                } });
        viewer
                .setColumnProperties(new String[] { NAME_PROPERTY,
                        VALUE_PROPERTY });
    }

    public void setHashtable(Hashtable<String, String> hashtable) {

        linkedHashMap.putAll(hashtable);
        Assert.isNotNull(viewer);
        viewer.setInput(linkedHashMap);
        if (!linkedHashMap.isEmpty())
            deleteAllButton.setEnabled(true);
        viewer.refresh();
    }

    public Hashtable<String, String> getHashtable() {
        return new Hashtable<String, String>(linkedHashMap);
    }

    public Object getPropertyValue() {
        return getHashtable();
    }

    public boolean validate() throws ValidationException {
        return true;
    }
}
