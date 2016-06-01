/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.cps.swing;

import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.cps.WizardPropertySheet;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import com.fiorano.esb.wrapper.QPortName;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.distributionservice.DistributionServiceConstants;
import com.fiorano.services.distributionservice.configuration.DistributionConfigurationSerializer;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;
import com.fiorano.util.StringUtil;
import fiorano.esb.record.ESBRecordDefinition;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration Property sheet for the distribution service
 *
 * @author FSIPL
 * @version 1.0
 * @created March 28, 2006
 */
public class DistributionServicePropertySheet extends WizardPropertySheet {

    @Override
    protected IServiceConfiguration getDefaultConfiguration() {
        return new DistributionServicePM();
    }

    @Override
    protected WizardStep[] createSteps(boolean readOnly) {
        WizardStep[] wizardSteps = new WizardStep[1];
        wizardSteps[0] = new DistributionConfigurationStep(readOnly);
        return wizardSteps;
    }

    @Override
    protected void onClose(boolean finished, CPSESBUtil cpsesbUtil) {
        int portCount = ((DistributionServicePM) configuration).getPortCount();
        Set<OutputPortInstanceAdapter> outputPortInstanceAdapters = new LinkedHashSet<>();
        for (int i = 0; i < portCount; ++i) {
            OutputPortInstanceAdapter outputPortInstanceAdapter = new OutputPortInstanceAdapter();
            outputPortInstanceAdapter.setName(DistributionServiceConstants.OUT_PORT_PREFIX + i);
            outputPortInstanceAdapters.add(outputPortInstanceAdapter);
        }
        cpsesbUtil.getServiceInstanceAdapter().setOutputPortInstances(outputPortInstanceAdapters);

        if (((DistributionServicePM) configuration).isPropagateSchema()) {
            Map<String, Map<QPortName, ESBRecordDefinition>> connectedPortSchemas = cpsesbUtil.fetchConnectedOutputPortSchemas();
            if (connectedPortSchemas != null) {
                for (String port : connectedPortSchemas.keySet()) {
                    Map<QPortName, ESBRecordDefinition> map = connectedPortSchemas.get(port);
                    for (ESBRecordDefinition schema : map.values()) {
                        if (schema != null && !StringUtil.isEmpty(schema.getStructure())) {
                            for (Object outPortInstance : cpsesbUtil.getServiceInstanceAdapter().getOutputPortInstances()) {
                                ((OutputPortInstanceAdapter) outPortInstance).setSchema(schema);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected IConfigurationSerializer createConfigurationSerializer() {
        return new DistributionConfigurationSerializer();
    }
}