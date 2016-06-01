/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cache.cps.ui;

import cache.Messages_Cache;
import com.fiorano.edbc.cache.configuration.Bundle;
import com.fiorano.esb.server.api.service.config.editorsupport.ValidationException;
import com.fiorano.esb.server.api.service.config.helpers.ConfigurationHelper;
import com.fiorano.esb.server.api.service.config.ui.ConfigurationComposite;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.services.cps.ui.schema.ESBSchemaEditorUI;
import com.fiorano.services.cps.ui.schema.SchemaController;
import com.fiorano.services.cps.ui.schema.SchemaModel;
import com.fiorano.services.cps.ui.schema.SchemaModelInterface;
import com.fiorano.tools.utilities.StringUtil;
import com.fiorano.util.ErrorListener;
import fiorano.esb.record.ESBRecordDefinition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class XSDSchemaConfigUI extends ConfigurationComposite<ESBRecordDefinition> {

	private ESBRecordDefinition config;
    private ConfigurationHelper configurationHelper;
    private ESBSchemaEditorUI schemaEditor;
    private SchemaController controller;
    private SchemaModel schemaModel;
    private Composite schemaControl;
    private String title;

	public XSDSchemaConfigUI(Composite parent) {
		this(parent, SWT.NONE, null);
	}

	public XSDSchemaConfigUI(Composite parent, int style, String title) {
		super(parent, style);
		this.title = title;
		buildControls();
	}

    public XSDSchemaConfigUI(Composite parent,ConfigurationHelper configurationHelper) {
        super(parent, SWT.NONE);
        this.title = title;
        this.configurationHelper = configurationHelper;
        buildControls();
    }

	private void buildControls() {
		
		setLayout(new GridLayout());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		schemaControl = new Composite(this, SWT.BEGINNING);
        schemaControl.setLayout(new GridLayout());

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        Rectangle rectangle = Display.getDefault().getClientArea();
        gridData.heightHint = (int) (rectangle.height * 0.4);
        gridData.widthHint = (int) (rectangle.width * 0.4);

        schemaControl.setLayoutData(gridData);
        schemaModel = new SchemaModel();
        schemaEditor = new ESBSchemaEditorUI(schemaControl, SWT.NONE, configurationHelper);
        controller = new SchemaController(schemaModel, schemaEditor);
        controller.getSchemaModel().addOption(SchemaModelInterface.OPTION_DTD_SUPPORTED, Boolean.FALSE);
        controller.getSchemaModel().addOption(SchemaModelInterface.OPTION_ALLOW_EMPTY_DEFINITION, Boolean.TRUE);
	}


	private void updateUI() {
		
		if (config == null) {
			return;
		}
		
		schemaModel.load(config);
        controller.loadModel();
	}
	
	private void updateConfig() {

		ESBRecordDefinition definition = schemaModel.getRecordDefFromModel();
        if(definition==null)
            definition = new ESBRecordDefinition();
        config.setDefinitionType(definition.getDefinitionType());
        config.setImportedStructures(definition.getImportedStructures());
        config.setNSPrefix(definition.getNSPrefix());
        config.setRootElementName(definition.getRootElementName());
        config.setStructure(definition.getStructure()!=null?definition.getStructure():""); //$NON-NLS-1$
        config.setTargetNamespace(definition.getTargetNamespace());
	}

	@Override
	public ESBRecordDefinition getConfiguration() {
		updateConfig();
		return config;
	}

	@Override
	public void loadConfiguration(ESBRecordDefinition config) {
		this.config = config;
		updateUI();
	}

	@Override
	public void validate(ErrorListener errorListener) throws ValidationException {
        try {
            if (schemaModel.getSelectedRootElements().size() == 0 && !StringUtil.isEmpty(schemaModel.getStructure())) {
                errorListener.error(new Exception(RBUtil.getMessage(Bundle.class, Bundle.NO_ROOT_ELEMENT)));
            }
		} catch (Exception e) {
			throw new ValidationException(e.getMessage());
		}

	}

	@Override
	public String getTitle() {
		return Messages_Cache.XSDSchemaConfigUI_1;
	}
}
