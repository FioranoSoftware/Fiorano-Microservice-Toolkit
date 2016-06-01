/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps.swing;

import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.cps.WizardPropertySheet;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.edbc.framework.service.internal.transport.Constants;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.feeder.FeederConfigurationSerializer;
import com.fiorano.services.feeder.cps.swing.panels.DefaultMessagePanel;
import com.fiorano.services.feeder.cps.swing.panels.FeederDTDPanel;
import com.fiorano.services.feeder.cps.swing.panels.HeaderPanel;

/**
 * Creates the CPS Wizard for Feeder
 *
 * @author Administrator
 * @version 2.0
 * @created April 17, 2002
 */
public class FeederPropertySheet extends WizardPropertySheet {

    @Override
    protected IServiceConfiguration getDefaultConfiguration() {
        return new FeederPM();
    }
    protected WizardStep[] createSteps(boolean readOnly) {
        FeederPM model = (FeederPM) configuration;
        WizardStep[] wizardSteps = new WizardStep[3];
        wizardSteps[0] = new FeederDTDPanel(readOnly, cpsESBUtil);
        wizardSteps[1] = new DefaultMessagePanel(model);
        wizardSteps[2] = new HeaderPanel(model);
        return wizardSteps;

    }
    /**
     * Services willing to take some action before CPS is closed should override this. Typcically operations
     * like creating ports or setting schemas on ports can be done here.
     *
     * @param finished   indicates whether CPS is finished or cancelled
     * @param cpsesbUtil {@link com.fiorano.esb.wrapper.CPSESBUtil}
     */
    @SuppressWarnings({"CastToConcreteClass"})
    protected void onClose(boolean finished, CPSESBUtil cpsesbUtil) {
        super.onClose(finished, cpsesbUtil);
        if (finished) {
            FeederPM config = (FeederPM) configuration;
            if (config.getMessageFormat() == FeederPM.XML) {
                cpsesbUtil.getServiceInstanceAdapter().getOutputPortInstance(Constants.OUT_PORT_NAME).setSchema(config.getSchema());
            } else {
                cpsesbUtil.getServiceInstanceAdapter().getOutputPortInstance(Constants.OUT_PORT_NAME).setSchema(null);
            }
        }
    }

    protected IConfigurationSerializer createConfigurationSerializer() {
        return new FeederConfigurationSerializer();
    }


}