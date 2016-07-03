/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps;

import cbr.Messages_CBR;
import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.esb.server.api.service.config.wizard.GenericConfigWizard;
import com.fiorano.services.cps.ui.schema.SchemaController;
import com.fiorano.tools.studio.soa.application.model.InputPortInstanceType;
import com.fiorano.tools.studio.soa.application.model.OutputPortInstanceType;
import com.fiorano.tools.studio.soa.application.model.PortInstanceType;
import com.fiorano.tools.studio.soa.application.model.custom.ApplicationFactory;
import fiorano.esb.record.ESBRecordDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Venkat
 * 
 */
public class ConfigLauncher extends GenericConfigWizard {

    SchemaSettingsPage schemaSettingsPage;
    XPathSettingsPage xpathSettingsPage;
    AdditionalSettingsPage additionalSettingsPage;

    public ConfigLauncher() {
        super(CBRPropertyModel.class);
    }

    public void addPages() {

        CBRPropertyModel configuration = (CBRPropertyModel) getConfiguration();

        xpathSettingsPage = new XPathSettingsPage(configuration);
        additionalSettingsPage = new AdditionalSettingsPage(configuration);
        schemaSettingsPage = new SchemaSettingsPage(configuration);

        addPage(schemaSettingsPage);
        addPage(xpathSettingsPage);
        addPage(additionalSettingsPage);
    }

    protected void initConfigurationObjects() {};

    @SuppressWarnings("unchecked")
    protected void onFinish() throws Exception {

        CBRPropertyModel configuration = (CBRPropertyModel) getConfiguration();

        List<OutputPortInstanceType> outputPortInstances = null;
        if (getServiceInstance().getOutputportInstances() != null) {
            outputPortInstances = getServiceInstance().getOutputportInstances()
                    .getOutputportInstance();
            clearPorts(outputPortInstances);
        }

        List<InputPortInstanceType> inputPortInstances = null;
        if (getServiceInstance().getInputportInstances() != null) {
            inputPortInstances = getServiceInstance().getInputportInstances()
                    .getInputportInstance();
            clearPorts(inputPortInstances);
        }

        InputPortInstanceType inPortInstance = ApplicationFactory.INSTANCE
                .createInputPortInstanceType();
        inPortInstance.setName(Messages_CBR.ConfigLauncher_0);
        ESBRecordDefinition schemaDefinition = configuration.getApplyOnXPath() ? null
                : configuration.getSchemaDefinition();
        getConfigurationHelper().getPortSchemaHelper().setSchema(
                inPortInstance, schemaDefinition);
        if (inputPortInstances != null) {
            inputPortInstances.add(inPortInstance);
        }

        ArrayList<String> portNames = configuration.getOutPortNames();
        for (String port : portNames) {
            OutputPortInstanceType outputPortInstance = ApplicationFactory.INSTANCE
                    .createOutputPortInstanceType();
            outputPortInstance.setName(port);
            outputPortInstance.setDescription(Messages_CBR.ConfigLauncher_1
                    + configuration.getXPaths().get(portNames.indexOf(port)));
            getConfigurationHelper().getPortSchemaHelper().setSchema(
                    outputPortInstance, schemaDefinition);
            if (outputPortInstances != null) {
                outputPortInstances.add(outputPortInstance);
            }
        }
        OutputPortInstanceType outPortInstance = ApplicationFactory.INSTANCE
                .createOutputPortInstanceType();
        outPortInstance.setName(Messages_CBR.ConfigLauncher_2);
        getConfigurationHelper().getPortSchemaHelper().setSchema(
                outPortInstance, schemaDefinition);
        outPortInstance.setDescription(Messages_CBR.ConfigLauncher_3);
        if (outputPortInstances != null) {
            outputPortInstances.add(outPortInstance);
        }
        // cpsHelper.setOutputPortInstanceTypes(outputPorts);

        super.onFinish();
    }

    private void clearPorts(List<? extends PortInstanceType> portInstances) {
        while (!portInstances.isEmpty()) {
            portInstances.remove(0);
        }
    }

    @Override
    protected String getConfigurationData() throws Exception {

        SchemaController schemaController = schemaSettingsPage.getController();
        CBRPropertyModel config = (CBRPropertyModel) getConfiguration();

        ESBRecordDefinition recDef = schemaController.getSchemaDefinition();
        if (recDef != null) {
            config.setSchemaDefinition(recDef);
        }

        return super.getConfigurationData();
    };
}
