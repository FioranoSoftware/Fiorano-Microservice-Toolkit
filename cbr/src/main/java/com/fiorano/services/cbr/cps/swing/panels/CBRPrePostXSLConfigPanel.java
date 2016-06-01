/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps.swing.panels;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.common.editors.xsl.XSLConfigurationPanel;
import org.openide.WizardValidationException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by laxman on 4/21/2015.
 *
 * @bundle $class.title= Pre/Post Processing XSL Configuration
 * @bundle $class.summary=Configure Pre/Post Processing XSL configurations
 */
public class CBRPrePostXSLConfigPanel extends WizardStep {
    private JPanel panel;

    public CBRPrePostXSLConfigPanel(CBRPropertyModel model) {
        panel = new JPanel(new GridLayout(2, 1));
        XSLConfigurationPanel inputPanel = new XSLConfigurationPanel(model.getInputXSLConfiguration());
        XSLConfigurationPanel outputPanel = new XSLConfigurationPanel(model.getOutputXSLConfiguration());
        inputPanel.setBorder(new TitledBorder("Pre Processing XSL Configuration"));
        outputPanel.setBorder(new TitledBorder("Post Processing XSL Configuration"));
        panel.add(inputPanel);
        panel.add(outputPanel);
    }

    @Override
    protected Component createComponent() {
        return panel;
    }

    @Override
    public void fastValidate() throws WizardValidationException {

    }

    @Override
    public void lazyValidate() throws WizardValidationException {

    }
}
