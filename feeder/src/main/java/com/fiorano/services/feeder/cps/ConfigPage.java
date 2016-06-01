/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.esb.server.api.service.config.helpers.ConfigurationHelper;
import com.fiorano.services.cps.ui.schema.ESBSchemaEditorUI;
import com.fiorano.services.cps.ui.schema.SchemaController;
import com.fiorano.services.cps.ui.schema.SchemaModel;
import com.fiorano.services.cps.ui.schema.SchemaModelEvent;
import com.fiorano.services.cps.ui.schema.SchemaModelListener;
import com.fiorano.services.cps.ui.schema.SchemaModelValidationException;
import com.fiorano.uif.xml.parser.ParserSupport;
import com.fiorano.uif.xml.parser.ParserSupportFactory;
import com.fiorano.uif.xml.treeui.MapperTreeMaker;
import com.fiorano.uif.xml.util.XMLValidator;

import feeder.Messages_Feeder;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.tifosi.common.TifosiException;

/**
 * 
 *@author geetha
 */
public class ConfigPage extends WizardPage {

    private final FeederPM configuration;
    private Composite schemaControl;
    private ESBSchemaEditorUI schemaEditor;
    private SchemaController controller;
    private Button xmlButton;
    private Button textButton;
    private String errorMsg;
    private ConfigurationHelper configurationHelper;
    private ControlEnableState enableState;
    private String lastSelection = ""; //$NON-NLS-1$

    protected ConfigPage(FeederPM configuration,
            ConfigurationHelper configurationHelper) {
        super("FeederConfigPage"); //$NON-NLS-1$
        this.configuration = configuration;
        this.configurationHelper = configurationHelper;
        setTitle(Messages_Feeder.ConfigPage_2);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createMsgFormat(composite);
        createSchemaControls(composite);

        loadConfiguration();
        initListeners();
        setControl(composite);
    }

    public SchemaController getController() {
        return controller;
    }

    private void loadConfiguration() {
        if (configuration.getMessageFormat() == FeederPM.XML) {
            xmlButton.setSelection(true);
            textButton.setSelection(false);
            doSelection(xmlButton);

        } else {
            xmlButton.setSelection(false);
            textButton.setSelection(true);
            doSelection(textButton);
        }
        if (configuration.getSchema() != null) {
            controller.getSchemaModel().load(configuration.getSchema());
            controller.loadModel();
        }

        validateData();
    }

    @SuppressWarnings("unchecked")
    private void validateData() {
        // schema validation
        if (xmlButton.getSelection()) {
            if (controller.getSchemaModel() != null) {
                try {
                    controller.getSchemaModel().validate();
                } catch (SchemaModelValidationException e) {
                    errorMsg = Messages_Feeder.ConfigPage_4 + e.getMessage();
                    setPageValid(false);
                    return;
                }
            }

            // check if schema is valid;
            ESBRecordDefinition recordDef = controller.getSchemaDefinition();
            String dtd = recordDef.getStructure();
            if (recordDef == null || dtd == null || dtd.length() == 0) {
                errorMsg = Messages_Feeder.ConfigPage_5;
                setPageValid(false);
                return;
            }
            if (recordDef.getDefinitionType() == ESBRecordDefinition.DTD) {
                try {
                    XMLValidator.validateDTD(dtd);
                } catch (Exception ex) {
                    errorMsg = Messages_Feeder.ConfigPage_4 + ex.getMessage();
                    setPageValid(false);
                    return;
                }
            } else {
                try {

                    Hashtable importedXSDs = (Hashtable) getImportedStructures(recordDef);
                    if(importedXSDs != null){
                    XMLValidator.validateSchema1(dtd, importedXSDs);
                    } else {
                    XMLValidator.validateSchema1(dtd);
                    }
                } catch (Exception ex) {
                    errorMsg = Messages_Feeder.ConfigPage_9;
                    setPageValid(false);
                    return;
                }
            }
        }
        // set page validity
        setPageValid(true);
    }

    private void setPageValid(boolean isValid) {
        setPageComplete(isValid);
        setErrorMessage(isValid ? null : errorMsg);
    }

