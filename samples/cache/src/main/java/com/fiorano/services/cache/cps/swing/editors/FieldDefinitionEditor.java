/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.swing.editors;

import com.fiorano.adapter.jca.editors.JMXBasedPropertyEditor;
import com.fiorano.edbc.cache.configuration.FieldDefinitions;
import com.fiorano.services.cache.cps.swing.editors.panels.FieldDefinitionPanel;

import java.awt.*;

/**
 * The Custom Editor class to be launched for adding key fields and data fields
 *
 * @author FSTPL
 */
public class FieldDefinitionEditor extends JMXBasedPropertyEditor {

    /**
     * @return the property value as a string suitable for presentation
     * to a human to edit.
     */
    public String getAsText() {
        return "Click ... to edit";
    }

    /**
     * Sets as text for object
     */
    public void setAsText(String text)
            throws IllegalArgumentException {
    }

    /**
     * Determines whether the propertyEditor can provide a custom editor.
     *
     * @return True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * @return Custom UI object
     */
    protected Component _getCustomEditor() {
        FieldDefinitions fieldDefinitions = ((FieldDefinitions) getValue());
        return new FieldDefinitionPanel(fieldDefinitions);
    }
}

