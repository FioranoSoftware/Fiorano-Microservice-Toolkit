/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps;

import cbr.Messages_CBR;
import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.services.cps.ui.schema.SchemaController;
import com.fiorano.services.cps.ui.schema.SchemaEditorUI;
import com.fiorano.services.cps.ui.schema.SchemaModel;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

public class ConfigPage extends WizardPage {

    private CBRPropertyModel configuration;
    private Composite schemaControl;
    private SchemaEditorUI schemaEditor;
    private SchemaController controller;
    private XPathTableUI xPathTableUI;

    protected ConfigPage(CBRPropertyModel configuration) {
        super("CBRConfigPage"); //$NON-NLS-1$
        this.configuration = configuration;
        setTitle(Messages_CBR.ConfigPage_1);
        setDescription(Messages_CBR.ConfigPage_2);
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createSchemaControls(composite);
        loadConfiguration();

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createPortControls(composite);

        setControl(composite);
    }

    private void createPortControls(Composite parent) {

        xPathTableUI = new XPathTableUI(parent, configuration);

        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
        tableData.minimumHeight = 150;
        xPathTableUI.setLayoutData(tableData);

    }

    private void createSchemaControls(Composite parent) {

        schemaControl = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 15;
        schemaControl.setLayout(layout);
        schemaControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        schemaEditor = new SchemaEditorUI(schemaControl, 0);
        controller = new SchemaController(new SchemaModel(), schemaEditor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IWizardPage getNextPage() {
        if (configuration.getXPaths() != null) {
            configuration.getXPaths().clear();
        } else {
            configuration.setXPaths(new ArrayList<String>());
        }
        // configuration.getOutPortNames().clear();
        Hashtable<String, String> hashtable = xPathTableUI.getHashtable();
        Set<Entry<String, String>> entrySet = hashtable.entrySet();
        for (Entry<String, String> entry : entrySet) {
            configuration.getXPaths().add(entry.getValue());
            // configuration.getOutPortNames().add(entry.getKey());
        }

        return super.getNextPage();
    }

    public SchemaController getController() {
        return controller;
    }

    private void loadConfiguration() {

        if (configuration.getSchemaDefinition() != null) {
            controller.getSchemaModel().load(
                    configuration.getSchemaDefinition());
            controller.loadModel();
        }

    }
}
