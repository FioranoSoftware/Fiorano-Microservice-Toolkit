/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.fiorano.services.feeder.cps.swing.panels.FeederConstants;
import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.bc.feeder.ps.panels.Attachment;
import com.fiorano.bc.feeder.ps.panels.Header;
import com.fiorano.services.common.configuration.EncryptDecryptElements;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.cps.swt.ui.EncryptDecryptElementsComposite;
import com.fiorano.services.cps.ui.GenerateSampleDialog;
import com.fiorano.services.cps.ui.TableContentProvider;
import com.fiorano.services.cps.ui.TableLabelProvider;
import com.fiorano.services.cps.ui.schema.SchemaController;
import com.fiorano.tools.common.ui.xml.XMLViewer;
import com.fiorano.tools.utilities.Logger;
import com.fiorano.xml.ClarkName;
import com.fiorano.xml.XMLStructure;
import com.fiorano.xml.dtd.DTDXMLStructure;
import com.fiorano.xml.xsd.XSDUtil;
import com.fiorano.xml.xsd.XSDXMLStructure;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;

import feeder.Activator;
import feeder.Messages_Feeder;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.utils.XSDValidator;

/**
 * 
 *@author geetha
 */
public class MessagePropertiesPage extends WizardPage {

    public static String PAGE_NAME = "MessagePropertiesPage"; //$NON-NLS-1$

    private FeederPM configuration;
    private Button haddButton;
    private Button hdelButton;
    private Button hdelAllButton;
    private Button aaddButton;
    private Button adelButton;
    private Button adelAllButton;
    private TableViewer headerTableViewer;
    private TableViewer attTableViewer;
    private Map<String, Header> hmodel;
    private Map<String, Attachment> amodel;
    private Table headerTable;
    private String[] VALUE_TYPES = new String[] {
            FeederConstants.STRING_PROPERTY_TYPE,
            FeederConstants.INT_PROPERTY_TYPE,
            FeederConstants.DOUBLE_PROPERTY_TYPE,
            FeederConstants.FLOAT_PROPERTY_TYPE,
            FeederConstants.LONG_PROPERTY_TYPE,
            FeederConstants.OBJECT_PROPERTY_TYPE };
    private XMLViewer defaultMsgViewer;
    private StyledText defaultMsg;
    private Button sampleButton;
    private Button validateButton;
    private SchemaController controller;
    private Button checkButton;
    private Spinner spinner;
    private FeederPM newConfiguration;
    @SuppressWarnings("unchecked")
    private Map importedStructures;
    private EncryptDecryptElementsComposite encryptDecryptComposite;

