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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class SchemaSettingsPage extends WizardPage {

    private CBRPropertyModel configuration;
    private Composite schemaControl;
    private SchemaEditorUI schemaEditor;
    private SchemaController controller;

    protected SchemaSettingsPage(CBRPropertyModel configuration) {

        super("CBRConfigPage"); //$NON-NLS-1$
        this.configuration = configuration;
        setTitle(Messages_CBR.SchemaSettingsPage_1);
        setDescription(Messages_CBR.SchemaSettingsPage_2);
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createSchemaControls(composite);
        loadConfiguration();

        setControl(composite);
    }

    private void createSchemaControls(Composite parent) {

        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 15;

        schemaControl = new Composite(parent, SWT.NONE);
        schemaControl.setLayout(layout);
        schemaControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        schemaEditor = new SchemaEditorUI(schemaControl, 0);
        controller = new SchemaController(new SchemaModel(), schemaEditor);
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
