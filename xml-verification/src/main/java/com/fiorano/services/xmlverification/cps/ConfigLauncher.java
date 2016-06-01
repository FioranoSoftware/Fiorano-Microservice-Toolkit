/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xmlverification.cps;

import com.fiorano.esb.server.api.service.config.wizard.GenericConfigWizard;
import com.fiorano.tools.studio.soa.application.model.PortInstanceType;
import com.fiorano.tools.utilities.Logger;
import com.fiorano.xmlverification.model.XmlVerificationPM;
import fiorano.esb.record.ESBRecordDefinition;
import org.eclipse.swt.widgets.Display;
import xmlverification.Activator;
import xmlverification.Messages_XmlVerification;

public class ConfigLauncher extends GenericConfigWizard {

    private static final String OUT_PORT_NAME = "OUT_PORT"; //$NON-NLS-1$

    public ConfigLauncher() {
        super(XmlVerificationPM.class);
    }

    @Override
    protected void onFinish() throws Exception {
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                try {
                    setOutPortSchema();
                } catch (Exception e) {
                    if (canFinish()) {
                        Logger.logException(Activator.PLUGIN_ID, Messages_XmlVerification.ConfigLauncher_0,
                                e);
                    } else {
                        Logger.logWarning(Activator.PLUGIN_ID, Messages_XmlVerification.ConfigLauncher_2,
                                e);
                    }
                }
            }
        });
        super.onFinish();
    }

    private void setOutPortSchema() throws Exception {
        String xsdStructures = ((XmlVerificationPM) getConfiguration())
                .getXSDStructures();
        if (XmlVerificationPM.BODY.equals(xsdStructures)
                || XmlVerificationPM.CONTEXT_BODY.equals(xsdStructures)) {
            ESBRecordDefinition schemaRec = ((XmlVerificationPM) getConfiguration())
                    .getBody();
            if (schemaRec != null) {
                PortInstanceType outPort = getServiceInstance().getPort(
                        OUT_PORT_NAME);
                getConfigurationHelper().getPortSchemaHelper().setSchema(
                        outPort, schemaRec);
            }
        }
    }
}
