/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.cps.swing;

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.configuration.IServiceConfiguration;
import com.fiorano.edbc.framework.service.cps.WizardPropertySheet;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.esb.wrapper.OutputPortInstanceAdapter;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.cbr.cps.swing.panels.CBRPrePostXSLConfigPanel;
import com.fiorano.services.cbr.cps.swing.panels.EncryptDecryptConfigPanel;
import com.fiorano.services.cbr.cps.swing.panels.NamespacePanel;
import com.fiorano.services.cbr.cps.swing.panels.ThreadPoolPanel;
import com.fiorano.services.cbr.cps.swing.steps.CBRXSDConfigurationStep;
import com.fiorano.services.cbr.cps.swing.steps.XPathConfigurationStep;
import fiorano.esb.record.ESBRecordDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: fiorano
 * Date: 27 Dec, 2010
 * Time: 7:47:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class CBRPropertySheet extends WizardPropertySheet {

    private CBRPropertyModel cbrPropertyModel;

    protected IServiceConfiguration getDefaultConfiguration() {
        return new CBRPropertyModel();
    }

    protected WizardStep[] createSteps(boolean readOnly) {
        this.cbrPropertyModel = (CBRPropertyModel) configuration;
        NamespacePanel nameSpacePanel = new NamespacePanel(cbrPropertyModel, cpsESBUtil);
        XPathConfigurationStep xPathConfigurationStep = new XPathConfigurationStep(readOnly, cpsESBUtil);
        EncryptDecryptConfigPanel encryptDecryptConfigPanel = new EncryptDecryptConfigPanel(cbrPropertyModel);
        CBRPrePostXSLConfigPanel xslConfigPanel = new CBRPrePostXSLConfigPanel(cbrPropertyModel);
        ThreadPoolPanel threadPoolPanel = new ThreadPoolPanel(cbrPropertyModel);
        WizardStep[] wizardSteps = new WizardStep[7];
        wizardSteps[0] = new CBRXSDConfigurationStep(cbrPropertyModel, readOnly, cpsESBUtil, nameSpacePanel, xPathConfigurationStep);
        wizardSteps[1] = nameSpacePanel;
        wizardSteps[2] = xPathConfigurationStep;
        wizardSteps[3] = encryptDecryptConfigPanel;
        wizardSteps[4] = xslConfigPanel;
        wizardSteps[5] = threadPoolPanel;
        return wizardSteps;

    }

    protected void onClose(boolean finished, CPSESBUtil cpsesbUtil) {
        super.onClose(finished, cpsesbUtil);
        cbrPropertyModel = (CBRPropertyModel) configuration;
        if (finished) {
            ArrayList outPorts = cbrPropertyModel.getOutPortNames();
            Set<OutputPortInstanceAdapter> outputPortInstanceAdapters = new HashSet<>();
            for (Object outPort : outPorts) {
                OutputPortInstanceAdapter outputPortInstanceAdapter = new OutputPortInstanceAdapter();
                outputPortInstanceAdapter.setName(outPort.toString());
                if (!(cbrPropertyModel.getApplyOnXPath())) {
                    outputPortInstanceAdapter.setSchema(cbrPropertyModel.getSchemaDefinition());
                }
                outputPortInstanceAdapters.add(outputPortInstanceAdapter);
            }
            OutputPortInstanceAdapter error = new OutputPortInstanceAdapter();
            error.setName("OUT_FALSE");
            if (!(cbrPropertyModel.getApplyOnXPath())) {
                error.setSchema(cbrPropertyModel.getSchemaDefinition());
            }
            outputPortInstanceAdapters.add(error);
            cpsesbUtil.getServiceInstanceAdapter().setOutputPortInstances(outputPortInstanceAdapters);
            if (!(cbrPropertyModel.getApplyOnXPath())) {
                cpsesbUtil.getServiceInstanceAdapter().getInputPortInstance(Constants.IN_PORT_NAME).setSchema(cbrPropertyModel.getSchemaDefinition());
            } else {
                cpsesbUtil.getServiceInstanceAdapter().getInputPortInstance(Constants.IN_PORT_NAME).setSchema(new ESBRecordDefinition());
            }
        }
    }
}


