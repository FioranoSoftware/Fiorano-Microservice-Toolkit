/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.editors;

import com.fiorano.edbc.cache.configuration.FieldDefinitions;
import com.fiorano.esb.server.api.service.config.editors.AbstractPropertyEditor;
import com.fiorano.esb.server.api.service.config.helpers.ConfigurationHelper;
import com.fiorano.services.cache.cps.ui.FieldDefinitionTableUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 *@author Deepthi
 */
public class FieldDefinitionTablePropertyEditor extends AbstractPropertyEditor<FieldDefinitions> {

    public FieldDefinitionTableUI fieldDefinitionTableComposite;
    private ConfigurationHelper helper;

    @Override
    public Control createEditor(Composite parent) {
        this.helper= getPropertyEnvironment().getConfigurationHelper();
        fieldDefinitionTableComposite = new FieldDefinitionTableUI(parent, SWT.NONE,helper);
        return fieldDefinitionTableComposite;
    }

    @Override
    public Control getEditor() {

        if (value == null)
            value = new FieldDefinitions();
        fieldDefinitionTableComposite.setValue((FieldDefinitions) value);
        return fieldDefinitionTableComposite;
    }

    public FieldDefinitions cloneAndFetchValue() {

    	FieldDefinitions fieldDefinitions = null;
    	
    	if (value != null) {
    		try {
    			fieldDefinitions = (FieldDefinitions) value.clone();
			} catch (CloneNotSupportedException e) {}
    	}

        FieldDefinitions toReturn = value;
        value = fieldDefinitions;
        return toReturn;
    }

}
