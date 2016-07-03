/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.ui;

import cache.Messages_Cache;
import com.fiorano.edbc.cache.configuration.Bundle;
import com.fiorano.edbc.cache.configuration.FieldDefinition;
import com.fiorano.edbc.cache.configuration.FieldDefinitions;
import com.fiorano.esb.server.api.service.config.editorsupport.EditorUI;
import com.fiorano.esb.server.api.service.config.editorsupport.ValidationException;
import com.fiorano.esb.server.api.service.config.helpers.ConfigurationHelper;
import com.fiorano.services.cache.engine.dmi.XML;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.cps.ui.CheckBoxColumnLabelProvider;
import fiorano.esb.record.ESBRecordDefinition;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

/**
 * 
 *@author Deepthi
 */
public class FieldDefinitionTableUI extends Composite implements EditorUI {

    private static final String NAME = RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.NAME);
    private static final String TYPE = RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.TYPE);
    private static final String KEY = RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.KEY);
    private static final String XSD = RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.XSD);

    private static final String[] COLUMN_NAMES = { NAME, TYPE, KEY , XSD};

    private Label titleLabel;
    private Button addBtn, removeBtn;
    private TableViewer tableViewer;
    private Table table;
    private Collection<FieldDefinition> fieldDefinitions;
    private ConfigurationHelper helper;
    public FieldDefinitionTableUI(Composite parent, int style,ConfigurationHelper helper) {
        super(parent, style);
        this.helper=helper;
        buildComponentControls();
    }

    // Contents of Shell

    public void buildComponentControls() {

		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
        GridData titleGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        titleGridData.horizontalSpan = 2;

        titleLabel = new Label(this, SWT.NONE);
        titleLabel.setText(RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.FIELD_DEFINITION_TABLE));
        titleLabel.setLayoutData(titleGridData);

        tableViewer = new TableViewer(this, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);

        TableViewerColumn tableViewerColumn = new TableViewerColumn(
                tableViewer, SWT.NONE);
        tableViewerColumn.getColumn().setWidth(135);
        tableViewerColumn.getColumn().setText(NAME);
        tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {

            public String getText(Object element) {
                return ((FieldDefinition) element).getName();
            }
        });

        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumn.getColumn().setWidth(135);
        tableViewerColumn.getColumn().setText(TYPE);
        tableViewerColumn.getColumn().setResizable(false);
        tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {

            public String getText(Object element) {
                return ((FieldDefinition) element).getClazz().getSimpleName();
            }
        });

        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumn.getColumn().setWidth(135);
        tableViewerColumn.getColumn().setText(KEY);
        tableViewerColumn.setLabelProvider(new CheckBoxColumnLabelProvider(
                tableViewer) {

            protected boolean isChecked(Object element) {
                return ((FieldDefinition) element).isKey();
            }
        });

        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumn.getColumn().setWidth(135);
        tableViewerColumn.getColumn().setText(XSD);
        tableViewerColumn.getColumn().setResizable(false);
        tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {

            public String getText(Object element) {
                return Messages_Cache.FieldDefinitionTableUI_0;
            }
        });

        table = tableViewer.getTable();

        GridData tableGridData = new GridData(GridData.FILL_BOTH);
        tableGridData.verticalSpan = 3;
        tableGridData.heightHint = 350;

        table.setLayoutData(tableGridData);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        addBtn = new Button(this, SWT.PUSH);
        addBtn.setText(RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.ADD));
        addBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        removeBtn = new Button(this, SWT.PUSH);
        removeBtn.setText(RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.REMOVE));

        CellEditor[] editor = new CellEditor[] {
                new TextCellEditor(table),
                new ComboBoxCellEditor(table, TypeValues.INSTANCES,
                        SWT.READ_ONLY) {

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

                }, new CheckboxCellEditor(table), new XSDCellEditor(table,helper)};

        tableViewer.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                return ((FieldDefinitions) inputElement).toArray();
            }

            public void dispose() {}

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {}
        });
        tableViewer.setColumnProperties(COLUMN_NAMES);
        tableViewer.setCellModifier(new FieldDefinitionTableCellModifier());
        tableViewer.setCellEditors(editor);

        attachListeners();
    }

    // addListeners

    private void attachListeners() {

        addBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                addNewFieldDef();
            }
        });

        removeBtn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                removeFieldDef();
            }
        });
    }

    // add_performAction

    private void addNewFieldDef() {

        FieldDefinition fieldDef = new FieldDefinition();
        fieldDef.setName(getUniqueName());
        fieldDef.setClazz(int.class);
        fieldDef.setKey(false);
        fieldDef.setXsd(new ESBRecordDefinition());
        fieldDefinitions.add(fieldDef);
        tableViewer.refresh();
    }

    // remove_performAction

    @SuppressWarnings("unchecked")
    private void removeFieldDef() {

        ISelection selection = tableViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            Iterator<FieldDefinition> iterator = ((IStructuredSelection) selection)
                    .iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                fieldDefinitions.remove(obj);
            }
            tableViewer.refresh();
        }
    }

    // getUniqueName

    private String getUniqueName() {

        String uniqueNamePrefix = "name"; //$NON-NLS-1$
        for (int index = 0;; index++) {
            if (!fieldNameExists(uniqueNamePrefix + index)) {
                return uniqueNamePrefix + index;
            }
        }
    }

    // checkIfFieldExists

    private boolean fieldNameExists(final Object cellValue) {

        if (fieldDefinitions.size() > 0) {
            for (int row = fieldDefinitions.size() - 1; row >= 0; --row) {
                Iterator<FieldDefinition> iterator = fieldDefinitions
                        .iterator();
                while (iterator.hasNext()) {
                    FieldDefinition field = iterator.next();
                    if (field.getName().equals(cellValue.toString()))
                        return true;
                }
            }
        }
        return false;
    }
    
	@SuppressWarnings("unchecked")
	public void setValue(FieldDefinitions config) {

		this.fieldDefinitions = config;
		updateUI();
	}

    protected void updateUI() {
        
        tableViewer.setInput(fieldDefinitions);
        tableViewer.refresh();
    }

    public Object getPropertyValue() {
		FieldDefinitions fdDefinitions = new FieldDefinitions();
		for (FieldDefinition fieldDefinition :fieldDefinitions){
			fdDefinitions.addFieldDefinition(fieldDefinition);
		}
        return fdDefinitions;
    }

    // InnerClass_CellModifier

    private class FieldDefinitionTableCellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
            FieldDefinition customFieldDef = (FieldDefinition) element;
            if (!customFieldDef.getClazz().getSimpleName().equals(XML.getSimpleName()) &&
                    property.equals(FieldDefinitionTableUI.XSD))
                return false;

            return true;
        }

        public Object getValue(Object element, String property) {

            FieldDefinition customFieldDef = (FieldDefinition) element;
            if (FieldDefinitionTableUI.NAME.equals(property))
                return customFieldDef.getName();
            else if (FieldDefinitionTableUI.TYPE.equals(property))
                return customFieldDef.getClazz().getSimpleName();
            else if (FieldDefinitionTableUI.KEY.equals(property))
                return customFieldDef.isKey();
            else if (FieldDefinitionTableUI.XSD.equals(property))
                return customFieldDef.getXsd();
            else
                return null;
        }

        public void modify(Object element, String property, Object value) {

            String test = "[a-zA-Z0-9_]+"; //$NON-NLS-1$

            if (element instanceof Item)
                element = ((Item) element).getData();

            FieldDefinition customFieldDef = (FieldDefinition) element;

            if (FieldDefinitionTableUI.NAME.equals(property)) {
                if (!(value.toString().equals("")) //$NON-NLS-1$
                        && ((value.toString()).matches(test))) {
                    if (!(customFieldDef.getName().equals(value.toString()))) {
                        if (fieldNameExists(value))
                            displayErrorMessage(RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.DUPLICATE_FIELD_NAME));
                        else {
                        	FieldDefinition newFieldDefinition = new FieldDefinition();
                        	newFieldDefinition.setName((String) value);
                        	newFieldDefinition.setClazz(customFieldDef.getClazz());
                        	newFieldDefinition.setKey(customFieldDef.isKey());
                            newFieldDefinition.setXsd(customFieldDef.getXsd());
                            fieldDefinitions.remove(customFieldDef);
                            fieldDefinitions.add(newFieldDefinition);
                        }
                    }
                } else
                    displayErrorMessage(RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.INVALID_FIELD_NAME));
            } else if (FieldDefinitionTableUI.TYPE.equals(property)) {
                for (int i = 0; i < TypeValues.INSTANCES.length; i++) {
                    if (TypeValues.INSTANCES[i].equals(value.toString())) {
                        customFieldDef.setClazz(TypeValues.INSTANCES_Class[i]);
                        break;
                    }
                }
            } else if (FieldDefinitionTableUI.KEY.equals(property)) {
                customFieldDef.setKey(((Boolean) value).booleanValue());
            } else if (FieldDefinitionTableUI.XSD.equals(property)) {
                customFieldDef.setXsd((ESBRecordDefinition) value);
            }

            tableViewer.refresh();
        }

        private void displayErrorMessage(String message) {
            MessageBox msgBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            msgBox.setMessage(message);
            msgBox.setText(Messages_Cache.FieldDefinitionTableUI_11);
            msgBox.open();
        }
    }

    protected boolean validateConfig() throws ValidationException {
		
		if (fieldDefinitions.isEmpty()) {
			throw new ValidationException(RBUtil.getMessage(Bundle.class, Bundle.NO_FIELDS));
		} else if (fieldDefinitions.size() == 1) {
			throw new ValidationException(RBUtil.getMessage(Bundle.class, Bundle.INSUFFICIENT_FIELDS));
        }

		if (getFieldDefinitions(true).isEmpty()) {
			throw new ValidationException(RBUtil.getMessage(Bundle.class, Bundle.NO_KEY_FIELDS));
        }
        if (getFieldDefinitions(false).isEmpty()) {
        	throw new ValidationException(RBUtil.getMessage(Bundle.class, Bundle.NO_DATA_FIELDS));
        }
        
        return true;
    }
    
    private Collection<FieldDefinition> getFieldDefinitions(boolean key) {
        Iterator<FieldDefinition> allFieldsIterator = fieldDefinitions.iterator();
        FieldDefinition fieldDefinition;
        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
        if (allFieldsIterator != null) {
            while (allFieldsIterator.hasNext()) {
                fieldDefinition = allFieldsIterator.next();
                boolean add = false;
                add = (key && fieldDefinition.isKey()) || (!key && !fieldDefinition.isKey());
                if (add) {
                    fieldDefinitions.add(fieldDefinition);
                }
            }
        }
        return fieldDefinitions;
    }

	@Override
	public boolean validate() throws ValidationException {
		// TODO Auto-generated method stub
		return false;
	}
}

