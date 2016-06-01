/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps;

import cache.Activator;
import cache.Messages_Cache;
import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.edbc.cache.configuration.FieldDefinitions;
import com.fiorano.edbc.cache.configuration.XSDGenerator;
import com.fiorano.esb.server.api.service.config.wizard.GenericConfigWizard;
import com.fiorano.services.cache.CacheConstants;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.tools.studio.soa.application.model.PortInstanceType;
import com.fiorano.tools.utilities.Logger;
import fiorano.esb.record.ESBRecordDefinition;
import org.eclipse.swt.widgets.Display;

public class ConfigLauncher extends GenericConfigWizard {

    public ConfigLauncher() {
        super(CachePM.class);
    }

    @Override
    protected void onFinish() throws Exception {
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                try {
                    setPortInfo();
                } catch (Exception e) {
                    if (canFinish()) {
                        Logger.logException(Activator.PLUGIN_ID, RBUtil.getMessage(com.fiorano.services.cache.cps.swing.editors.panels.Bundle.class, com.fiorano.services.cache.cps.swing.editors.panels.Bundle.UNABLE_TO_SET_PORT_XSD),
                                e);
                    } else {
                        Logger.logWarning(Activator.PLUGIN_ID, Messages_Cache.ConfigLauncher_1,
                                e);
                    }
                }
            }
        });
        super.onFinish();
    }

    private void setPortInfo() throws Exception {
        populateSchemaOnPort(CacheConstants.ADD_PORT);
        populateSchemaOnPort(CacheConstants.DEL_PORT);
        populateSchemaOnPort(CacheConstants.OUT_PORT);
    }

    private void populateSchemaOnPort(String portName) throws Exception {

        final FieldDefinitions fieldDefinitions = ((CachePM) getConfiguration())
                .getFieldDefinitions();

        if (fieldDefinitions != null) {
            PortInstanceType portInstanceType = getServiceInstance().getPort(
                    portName);
            ESBRecordDefinition esbRecordDefinition = XSDGenerator.getSchema(
                    fieldDefinitions, portName);
            getConfigurationHelper().getPortSchemaHelper().setSchema(
                    portInstanceType, esbRecordDefinition);
        }
    }
}
