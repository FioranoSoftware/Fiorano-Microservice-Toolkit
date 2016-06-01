/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import com.fiorano.edbc.framework.service.configuration.AbstractErrorHandlingConfiguration;
import com.fiorano.services.common.util.RBUtil;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyEditorSupport;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ErrorHandlingActionsEditor extends PropertyEditorSupport implements ExPropertyEditor {

    protected PropertyEnv propertyEnv;

    /**
     * Returns custom editor for object
     */
    public final Component getCustomEditor() {
        try {
            AbstractErrorHandlingConfiguration configuration = (AbstractErrorHandlingConfiguration) getValue();
            return new ErrorHandlingPanel(configuration);
        } catch (Exception ex) {
            String message = RBUtil.getMessage(Bundle.class, Bundle.CUSTOM_EDITOR_INIT_FAILED, new Object[]{this.getClass().getName()});
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, new Exception(message, ex));
            return new JLabel(message);
        }
    }

    /**
     * @param propertyEnv
     */
    public void attachEnv(PropertyEnv propertyEnv) {
        this.propertyEnv = propertyEnv;
    }

    public void setAsText(String text) throws IllegalArgumentException {
    }

    public boolean supportsCustomEditor() {
        return true;
    }
}

