/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.xslt.cps.swt.ui;

import com.fiorano.esb.server.api.service.config.editorsupport.PropertyEnvironment;
import com.fiorano.services.cps.swt.ui.TreeStructureUI;
import com.fiorano.services.xslt.configuration.XsltPM;
import fiorano.esb.record.ESBRecordDefinition;
import org.eclipse.swt.widgets.Composite;

public class XSDStructureUI extends TreeStructureUI {

	public XSDStructureUI(PropertyEnvironment propertyEnv, Composite parent, int style) {
		super(propertyEnv, parent, style);
	}

	protected ESBRecordDefinition getSchema() throws Exception {
		
		ESBRecordDefinition recordDefinition = null;
		
		if (isEncrypt()) {
			String outStructure = (String) propertyEnv.getPropertyValue("OutputStructure");
			boolean isContext = XsltPM.CONTEXT.equals(outStructure);
			if (isContext) {
				recordDefinition = (ESBRecordDefinition) propertyEnv.getPropertyValue("EsbDefInPort");
			} else {
				recordDefinition = (ESBRecordDefinition) propertyEnv.getPropertyValue("EsbDefOutPort");
			}
		} else {
			recordDefinition = (ESBRecordDefinition) propertyEnv.getPropertyValue("EsbDefInPort");
		}
		
		return recordDefinition;
	}
}
