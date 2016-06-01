/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.cps;

import com.fiorano.esb.server.api.service.config.wizard.GenericConfigWizard;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.common.util.ConnectedPortKey;
import com.fiorano.services.distributionservice.configuration.DistributionConfigurationSerializer;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;
import com.fiorano.tools.studio.soa.application.model.OutputPortInstanceType;
import com.fiorano.tools.studio.soa.application.model.PortInstanceType;
import com.fiorano.tools.studio.soa.application.model.custom.ApplicationFactory;
import com.fiorano.tools.utilities.Logger;
import com.fiorano.util.StringUtil;
import distributionservice.Activator;
import distributionservice.Messages_Distribution;

import java.util.List;
import java.util.Map;

/**
 * @author Prakash G.R.
 * 
 */
public class ConfigLauncher extends GenericConfigWizard {
	
	private ConfigPage configPage;

	public ConfigLauncher() {
		super(DistributionServicePM.class);
	}

	private DistributionConfigurationSerializer serializer = new DistributionConfigurationSerializer();

	@Override
	protected void readConfig() throws Exception {
		configuration = serializer.deserializeFromString(readConfigString());
	}

	@Override
	protected String getConfigurationData() throws Exception {
		return configuration != null ? serializer.serializeToString((DistributionServicePM) configuration) : null;
	}

	@Override
	public void addPages() {
		configPage = new ConfigPage((DistributionServicePM) configuration);
		addPage(configPage);
	}

	@Override
	public boolean performFinish() {

		configPage.updateConfiguration();

		int portCount = ((DistributionServicePM) configuration).getPortCount();

		List<OutputPortInstanceType> outputPorts = getServiceInstance().getOutputportInstances().getOutputportInstance();
		//outputPorts.clear();
        for (int i = outputPorts.size() - 1; i > portCount; i--) {
                outputPorts.remove(i);
        }

		for (int i = 0; i < portCount; ++i) {
            PortInstanceType portInstance = getServiceInstance().getPort("OUT_PORT_" + i);
            if (!outputPorts.contains(portInstance))
            {
                OutputPortInstanceType outPort = ApplicationFactory.INSTANCE.createOutputPortInstanceType();
                outPort.setName("OUT_PORT_" + i); //$NON-NLS-1$
                outPort.setDescription(Messages_Distribution.ConfigDialog_7);
                outputPorts.add(outPort);
            }
        }

        if (((DistributionServicePM) configuration).isPropagateSchema()) {
            try {
                Map<ConnectedPortKey, ISchema> connectedPortSchemas = configurationHelper.fetchSchemasConnectedToInputPorts();

                /* to propagate schema of components connected to output ports of this service
                if (connectedPortSchemas != null) {
                    for (ConnectedPortKey key : connectedPortSchemas.keySet()) {
                        ApplicationType application = configurationHelper.getApplicationType();

                        for (RouteType route : application.getRoutes().getRoute()) {
                            if (route.getSource().getInst().equals(configurationHelper.getServiceInstanceType().getName())) {
                                if (route.getTarget().getPort().equals(key.getConnectedPortName()) && route.getTarget().getInst().equals(key.getServiceInstanceName())) {
                                    PortInstanceType outputPort = configurationHelper.getServiceInstanceType().getPort(route.getSource().getPort());
                                    ISchema schema = connectedPortSchemas.get(key);
                                    if (schema != null && !StringUtil.isEmpty(schema.getStructure())) {
                                        configurationHelper.getPortSchemaHelper().setSchema(outputPort, schema);
                                    }
                                }
                            }
                        }
                    }
                }*/

                if (connectedPortSchemas != null) {
                    for (ISchema schema : connectedPortSchemas.values()) {
                        if (schema != null && !StringUtil.isEmpty(schema.getStructure())) {
                            for (OutputPortInstanceType port : outputPorts) {
                                configurationHelper.getPortSchemaHelper().setSchema(port, schema);
                            }
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                Logger.logException(Activator.PLUGIN_ID, Messages_Distribution.ConfigLauncher_0, e);
                return false;
            }
        }

        return super.performFinish();
	}
}
