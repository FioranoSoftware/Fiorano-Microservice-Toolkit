/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.cps.swing;


import com.fiorano.bc.display.model.ConfigurationPM;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.cps.WizardPropertySheet;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.display.DisplayConfigurationSerializer;
import com.fiorano.services.display.cps.swing.panels.Bundle;
import com.fiorano.services.display.cps.swing.panels.DisplayInfoPanel;
import com.fiorano.services.display.cps.swing.panels.FinishWizardPanel;
import fiorano.esb.utils.RBUtil;
import fiorano.tifosi.common.TifosiException;

import java.util.logging.Level;

/**
 * Description of the Class
 *
 * @author Administrator
 * @version 2.0
 * @created April 17, 2002
 */
public class DisplayPropertySheet extends WizardPropertySheet {


    protected WizardStep[] createSteps(boolean readOnly) {
        WizardStep[] wizardSteps = new WizardStep[2];
        try {
            wizardSteps[0] = new DisplayInfoPanel((ConfigurationPM) configuration);
            wizardSteps[1] = new FinishWizardPanel((ConfigurationPM) configuration);
        } catch (TifosiException e) {
            logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.PS_SHOW_FAILED_DESC));
        }
        return wizardSteps;

    }

    @Override
    protected IServiceConfiguration getDefaultConfiguration() {
        return new ConfigurationPM();
    }

    protected IConfigurationSerializer createConfigurationSerializer() {
           return new DisplayConfigurationSerializer();
       }

}
