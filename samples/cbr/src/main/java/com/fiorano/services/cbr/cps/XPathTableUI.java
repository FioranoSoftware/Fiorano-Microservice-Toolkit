/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps;

import cbr.Activator;
import cbr.Messages_CBR;
import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.tools.utilities.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class XPathTableUI extends Composite {

    private final String PORT_PROPERTY = Messages_CBR.XPathTableUI_0;
    private final String XPATH_PROPERTY = Messages_CBR.XPathTableUI_1;

    private TableViewer viewer;
    private Table table;

    private Button addButton;
    private Button deleteButton;
    private Button deleteAllButton;
    private Button fileButton;

    private GridData addButtonData;
    private GridData delButtonData;
    private GridData delAllButtonData;
    private GridData fileButtonData;

    private Hashtable<String, String> hashtable = new Hashtable<String, String>();
    private final CBRPropertyModel model;

    public Hashtable<String, String> getHashtable() {
        return hashtable;
    }

    public void setHashtable(Hashtable<String, String> hashtable) {

        this.hashtable = hashtable;
        Assert.isNotNull(viewer);
        viewer.setInput(hashtable);

        if (!hashtable.isEmpty())
            deleteAllButton.setEnabled(true);
        viewer.refresh();
    }

    public XPathTableUI(Composite parent, CBRPropertyModel model) {
        super(parent, SWT.NONE);
        this.model = model;
        buildControls();
        modelToComponent();
    }

    @SuppressWarnings("unchecked")
    private void modelToComponent() {
        Hashtable<String, String> xpathTable = new Hashtable<String, String>();
        ArrayList<String> portList = model.getOutPortNames() != null ? model
                .getOutPortNames() : new ArrayList<String>();
        ArrayList<String> xpathList = model.getXPaths() != null ? model
                .getXPaths() : new ArrayList<String>();
        for (String port : portList) {
            xpathTable.put(port, xpathList.get(portList.indexOf(port)));
        }
        setHashtable(xpathTable);
    }

    @SuppressWarnings("unchecked")
    private void componentToModel() {
        model.getOutPortNames().clear();
        model.getXPaths().clear();
        Hashtable<String, String> xpathTable = getHashtable();
        for (Entry<String, String> entry : xpathTable.entrySet()) {
            model.getOutPortNames().add(entry.getKey());
            model.getXPaths().add(entry.getValue());
        }
    }

    protected void buildControls() {

        GridLayout compositeLayout = new GridLayout(3, false);
        setLayout(compositeLayout);

        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 6);
        tableData.widthHint = 200;
        tableData.heightHint = 200;

        table = new Table(this, SWT.FULL_SELECTION);
        table.setLayoutData(tableData);

        buildAndLayoutTable();

        attachButtons();
        attachContentProvider();
        attachLabelProvider();
        attachCellEditors();

        viewer.setInput(hashtable);
    }

    private void attachButtons() {

        addButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);

        addButton = new Button(this, SWT.PUSH);
        addButton.setText(Messages_CBR.XPathTableUI_2);
        addButton.setLayoutData(addButtonData);

        delButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);

        deleteButton = new Button(this, SWT.PUSH);
        deleteButton.setText(Messages_CBR.XPathTableUI_3);
        deleteButton.setLayoutData(delButtonData);
        deleteButton.setEnabled(false);

        delAllButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);

        deleteAllButton = new Button(this, SWT.PUSH);
        deleteAllButton.setText(Messages_CBR.XPathTableUI_4);
        deleteAllButton.setLayoutData(delAllButtonData);
        deleteAllButton.setEnabled(false);

        fileButtonData = new GridData(SWT.FILL, SWT.FILL, false, false);

        fileButton = new Button(this, SWT.PUSH);
        fileButton.setText(Messages_CBR.XPathTableUI_5);
        fileButton.setLayoutData(fileButtonData);

        attachButtonListeners();
    }

    private void attachButtonListeners() {

        addButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                if (table.getItemCount() == 0) {
                    deleteAllButton.setEnabled(true);
                }
                hashtable.put(generateName(), " "); //$NON-NLS-1$
                deleteButton.setEnabled(true);
                viewer.refresh();
                componentToModel();
            }

            private String generateName() {

                int i = 0;
                while (hashtable.containsKey("OUT_XPATH" + (i++))) //$NON-NLS-1$
                    ;
                return Messages_CBR.XPathTableUI_8 + (i - 1);
            }
        });

        deleteButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {

                TableItem[] selection = table.getSelection();
                for (TableItem tableItem : selection) {
                    Entry entry = (Entry) tableItem.getData();
                    hashtable.remove(entry.getKey());
                }
                viewer.refresh();
                if (table.getItemCount() == 0) {
                    deleteAllButton.setEnabled(false);
                }
                deleteButton.setEnabled(false);
                componentToModel();
            }
        });

        deleteAllButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {

                hashtable.clear();
                viewer.refresh();
                deleteAllButton.setEnabled(false);
                deleteButton.setEnabled(false);
                componentToModel();
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
            }

        });

        fileButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                loadPropertiesFromFile();
            }

            public void widgetSelected(SelectionEvent e) {
                loadPropertiesFromFile();
            }
        });

    }

    private void attachLabelProvider() {

        viewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                switch (columnIndex) {
                case 0:
                    return ((Entry) element).getKey().toString();
                case 1:

                    return ((Entry) element).getValue().toString();
                default:
                    return Messages_CBR.XPathTableUI_9 + columnIndex;
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

    private void attachContentProvider() {

        viewer.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                return ((Hashtable) inputElement).entrySet().toArray();
            }

            public void dispose() {}

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {}
        });
    }

    private void buildAndLayoutTable() {

        viewer = new TableViewer(table);

        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(40, 80, true));
        layout.addColumnData(new ColumnWeightData(60, 120, true));

        table.setLayout(layout);

        TableColumn tagNameColumn = new TableColumn(table, SWT.LEFT);
        tagNameColumn.setText(PORT_PROPERTY);

        TableColumn dataTypeColumn = new TableColumn(table, SWT.LEFT);
        dataTypeColumn.setText(XPATH_PROPERTY);

        table.setHeaderVisible(true);
    }

    private void attachCellEditors() {

        viewer.setCellModifier(new ICellModifier() {

            public boolean canModify(Object element, String property) {
                return true;
            }

            public Object getValue(Object element, String property) {
                if (PORT_PROPERTY.equals(property))
                    return ((Entry) element).getKey();
                else
                    return ((Entry) element).getValue();
            }

            public void modify(Object element, String property, Object value) {

                TableItem tableItem = (TableItem) element;
                Entry data = (Entry) tableItem.getData();

                String dataKey = (String) data.getKey();
                String dataValue = (String) data.getValue();

                if (PORT_PROPERTY.equals(property)) {
                    if (!hashtable.containsKey(value.toString())) {
                        hashtable.remove(dataKey);
                        hashtable.put(value.toString(), dataValue);
                    }
                } else {
                    data.setValue(value.toString());
                }
                viewer.refresh();
                componentToModel();
            }
        });

        viewer.setCellEditors(new CellEditor[] { new TextCellEditor(table),
                new TextCellEditor(table) });
        viewer
                .setColumnProperties(new String[] { PORT_PROPERTY,
                        XPATH_PROPERTY });
    }

    @SuppressWarnings("unchecked")
    private void loadPropertiesFromFile() {

        FileDialog fd = new FileDialog(getShell());
        fd.setFilterExtensions(new String[] { "*.properties" }); //$NON-NLS-1$
        fd.setFilterNames(new String[] { "Properties File" }); //$NON-NLS-1$
        String path = fd.open();

        Properties props = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
			props.load(inputStream);
        } catch (FileNotFoundException e) {
            Logger.logException(Activator.PLUGIN_ID, e);
        } catch (IOException e) {
            Logger.logException(Activator.PLUGIN_ID, e);
        } finally {
        	if (inputStream != null) {
        		try {
					inputStream.close();
				} catch (IOException e) {
		            Logger.logException(Activator.PLUGIN_ID, e);
				}
        	}
        }
        Set<Entry<Object, Object>> entrySet = props.entrySet();
        for (Entry entry : entrySet) {
            hashtable.put((String) entry.getKey(), (String) entry.getValue());
        }
        viewer.refresh();
        componentToModel();
    }
}