    private void initListeners() {

        Listener listener = new Listener() {

            public void handleEvent(Event e) {
                doSelection((Button) e.widget);
                validateData();
            }

        };
        xmlButton.addListener(SWT.Selection, listener);

        textButton.addListener(SWT.Selection, listener);

    }

    private void createMsgFormat(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages_Feeder.ConfigPage_10);
        label.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, false));

        xmlButton = new Button(composite, SWT.RADIO);
        xmlButton.setText(Messages_Feeder.ConfigPage_11);
        xmlButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        textButton = new Button(composite, SWT.RADIO);
        textButton.setText(Messages_Feeder.ConfigPage_12);
        textButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));

    }

    private void doSelection(Button button) {
        if (button.getSelection()) {
            if (Messages_Feeder.ConfigPage_11.equals(button.getText())) {
            	setDescription(Messages_Feeder.ConfigPage_3);
            	if (!(Messages_Feeder.ConfigPage_11.equals(lastSelection))) {
            		lastSelection = Messages_Feeder.ConfigPage_11;
	                if (enableState != null)
	                    enableState.restore();
            	}

                configuration.setMessageFormat(FeederPM.XML);
                ESBRecordDefinition recDef = controller.getSchemaDefinition();
                if (recDef != null) {
                    configuration.setSchema(recDef);
                }

            } else if (Messages_Feeder.ConfigPage_12.equals(button.getText())) {
            	setDescription(null);
            	if (!(Messages_Feeder.ConfigPage_12).equals(lastSelection)) {
            		lastSelection = Messages_Feeder.ConfigPage_12;
	                if (schemaControl != null)
	                    enableState = ControlEnableState.disable(schemaControl);
            	}
                configuration.setMessageFormat(FeederPM.TEXT);
            }
        }
    }

    public ESBRecordDefinition getRecordDefinition() {
        ESBRecordDefinition schema = (ESBRecordDefinition) controller
                .getSchemaDefinition().clone();
        return schema;
    }

    private void createSchemaControls(Composite parent) {
        schemaControl = new Composite(parent, SWT.NONE);
        GridLayout schemaControlLayout = new GridLayout();
        schemaControlLayout.horizontalSpacing=schemaControlLayout.marginHeight=schemaControlLayout.marginWidth=0;
		schemaControl.setLayout(schemaControlLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        Rectangle rectangle = Display.getDefault().getClientArea();
        gridData.heightHint=(int) (rectangle.height*0.4);
        schemaControl.setLayoutData(gridData);
        
        schemaEditor = new ESBSchemaEditorUI(schemaControl, SWT.NONE,
                configurationHelper);
        controller = new SchemaController(new SchemaModel(), schemaEditor);
        controller.getSchemaModel().addEventListener(new SchemaModelListener() {

            public void schemaModelChanged(SchemaModelEvent event) {
                validateData();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map getImportedStructures(ESBRecordDefinition schema) {
        if (schema != null) {
            Map imports = schema.getImportedStructures();
            // use this object to set to the resource
            Hashtable importedXSDs = new Hashtable();
            if (imports != null) {
                Set properties = imports.entrySet();
                Iterator iter = properties.iterator();
                if (iter != null) {
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        if (value instanceof java.util.List) {
                            importedXSDs.put(key, ((java.util.List) value)
                                    .get(0));
                        } else if (value instanceof String[]) {
                            importedXSDs.put(key, ((String[]) value)[0]);
                        } else if (value instanceof String) {
                            importedXSDs.put(key, value);
                        }
                    }
                }
            }
            return importedXSDs;
        }
        return null;
    }

    public IWizardPage getNextPage() {

        configuration.setMessageFormat(xmlButton.getSelection() ? FeederPM.XML
                : FeederPM.TEXT);
        MessagePropertiesPage page = (MessagePropertiesPage) getWizard()
                .getPage(MessagePropertiesPage.PAGE_NAME);
        page.setConfiguration(configuration, controller,
                getImportedStructures(configuration.getSchema()));

        return page;
    }

}
