/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.cps;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.fiorano.bc.display.model.ConfigurationPM;

import display.Messages_Display;


/**
 * @author Prakash G.R.
 *
 */
public class ConfigPage extends WizardPage {

	private final ConfigurationPM configuration;
	private Button checkButton;
	private Spinner spinner;

	protected ConfigPage(ConfigurationPM configuration) {
		super("DisplayConfigPage"); //$NON-NLS-1$
		this.configuration = configuration;
        setTitle(Messages_Display.ConfigPage_1);
        setDescription(Messages_Display.ConfigPage_2);
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 15;
		layout.marginHeight = 15;
		composite.setLayout(layout);

		// row 1
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages_Display.ConfigPage_3);

		checkButton = new Button(composite, SWT.CHECK);
		checkButton.setText(Messages_Display.ConfigPage_4);

		// row 2
		new Label(composite, SWT.NONE);

		spinner = new Spinner(composite, SWT.BORDER);
		spinner.setMinimum(0);
		spinner.setMaximum(1000);
		spinner.setSelection(10);
		spinner.setIncrement(1);
		spinner.setPageIncrement(100);


		boolean isInfinite = configuration.getMaxBufferedMessages() == -1;
		if (isInfinite) {
			checkButton.setSelection(isInfinite);
			spinner.setEnabled(false);
		} else
			spinner.setSelection(configuration.getMaxBufferedMessages());

		checkButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();

			}
		});

		spinner.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		setControl(composite);

	}

	private void update() {

		if (checkButton.getSelection()) {
			spinner.setEnabled(false);
			configuration.setMaxBufferedMessages(-1);
		} else {
			spinner.setEnabled(true);
			configuration.setMaxBufferedMessages(spinner.getSelection());
		}
	}

}
