/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps;

import cbr.Messages_CBR;
import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.services.cbr.engine.CBRConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 *@author chaitanya
 */
public class XPathSettingsPage extends WizardPage {

    protected CBRPropertyModel configuration;
    private XPathTableUI xPathTableUI;

    private Button applyOnContextButton;
    private Button useXsltButton;
    private Button useXPath1Button;
    private Button useXPath2Button;

    protected XPathSettingsPage(CBRPropertyModel configuration) {

        super("Options"); //$NON-NLS-1$
        this.configuration = configuration;
        setTitle(Messages_CBR.XPathSettingsPage_1);
        setDescription(Messages_CBR.XPathSettingsPage_2);
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createPortControls(composite);

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createXPathOptionControls(composite);

        setControl(composite);
    }

    private void createPortControls(Composite parent) {

        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
        tableData.minimumHeight = 150;

        xPathTableUI = new XPathTableUI(parent, configuration);
        xPathTableUI.setLayoutData(tableData);
    }

    private void createXPathOptionControls(Composite parent) {

        GridLayout layout = new GridLayout();
        layout.marginWidth = 20;
        layout.marginHeight = 20;

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridData groupData = new GridData();
        groupData.minimumWidth = 200;

        Group group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages_CBR.XPathSettingsPage_3);

        applyOnContextButton = new Button(group, SWT.CHECK);
        applyOnContextButton.setText(Messages_CBR.XPathSettingsPage_4);
        applyOnContextButton.setSelection(configuration.getApplyOnXPath());
        applyOnContextButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                configuration.setApplyOnXPath(applyOnContextButton
                        .getSelection());
            }
        });

        boolean isXSLTProcessor = configuration.getProcessorType() == CBRConstants.XSLT_TYPE;
        boolean isXPathProcessor = configuration.getProcessorType() == CBRConstants.XPATH_TYPE;
        boolean usingXPath1_0 = configuration.getUseXPath1_0();

        useXsltButton = new Button(group, SWT.RADIO);
        useXsltButton.setText(Messages_CBR.XPathSettingsPage_5);
        useXsltButton.setSelection(isXSLTProcessor);
        useXsltButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                configuration.setProcessorType(CBRConstants.XSLT_TYPE);
            }
        });

        useXPath1Button = new Button(group, SWT.RADIO);
        useXPath1Button.setText(Messages_CBR.XPathSettingsPage_6);
        useXPath1Button.setSelection(usingXPath1_0 && isXPathProcessor);
        useXPath1Button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                configuration.setUseXPath1_0(useXPath1Button.getSelection());
                configuration.setProcessorType(CBRConstants.XPATH_TYPE);
            }
        });

        // The Default XPath Processor is 2.0
        useXPath2Button = new Button(group, SWT.RADIO);
        useXPath2Button.setText(Messages_CBR.XPathSettingsPage_7);
        useXPath2Button.setSelection(isXPathProcessor && !usingXPath1_0);
        useXPath2Button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                configuration.setProcessorType(CBRConstants.XPATH_TYPE);
                configuration.setUseXPath1_0(false);
            }
        });
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
}
