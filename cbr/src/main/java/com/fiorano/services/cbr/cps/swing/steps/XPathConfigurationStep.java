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
import com.fiorano.services.cbr.cps.swing.panels.XPathConfigurationPanel;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * @bundle $class.title=Routing Rules
 * @bundle $class.summary=Enter Routing Rules
 */
public class XPathConfigurationStep extends ConfigurationStep<CBRPropertyModel> {
    private XPathConfigurationPanel xPathConfigurationPanel;

    public XPathConfigurationStep(boolean readOnly, CPSESBUtil cpsesbUtil) {
        super(readOnly, new XPathConfigurationPanel());
        try {
            xPathConfigurationPanel = ((XPathConfigurationPanel) getComponent());
            xPathConfigurationPanel.jbInit(cpsesbUtil);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void setResourceResolver(LSResourceResolver ls) {
        xPathConfigurationPanel.setResolverResolver_(ls);
    }

    protected CBRPropertyModel fetchConfigurationToLoad() {
        return (CBRPropertyModel) WizardUtil.getSettings(wizard);
    }

    protected void updateConfiguration(CBRPropertyModel configuration) {
        WizardUtil.getSettings(wizard);
    }
}

 