// ComboBoxValues

class TypeValues {

    public static final Class<?> INT_Class = int.class;
    public static final Class<?> LONG_Class = long.class;
    public static final Class<?> SHORT_Class = short.class;
    public static final Class<?> FLOAT_Class = float.class;
    public static final Class<?> DOUBLE_Class = double.class;
    public static final Class<?> BOOLEAN_Class = boolean.class;
    public static final Class<?> STRING_Class = String.class;
    public static final Class<?> DATE_Class = Date.class;
    public static final Class<?> XML_Class = XML.class;

    public static final Class<?>[] INSTANCES_Class = { INT_Class, LONG_Class,
            SHORT_Class, FLOAT_Class, DOUBLE_Class, BOOLEAN_Class,
            STRING_Class, DATE_Class, XML_Class };

    public static final String INT = INT_Class.getSimpleName();
    public static final String LONG = LONG_Class.getSimpleName();
    public static final String SHORT = SHORT_Class.getSimpleName();
    public static final String FLOAT = FLOAT_Class.getSimpleName();
    public static final String DOUBLE = DOUBLE_Class.getSimpleName();
    public static final String BOOLEAN = BOOLEAN_Class.getSimpleName();
    public static final String STRING = STRING_Class.getSimpleName();
    public static final String DATE = DATE_Class.getSimpleName();
    public static final String xml = XML.getSimpleName();
    public static final String[] INSTANCES = { INT, LONG, SHORT, FLOAT, DOUBLE,
            BOOLEAN, STRING, DATE, xml };
}
