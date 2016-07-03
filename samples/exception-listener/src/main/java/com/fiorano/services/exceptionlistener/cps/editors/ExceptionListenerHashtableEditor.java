/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener.cps.editors;

import java.util.Hashtable;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.fiorano.esb.server.api.service.config.editors.AbstractPropertyEditor;
import com.fiorano.services.exceptionlistener.cps.ui.ExceptionListenerHashtableUI;

/**
 * 
 *@author Deepthi
 */
public class ExceptionListenerHashtableEditor extends AbstractPropertyEditor {

    ExceptionListenerHashtableUI hashtableUI;

    @Override
    public Control createEditor(Composite parent) {
        this.hashtableUI = new ExceptionListenerHashtableUI(parent);
        return hashtableUI;
    }

    @Override
    public Control getEditor() {
        if (value == null) {
            value = new Hashtable<String, String>();
        }
        hashtableUI.setHashtable((Hashtable<String, String>) value);
        return hashtableUI;
    }

    @Override
    public Object cloneAndFetchValue() {
        Object clone = null;
        if (value != null) {
            clone = ((Hashtable<String, String>) value).clone();
        }
        Object toReturn = value;
        value = clone;
        return toReturn;
    }

}
