/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.xslt.cps.swt.editor;

import com.fiorano.services.xslt.cps.swt.ui.XSDStructureUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class XSDStructureEditor extends com.fiorano.services.cps.swt.editor.XSDStructureEditor {
	
	@Override
	public Control createEditor(Composite parent) {
		configUI = new XSDStructureUI(getPropertyEnvironment(), parent, SWT.NONE);
		return configUI;
	}
}
