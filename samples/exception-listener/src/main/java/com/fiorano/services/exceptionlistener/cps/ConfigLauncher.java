/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.exceptionlistener.cps;

import com.fiorano.esb.server.api.service.config.wizard.GenericConfigWizard;
import com.fiorano.services.exceptionlistener.configuration.ExceptionListenerConfiguration;

public class ConfigLauncher extends GenericConfigWizard {

    public ConfigLauncher() {
        super(ExceptionListenerConfiguration.class);
    }

    @Override
    protected String getConfigurationData() throws Exception {
/*		if (configuration != null) {
            ((ExceptionListenerPM) configuration).encryptPasswords();
		}*/
        return super.getConfigurationData();
    }

    @Override
    protected void readConfig() throws Exception {
        super.readConfig();
		/*if (configuration != null) {
			((ExceptionListenerPM) configuration).decryptPasswords();
		}*/
    }
}
