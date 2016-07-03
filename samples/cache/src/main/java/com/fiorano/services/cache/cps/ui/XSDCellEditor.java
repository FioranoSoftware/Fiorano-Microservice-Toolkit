/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cache.cps.ui;

import com.fiorano.esb.server.api.service.config.helpers.ConfigurationHelper;
import com.fiorano.esb.server.api.service.config.ui.ConfigurationBrowser;
import fiorano.esb.record.ESBRecordDefinition;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class XSDCellEditor extends CellEditor {

	private ConfigurationBrowser<XSDSchemaConfigUI, ESBRecordDefinition> browser;
	private ESBRecordDefinition config;
    protected ConfigurationHelper helper;
	public XSDCellEditor() {

	}

	public XSDCellEditor(Composite parent,ConfigurationHelper helper) {
        this.helper = helper;
        create(parent);
	}

	public XSDCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected Control createControl(Composite parent) {
		browser = new ConfigurationBrowser<com.fiorano.services.cache.cps.ui.XSDSchemaConfigUI, ESBRecordDefinition>(parent, SWT.NONE, false, com.fiorano.services.cache.cps.ui.XSDSchemaConfigUI.class,helper);
		return browser;
	}

	@Override
	protected Object doGetValue() {
        config =browser.getConfiguration();
		return config;
	}

	@Override
	protected void doSetFocus() {}

	@Override
	protected void doSetValue(Object value) {
		if (browser == null || value == null || !(value instanceof ESBRecordDefinition)) {
			return;
		}
		config = (ESBRecordDefinition) value;
		browser.loadConfiguration(config);
	}
}