    @SuppressWarnings("unchecked")
    public MessagePropertiesPage(FeederPM configuration) {
        super(PAGE_NAME);
        this.configuration = configuration;
        setTitle(Messages_Feeder.MessagePropertiesPage_1);
        setDescription(Messages_Feeder.MessagePropertiesPage_2);
        if (configuration.getHeader() != null) {
            hmodel = configuration.getHeader();
        } else {
            hmodel = new LinkedHashMap<String, Header>();
            configuration.setHeader(hmodel);
        }
        if (configuration.getAttachment() != null) {
            amodel = configuration.getAttachment();
        } else {
            amodel = new LinkedHashMap<String, Attachment>();
            configuration.setAttachment(amodel);
        }
        if (configuration.getElementsToEncrypt() == null) {
        	configuration.setElementsToEncrypt(new EncryptDecryptElements());
        }
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabFolder tf = new TabFolder(composite, SWT.NONE);
        tf.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem tabItem = new TabItem(tf, SWT.NONE);
        tabItem.setText(Messages_Feeder.MessagePropertiesPage_3);
        tabItem.setControl(createDefaultMsgControls(tf));

        TabItem item = new TabItem(tf, SWT.NONE);
        item.setText(Messages_Feeder.MessagePropertiesPage_4);
        item.setControl(createHeaderControls(tf));

        TabItem item2 = new TabItem(tf, SWT.NONE);
        item2.setText(Messages_Feeder.MessagePropertiesPage_5);
        item2.setControl(createAttachmentControls(tf));
        
        TabItem encryptTabItem = new TabItem(tf, SWT.NONE);
        encryptTabItem.setText(Messages_Feeder.MessagePropertiesPage_18);
        encryptTabItem.setControl(createEncryptConfigControl(tf));
        
        createHistoryControls(composite);

        composite.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				defaultMsgViewer.dispose();
			}
		});
        
        setControl(composite);
        validateHeaders();
    }
    
    private Control createEncryptConfigControl(final Composite parent) {
    	
    	ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
    
    	encryptDecryptComposite = new EncryptDecryptElementsComposite(scrolledComposite, SWT.NONE, SWT.MULTI | SWT.BORDER);
		encryptDecryptComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		scrolledComposite.setContent(encryptDecryptComposite);
		
    	return scrolledComposite;
    }

    private Control createDefaultMsgControls(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setText(Messages_Feeder.MessagePropertiesPage_6);
        defaultMsgViewer = new XMLViewer(group, null, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        defaultMsg = defaultMsgViewer.getTextWidget();
        int columns = 40;
        GC gc = new GC(defaultMsg);
        FontMetrics fm = gc.getFontMetrics();
        int width = columns * fm.getAverageCharWidth();
        int height = fm.getHeight();
        gc.dispose();
        defaultMsg.setSize(defaultMsg.computeSize(width, height));
        GridData griddata = new GridData(GridData.FILL_BOTH);
        griddata.horizontalSpan = 2;
        griddata.verticalSpan = 20;
        griddata.widthHint = width;
        griddata.heightHint = height;
        defaultMsg.setLayoutData(griddata);
        defaultMsg.setText(configuration.getDefaultMessage());
        defaultMsg.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                configuration.setDefaultMessage(defaultMsg.getText());
            }
        });

        sampleButton = new Button(group, SWT.NONE);
        sampleButton.setText(Messages_Feeder.MessagePropertiesPage_7);
        sampleButton
                .setToolTipText(Messages_Feeder.MessagePropertiesPage_8);
        sampleButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String oldDefaultMsg = defaultMsg.getText();
                GenerateSampleDialog generateSampleDialog = new GenerateSampleDialog(
                        parent.getShell(), controller.getSchemaDefinition(),
                        controller.getSchemaModel());
                String newDefaultMsg = generateSampleDialog.generateSampleXML();
                defaultMsg.setText(newDefaultMsg != null ? newDefaultMsg
                        : oldDefaultMsg);
            }
        });

        validateButton = new Button(group, SWT.NONE);
        validateButton.setText(Messages_Feeder.MessagePropertiesPage_9);
        validateButton
                .setToolTipText(Messages_Feeder.MessagePropertiesPage_10);

        validateButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                validateXML();
            }
        });

        return composite;
    }

    private void createHistoryControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.marginWidth = 20;
        layout.marginHeight = 20;
        composite.setLayout(layout);

        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages_Feeder.MessagePropertiesPage_11);

        checkButton = new Button(composite, SWT.CHECK);
        checkButton.setText(Messages_Feeder.MessagePropertiesPage_12);

        spinner = new Spinner(composite, SWT.BORDER);
        spinner.setMinimum(0);
        spinner.setMaximum(1000);
        spinner.setSelection(10);
        spinner.setIncrement(1);
        spinner.setPageIncrement(100);

        checkButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                update();
            }
        });

        spinner.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                update();
            }
        });
    }

    private void update() {
        if (checkButton.getSelection()) {
            spinner.setEnabled(false);
            configuration.setHistorySize(-1);
        } else {
            spinner.setEnabled(true);
            configuration.setHistorySize(spinner.getSelection());
        }
    }

    private Control createAttachmentControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        composite.setLayout(layout);

        Table table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION
                | SWT.BORDER);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
        tableData.heightHint = 150;
        table.setLayoutData(tableData);

        TableColumn Column1 = new TableColumn(table, SWT.NONE);
        Column1.setText(Messages_Feeder.MessagePropertiesPage_13);

        TableColumn Column2 = new TableColumn(table, SWT.NONE);
        Column2.setText(Messages_Feeder.MessagePropertiesPage_14);

        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnWeightData(1, 40));
        tableLayout.addColumnData(new ColumnWeightData(1, 60));
        table.setLayout(tableLayout);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        aaddButton = new Button(composite, SWT.PUSH);
        aaddButton.setText(Messages_Feeder.MessagePropertiesPage_15);
        aaddButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        aaddButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                add_attachment(e);
                if (amodel.size() > 0) {
                    adelAllButton.setEnabled(true);
                }
                attTableViewer.refresh();
            }
        });

        adelButton = new Button(composite, SWT.PUSH);
        adelButton.setText(Messages_Feeder.MessagePropertiesPage_16);
        adelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        adelButton.setEnabled(false);
        adelButton.addSelectionListener(new SelectionAdapter() {

            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = ((IStructuredSelection) attTableViewer
                        .getSelection());
                List<Attachment> attachments = (List<Attachment>) selection
                        .toList();
                for (Attachment attachment : attachments) {
                    amodel.remove(attachment.getName());
                }
                if (amodel.size() == 0) {
                    adelAllButton.setEnabled(false);
                }
                attTableViewer.refresh();
            }
        });

        adelAllButton = new Button(composite, SWT.NONE);
        adelAllButton.setText(Messages_Feeder.MessagePropertiesPage_17);
        adelAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        adelAllButton.setEnabled(amodel.size() > 0);
        adelAllButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                amodel.clear();
                adelAllButton.setEnabled(false);
                attTableViewer.refresh();
            }
        });

        createAttachmentViewer(table);
        return composite;
    }

    private void add_attachment(SelectionEvent e) {

        FileDialog dialog = new FileDialog(getShell());
        String path = dialog.open();

        if (path == null)
            return;

        File file = new File(path);

        Attachment attachment = new Attachment();
        attachment.setName(file.getName());
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(file);

            try {
                byte bytes[] = new byte[1024];

                while (true) {
                    int read = fis.read(bytes);

                    if (read == -1) {
                        break;
                    }
                    bout.write(bytes, 0, read);
                }
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.logException(Activator.PLUGIN_ID, ex);
                }
                try {
                    bout.close();
                } catch (IOException ex) {
                    Logger.logException(Activator.PLUGIN_ID, ex);
                }
            }
            attachment.saveValue(bout.toByteArray());
            amodel.put(attachment.getName(), attachment);
        } catch (IOException ex) {
            Logger.logException(Activator.PLUGIN_ID, ex);
        }
    }

    private void createAttachmentViewer(Table table) {
        attTableViewer = new TableViewer(table);
        attTableViewer.setContentProvider(new TableContentProvider() {
            
            @SuppressWarnings("unchecked")
            public Object[] getElements(Object inputElement) {
                if (((Map<String, Attachment>) inputElement).size() > 0)
                    return ((Map<String, Attachment>) inputElement).values().toArray();
                else
                    return new Object[0];
            }
        });
        attTableViewer.setLabelProvider(new TableLabelProvider() {
            
            public String getColumnText(Object element, int columnIndex) {
                Attachment attachment = (Attachment) element;
                String value = ""; //$NON-NLS-1$
                switch (columnIndex) {
                case 0:
                    value = attachment.getName();
                    break;
                case 1:
                    byte[] val = attachment.fetchValue();
                    float size = val.length;

                    if (size != 0) {
                        size = size / 1024;
                    }
                    if (size >= 1) {
                        value = size + " KB"; //$NON-NLS-1$
                    } else {
                        value = val.length + " bytes"; //$NON-NLS-1$
                    }
                    break;
                default:
                    break;
                }
                return value;
            }
        });

        attTableViewer.setColumnProperties(new String[] { "Name", "Size" }); //$NON-NLS-1$ //$NON-NLS-2$

        attTableViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table), new TextCellEditor(table) });
        attTableViewer.setCellModifier(getCellModifier());

        attTableViewer.setInput(amodel);
        attTableViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        adelButton
                                .setEnabled(!(event.getSelection().isEmpty()));
                    }
                });
    }

    private ICellModifier getCellModifier() {
        return new ICellModifier() {

            public boolean canModify(Object element, String property) {
                return !"Size".equals(property); //$NON-NLS-1$
            }

            public Object getValue(Object element, String property) {
                Attachment data = (Attachment) element;
                if ("Name".equals(property)) { //$NON-NLS-1$
                    return data.getName();
                } else {
                    return data.fetchValue();
                }
            }

            public void modify(Object element, String property, Object value) {

                TableItem item = (TableItem) element;
                Attachment data = (Attachment) item.getData();
                if ("Name".equals(property)) { //$NON-NLS-1$
                    if (value.toString().trim().length() != 0
                            && !amodel.containsKey(value.toString())) {
                        editKey(amodel, data.getName(), value.toString());
                    }
                }
                attTableViewer.refresh();
            }

            private void editKey(Map<String, Attachment> linkedHashMap,
                    String oldKey, String newKey) {

                Map<String, Attachment> originalMap = new LinkedHashMap<String, Attachment>(
                        linkedHashMap);
                linkedHashMap.clear();
                for (Entry<String, Attachment> entry : originalMap.entrySet()) {
                    if (entry.getKey().equals(oldKey)) {
                        Attachment attachment = originalMap.get(oldKey);
                        Attachment newAttachment = new Attachment();
                        newAttachment.setName(newKey);
                        newAttachment.saveValue(attachment.fetchValue());
                        linkedHashMap.put(newKey, newAttachment);
                    } else {
                        linkedHashMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        };
    }

    private Control createHeaderControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        composite.setLayout(layout);

        headerTable = new Table(composite, SWT.FULL_SELECTION | SWT.MULTI
                | SWT.BORDER);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
        tableData.heightHint = 150;
        headerTable.setLayoutData(tableData);

        TableColumn Column1 = new TableColumn(headerTable, SWT.NONE);
        Column1.setText(Messages_Feeder.MessagePropertiesPage_0);

        TableColumn Column2 = new TableColumn(headerTable, SWT.NONE);
        Column2.setText(Messages_Feeder.MessagePropertiesPage_27);

        TableColumn Column3 = new TableColumn(headerTable, SWT.NONE);
        Column3.setText(Messages_Feeder.MessagePropertiesPage_28);

        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnWeightData(1, 30));
        tableLayout.addColumnData(new ColumnWeightData(1, 30));
        tableLayout.addColumnData(new ColumnWeightData(1, 40));

        headerTable.setLayout(tableLayout);

        headerTable.setHeaderVisible(true);
        headerTable.setLinesVisible(true);

        haddButton = new Button(composite, SWT.PUSH);
        haddButton.setText(Messages_Feeder.MessagePropertiesPage_29);
        haddButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        haddButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Header header = new Header();
                header.setName(generateName());
                header.setType(FeederConstants.STRING_PROPERTY_TYPE);
                header.setValue(""); //$NON-NLS-1$
                hmodel.put(header.getName(), header);
                if (hmodel.size() > 0) {
                    hdelAllButton.setEnabled(true);
                }
                headerTableViewer.refresh();
            }

            private String generateName() {
                int i = 0;
                while (hmodel.containsKey("Name" + (i++))) {} //$NON-NLS-1$
                return "Name" + (i - 1); //$NON-NLS-1$
            }
        });

        hdelButton = new Button(composite, SWT.PUSH);
        hdelButton.setText(Messages_Feeder.MessagePropertiesPage_33);
        hdelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        hdelButton.setEnabled(false);
        hdelButton.addSelectionListener(new SelectionAdapter() {

            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) headerTableViewer
                        .getSelection();
                List<Header> headers = (List<Header>) selection.toList();
                for (Header header : headers) {
                    hmodel.remove(header.getName());
                }

                if (hmodel.size() == 0) {
                    hdelAllButton.setEnabled(false);
                }
                headerTableViewer.refresh();
            }
        });

        hdelAllButton = new Button(composite, SWT.NONE);
        hdelAllButton.setText(Messages_Feeder.MessagePropertiesPage_34);
        hdelAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));

        hdelAllButton.setEnabled(hmodel.size() > 0);
        hdelAllButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                hmodel.clear();
                hdelAllButton.setEnabled(false);
                headerTableViewer.refresh();
            }
        });

        createHeaderViewer(headerTable);
        return composite;
    }

    private void createHeaderViewer(Table table) {
        headerTableViewer = new TableViewer(table);
        headerTableViewer.setContentProvider(new TableContentProvider() {
            
            @SuppressWarnings("unchecked")
            public Object[] getElements(Object inputElement) {
                if (((Map<String, Header>) inputElement).size() > 0)
                    return ((Map<String, Header>) inputElement).values().toArray();
                else
                    return new Object[0];
            }
        });
        
        headerTableViewer.setLabelProvider(new TableLabelProvider() {
            
            public String getColumnText(Object element, int columnIndex) {
                Header header = (Header) element;
                String value = ""; //$NON-NLS-1$
                switch (columnIndex) {
                case 0:
                    value = header.getName();
                    break;
                case 1:
                    value = header.getType();
                    break;
                case 2:
                    value = header.getValue();
                    break;
                default:
                    break;
                }
                return value;
            }
        });

        headerTableViewer.setColumnProperties(new String[] { "Name", "Type", //$NON-NLS-1$ //$NON-NLS-2$
                "Value" }); //$NON-NLS-1$

        headerTableViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table),
                new ComboBoxCellEditor(table, VALUE_TYPES, SWT.READ_ONLY),
                new TextCellEditor(table) });

        headerTableViewer.setCellModifier(getHeaderCellModifier());
        headerTableViewer.setInput(hmodel);
        headerTableViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        hdelButton
                                .setEnabled(!(event.getSelection().isEmpty()));
                    }
                });
    }

    public String[] getChoices(String property) {
        if ("Type".equals(property)) //$NON-NLS-1$
            return getValueTypes();
        else
            return new String[] {};
    }

    public String[] getValueTypes() {
        return VALUE_TYPES;
    }

    private ICellModifier getHeaderCellModifier() {

        return new ICellModifier() {

            public boolean canModify(Object element, String property) {
                return true;
            }

            public Object getValue(Object element, String property) {
                Header data = (Header) element;
                if (property.equals("Name")) //$NON-NLS-1$
                    return data.getName();
                else if (property.equals("Type")) { //$NON-NLS-1$
                    String stringValue = data.getType();
                    String[] choices = getChoices(property);
                    int i = choices.length - 1;
                    while (!stringValue.equals(choices[i]) && i > 0)
                        --i;
                    return new Integer(i);
                } else
                    return data.getValue();
            }

            public void modify(Object element, String property, Object value) {

                TableItem item = (TableItem) element;

                Header data = (Header) item.getData();

                if (property.equals("Name")) { //$NON-NLS-1$
                    if (value.toString().trim().length() != 0
                            && !hmodel.containsKey(value.toString())) {
                        editKey(hmodel, data.getName(), value.toString());
                    }
                } else if (property.equals("Type")) { //$NON-NLS-1$
                    String valueString = getChoices(property)[((Integer) value)
                            .intValue()].trim();
                    data.setType(valueString);
                } else
                    data.setValue((String) value);
                

                validateHeaders();
                headerTableViewer.refresh();
            }

            private void editKey(Map<String, Header> linkedHashMap,
                    String oldKey, String newKey) {

                Map<String, Header> originalMap = new LinkedHashMap<String, Header>(
                        linkedHashMap);
                linkedHashMap.clear();
                for (Entry<String, Header> entry : originalMap.entrySet()) {
                    if (entry.getKey().equals(oldKey)) {
                        Header header = originalMap.get(oldKey);
                        Header newHeader = new Header();
                        newHeader.setName(newKey);
                        newHeader.setType(header.getType());
                        newHeader.setValue(header.getValue());
                        linkedHashMap.put(newKey, newHeader);
                    } else {
                        linkedHashMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        };
    }

    private void loadConfiguration() {

        if (newConfiguration.getMessageFormat() == FeederPM.TEXT) {
            validateButton.setEnabled(false);
            sampleButton.setEnabled(false);
        } else {
            validateButton.setEnabled(true);
            sampleButton.setEnabled(true);
        }
        
        ESBRecordDefinition recordDefinition = controller.getSchemaDefinition();
        XMLStructure structure = null;
        
        if (recordDefinition != null) {
        	XSLoader xsLoader;
			try {
				xsLoader = XSDUtil.createXSLoader(recordDefinition, null);
				XSModel xsModel = xsLoader.load(new DOMInputImpl(null, null, null, recordDefinition.getStructure(), null));
	    		String rootElementClarkName = ClarkName.toClarkName(recordDefinition.getTargetNamespace(), recordDefinition.getRootElementName());
	    		if (recordDefinition.getDefinitionType() == ISchema.XSD) {
	    			structure = new XSDXMLStructure(xsModel, rootElementClarkName);
	    		} else {
	    			DTDParser parser = new DTDParser(new StringReader(recordDefinition.getStructure()));
	                DTD dtd = parser.parse(true);
	                structure = new DTDXMLStructure(dtd, rootElementClarkName);
	    		}
	    		
			} catch (Exception e) {}
        }
        encryptDecryptComposite.updateUIFromConfig(configuration.getElementsToEncrypt(), structure);
    }

    private void initListeners() {

        boolean isInfinite = newConfiguration.getHistorySize() == -1;
        if (isInfinite) {
            checkButton.setSelection(isInfinite);
            spinner.setEnabled(false);
        } else
            spinner.setSelection(newConfiguration.getHistorySize());
    }

    @SuppressWarnings("unchecked")
    public void setConfiguration(FeederPM configuration,
            SchemaController controller, Map importedStructures) {
        this.newConfiguration = configuration;
        this.controller = controller;
        this.importedStructures = importedStructures;
        defaultMsg.setText(newConfiguration.getDefaultMessage());
        loadConfiguration();
        initListeners();
        validateHeaders();
    }

    private boolean validateXML(String xml, ESBRecordDefinition schema)
            throws Exception {
        if (schema == null || schema.getStructure() == null) {
            return true;
        }
        if (schema.getDefinitionType() == ESBRecordDefinition.DTD) {
            XSDValidator.validateXMLwithDTD(xml, schema.getStructure(), schema
                    .getRootElementName());
        } else {
            XSDValidator.validateXMLwithSchema(xml, schema.getStructure(),
                    importedStructures);
        }
        return true;
    }

    private void validateXML() {

        try {
            if (newConfiguration.getMessageFormat() == FeederPM.XML) {
                if (!(controller.getSchemaDefinition() == null)) {
                    if (validateXML(defaultMsg.getText(), controller
                            .getSchemaDefinition())) {
                        MessageDialog.openInformation(getShell(),
                                Messages_Feeder.MessagePropertiesPage_44,
                                Messages_Feeder.MessagePropertiesPage_44);
                    }
                }
            }
        } catch (Exception e1) {
            MessageDialog.openWarning(getShell(), Messages_Feeder.MessagePropertiesPage_46, e1.getMessage());
        }
    }

    public void validateHeaders() {

        Collection<Header> headers = hmodel.values();

        if (headers != null) {
            Iterator<Header> iterator = headers.iterator();

            while (iterator.hasNext()) {
                Header header = iterator.next();
                if (!(validateHeader(header.getName(), header.getType(), header
                        .getValue()))) {
                    return;
                }
            }
        }
    }

    private boolean validateHeader(String name, String type, String value) {

        String errorMsg = ""; //$NON-NLS-1$

        try {
            if (FeederConstants.INT_PROPERTY_TYPE.equalsIgnoreCase(type)) {
                Integer.parseInt(value);
            } else if (FeederConstants.FLOAT_PROPERTY_TYPE
                    .equalsIgnoreCase(type)) {
                Float.parseFloat(value);
            } else if (FeederConstants.DOUBLE_PROPERTY_TYPE
                    .equalsIgnoreCase(type)) {
                Double.parseDouble(value);
            } else if (FeederConstants.LONG_PROPERTY_TYPE
                    .equalsIgnoreCase(type)) {
                Long.parseLong(value);
            } // No validation required for String and Object. Empty string is also valid in this
            // case.
            setErrorMessage(null);
            setPageComplete(true);
            return true;
        } catch (Exception ex) {

            String exMessage = Messages_Feeder.MessagePropertiesPage_48 + name + Messages_Feeder.MessagePropertiesPage_49
                    + value + Messages_Feeder.MessagePropertiesPage_50 + type + "'"; //$NON-NLS-2$ //$NON-NLS-1$
            errorMsg = errorMsg + exMessage + "\n"; //$NON-NLS-1$
            setErrorMessage(errorMsg);
            setPageComplete(false);
            return false;
        }
    }
    
    public void updateConfiguration() {
		encryptDecryptComposite.updateConfigFromUI();
	}

	@Override
	public IWizardPage getPreviousPage() {
		updateConfiguration();
		return super.getPreviousPage();
	}
}
