/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps;

import org.eclipse.swt.widgets.Display;

import com.fiorano.bc.feeder.ConfigurationConverter;
import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.tools.studio.soa.application.model.OutputPortInstanceType;
import com.fiorano.tools.studio.soa.application.model.ServiceInstanceType;
import com.fiorano.esb.server.api.service.config.wizard.GenericConfigWizard;
import com.fiorano.services.common.exception.ServiceException;
import com.fiorano.services.cps.ui.schema.SchemaController;
import com.fiorano.tools.utilities.Logger;

import feeder.Activator;
import feeder.Messages_Feeder;
import fiorano.esb.record.ESBRecordDefinition;

/**
 * 
 *@author geetha
 */
public class ConfigLauncher extends GenericConfigWizard {

	private ConfigPage configPage;
	private MessagePropertiesPage propertiesPage;
	private static final String OUT_PORT_NAME = "OUT_PORT"; //$NON-NLS-1$

	public ConfigLauncher() {
		super(FeederPM.class);
	}

	public void addPages() {
		FeederPM configuration = ConfigurationConverter
				.convert(getConfiguration());
		configPage = new ConfigPage(configuration, getConfigurationHelper());
		addPage(configPage);
		propertiesPage = new MessagePropertiesPage(
				configuration);

		addPage(propertiesPage);
	}

	@Override
	protected void initConfig() throws Exception {
		// TODO Auto-generated method stub
		super.initConfig();
		setSchemaFromPort();
	}

	protected void initConfigurationObjects() {
	}

	@Override
	protected String getConfigurationData() throws Exception {
		SchemaController schemaController = configPage.getController();
		FeederPM config = ConfigurationConverter.convert(getConfiguration());
		if (config.getMessageFormat() == FeederPM.XML) {
			ESBRecordDefinition recDef = schemaController.getSchemaDefinition();
			config.setSchema(recDef);
		} else {
			config.setSchema(null);
		}
		setSchemaOnPort();
		return super.getConfigurationData();
	}

	private void setSchemaOnPort() {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				FeederPM config = ConfigurationConverter
						.convert(getConfiguration());
				ServiceInstanceType serviceInstance = getServiceInstance();

				for (OutputPortInstanceType portInstance : serviceInstance
						.getOutputportInstances().getOutputportInstance()) {
					if (OUT_PORT_NAME.equals(portInstance.getName())) {
						try {
							getConfigurationHelper()
									.getPortSchemaHelper()
									.setSchema(portInstance, config.getSchema());
						} catch (ServiceException e) {
							Logger.logException(Activator.PLUGIN_ID, e);
						}
					}
				}
			}
		});
	}

	private void setSchemaFromPort() {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				FeederPM config = ConfigurationConverter
						.convert(getConfiguration());
				ServiceInstanceType serviceInstance = getServiceInstance();

				for (OutputPortInstanceType portInstance : serviceInstance
						.getOutputportInstances().getOutputportInstance()) {
					if (OUT_PORT_NAME.equals(portInstance.getName())) {
						try {
							config
									.setSchema((ESBRecordDefinition) getConfigurationHelper()
											.getPortSchemaHelper().getSchema(
													portInstance));
						} catch (ServiceException e) {
							Logger.logException(Activator.PLUGIN_ID, e);
						}
					}
				}
			}
		});
	}

	@Override
	public boolean performFinish() {
		propertiesPage.updateConfiguration();
		return super.performFinish();
	}
}
