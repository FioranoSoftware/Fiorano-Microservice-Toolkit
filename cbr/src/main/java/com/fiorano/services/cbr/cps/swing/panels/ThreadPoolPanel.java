/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.cps.swing.panels;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.uif.wizard.WizardPanel;
import org.openide.WizardValidationException;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * Date: Apr 13, 2008
 * Time: 1:01:59 AM
 * To change this template use File | Settings | File Templates.
 *
 * @author Venkat
 * @version 1.0, 3 April 2008
 */

/**
 * @bundle $class.title=Thread Pool Details
 * @bundle $class.summary=Enter  Details
 */
public class ThreadPoolPanel extends WizardStep {
    private CBRPropertyModel model;
    private ThreadPoolConfigurationPanel threadPoolConfigurationPanel;
    private WizardPanel panel = new WizardPanel();

    public ThreadPoolPanel(CBRPropertyModel propertyModel) {
        panel.setLayout(new BorderLayout());
        this.model = propertyModel;
        threadPoolConfigurationPanel = new ThreadPoolConfigurationPanel(model);
        panel.add(threadPoolConfigurationPanel, BorderLayout.NORTH);
    }

    public void model2Component() {
        threadPoolConfigurationPanel.loadConfiguration(model);
    }

    public void component2Model() {
        CBRPropertyModel cbrPropertyModel = threadPoolConfigurationPanel.getConfiguration();
    }

    @Override
    public void fastValidate() throws WizardValidationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void lazyValidate() throws WizardValidationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected WizardPanel createComponent() {
        return panel;
    }
}