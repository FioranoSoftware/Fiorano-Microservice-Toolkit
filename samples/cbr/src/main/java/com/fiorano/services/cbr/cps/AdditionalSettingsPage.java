/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps;

import cbr.Messages_CBR;
import com.fiorano.cbr.model.CBRPropertyModel;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * 
 *@author chaitanya
 */
public class AdditionalSettingsPage extends WizardPage {

    private CBRPropertyModel configuration;
    private Button enableMonitoringButton;
    private Spinner intervalSpinner;
    private Combo intervalUnit;

    protected AdditionalSettingsPage(CBRPropertyModel configuration) {

        super("Schema Settings"); //$NON-NLS-1$
        this.configuration = configuration;

        setTitle(Messages_CBR.AdditionalSettingsPage_1);
        setDescription(Messages_CBR.AdditionalSettingsPage_2);
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createMonitoringControls(composite);

        setControl(composite);
    }

    private void createMonitoringControls(Composite parent) {

        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 20;
        layout.marginHeight = 20;

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        enableMonitoringButton = new Button(composite, SWT.CHECK);
        enableMonitoringButton.setText(Messages_CBR.AdditionalSettingsPage_3);
        enableMonitoringButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
                true, false, 3, 1));
        enableMonitoringButton.setSelection(configuration
                .getMonitoringConfiguration().isEnabled());
        enableMonitoringButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                configuration.getMonitoringConfiguration().setEnabled(
                        enableMonitoringButton.getSelection());
            }
        });

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages_CBR.AdditionalSettingsPage_4);

        intervalSpinner = new Spinner(composite, SWT.BORDER);
        intervalSpinner.setSelection((int) configuration
                .getMonitoringConfiguration().getPublishInterval());
        intervalSpinner.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                configuration.getMonitoringConfiguration().setPublishInterval(
                        intervalSpinner.getSelection());
            }
        });

        intervalUnit = new Combo(composite, SWT.READ_ONLY);
        intervalUnit.add(Messages_CBR.AdditionalSettingsPage_0);
        intervalUnit.add(Messages_CBR.AdditionalSettingsPage_5);
        intervalUnit.add(Messages_CBR.AdditionalSettingsPage_6);
        intervalUnit.add(Messages_CBR.AdditionalSettingsPage_7);
        intervalUnit.add(Messages_CBR.AdditionalSettingsPage_8);
        intervalUnit.select(configuration.getMonitoringConfiguration()
                .getUnit());
        intervalUnit.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                configuration.getMonitoringConfiguration().setUnit(
                        intervalUnit.getSelectionIndex());
            }
        });
    }
}
