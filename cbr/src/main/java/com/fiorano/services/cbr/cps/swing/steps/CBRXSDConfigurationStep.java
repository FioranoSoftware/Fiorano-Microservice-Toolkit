/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.cps.swing.steps;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.cps.steps.ConfigurationStep;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.openide.wizard.WizardUtil;
import com.fiorano.services.cbr.cps.swing.panels.NamespacePanel;
import com.fiorano.services.cbr.cps.swing.panels.XSDPanel;

/**
 * Created by IntelliJ IDEA.
 * User: Spurthy
 * Date: 1 Feb, 2011
 * Time: 6:37:06 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * @bundle $class.title=Schema
 * @bundle $class.summary=Enter Schema
 */
public class CBRXSDConfigurationStep extends ConfigurationStep<CBRPropertyModel> {

    public CBRXSDConfigurationStep(CBRPropertyModel cbrPropertyModel, boolean readOnly, CPSESBUtil cpsesbUtil, NamespacePanel nameSpacePanel, XPathConfigurationStep xPathConfigurationStep) {
        super(readOnly, new XSDPanel());
        ((XSDPanel) getComponent()).jbInit(cbrPropertyModel, cpsesbUtil, nameSpacePanel, xPathConfigurationStep);
    }

    @Override
    protected CBRPropertyModel fetchConfigurationToLoad() {
        return (CBRPropertyModel) WizardUtil.getSettings(wizard);
    }

    @Override
    protected void updateConfiguration(CBRPropertyModel configuration) {
        WizardUtil.getSettings(wizard);
    }
}

