/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.cps;

import com.fiorano.bc.display.model.Configuration;
import com.fiorano.bc.display.model.ConfigurationPM;
import com.fiorano.esb.server.api.service.config.wizard.GenericConfigWizard;


/**
 * @author Prakash G.R.
 *
 */
public class ConfigLauncher extends GenericConfigWizard {


	public ConfigLauncher() {
		super(ConfigurationPM.class);
	}

	public void addPages() {

		ConfigurationPM configuration = null;
		
		if (getConfiguration() instanceof Configuration) {
			configuration = new ConfigurationPM();
			configuration.setMaxBufferedMessages(((Configuration) getConfiguration()).getMaxBufferedMessages());
		} else {
			configuration = (ConfigurationPM) getConfiguration();
		}
		
		ConfigPage configPage = new ConfigPage(configuration);
		addPage(configPage);
	}

    protected void initConfigurationObjects(){};

}
