/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.cps.swing;

import com.fiorano.edbc.framework.service.cps.steps.ConfigurationStep;
import com.fiorano.openide.wizard.WizardUtil;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;

/**
 * @bundle $class.title=Distribution Service Configuration
 * @bundle $class.summary=Specify number of ports and port weights
 */
public class DistributionConfigurationStep extends ConfigurationStep<DistributionServicePM> {
    public DistributionConfigurationStep(boolean readOnly) {
        super(readOnly, new DistributionConfigurationPanel());
    }

    @Override
    protected DistributionServicePM fetchConfigurationToLoad() {
        return (DistributionServicePM) WizardUtil.getSettings(wizard);
    }

    @Override
    protected void updateConfiguration(DistributionServicePM configuration) {
        DistributionServicePM model = (DistributionServicePM) WizardUtil.getSettings(wizard);
        model.setPortCount(configuration.getPortCount());
        model.setPortWeights(configuration.getPortWeights());
        model.setPropagateSchema(configuration.isPropagateSchema());
    }
}
