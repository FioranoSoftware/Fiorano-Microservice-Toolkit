/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cbr.cps.swing.panels;

import com.fiorano.adapter.esb.editors.schema.ESBSchemaEditor;
import com.fiorano.adapter.jca.editors.schema.*;
import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.edbc.framework.service.Constants;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.services.cbr.cps.swing.Bundle;
import com.fiorano.services.cbr.cps.swing.steps.XPathConfigurationStep;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.swing.ConfigurationPanel;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.uif.wizard.WizardPanel;
import com.fiorano.util.ErrorListener;
import com.fiorano.util.StringUtil;
import fiorano.esb.record.ESBRecordDefinition;

import java.awt.*;


/**
 * <p>Title: CBR COMPONENT</p>
 * <p>Description: Panel to add Namespaces in CBR</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: Fiorano</p>
 *
 * @author Rohit
 * @author Deepthi
 * @version 1.21, 18 March 2009
 * @created August 04, 2006
 */

/**
 * @bundle $class.title=Schema
 * @bundle $class.summary=Enter Schema
 */
public class XSDPanel extends ConfigurationPanel<CBRPropertyModel> {
    private CBRPropertyModel model;
    private NamespacePanel nameSpacePanel;
    private XPathConfigurationStep xPathConfigurationStep;
    private SchemaController schemaController;
    private WizardPanel panel = new WizardPanel();

    public XSDPanel() {
        super(new BorderLayout());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension preferredSize = new Dimension((int) (screenSize.getWidth() * .5), (int) (screenSize.getHeight() * .5));
        setPreferredSize(preferredSize);
    }

    public void jbInit(CBRPropertyModel model, CPSESBUtil cpsesbUtil, NamespacePanel nameSpacePanel, XPathConfigurationStep xPathConfigurationStep) {
        this.model = model;
        this.nameSpacePanel = nameSpacePanel;
        this.xPathConfigurationStep = xPathConfigurationStep;
        ESBSchemaEditor cbrSchemaEditor = new ESBSchemaEditor(cpsesbUtil);
        ESBRecordDefinition oldXSD = cpsesbUtil.getServiceInstanceAdapter().getInputPortInstance(Constants.IN_PORT_NAME).getSchema();
        model.setSchemaDefinition(oldXSD);
        schemaController = new SchemaController(new SchemaModel(model.getSchemaDefinition()), cbrSchemaEditor);
        schemaController.getSchemaModel().addOption(SchemaModelInterface.OPTION_DTD_SUPPORTED, Boolean.FALSE);
        schemaController.getSchemaModel().addOption(SchemaModelInterface.OPTION_MANDATE_ROOT_ELEM, Boolean.TRUE);
        // not mandatory
        schemaController.getSchemaModel().addOption(SchemaModelInterface.OPTION_ALLOW_EMPTY_DEFINITION, Boolean.TRUE);
        schemaController.getSchemaModel().addEventListener(
                new SchemaModelListener() {
                    public void schemaModelChanged(SchemaModelEvent event) {
                        modelChanged(event);
                    }
                }
        );
        panel.setLayout(new GridBagLayout());
        panel.add(cbrSchemaEditor,
                new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHEAST,
                        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(panel);
    }


    private void modelChanged(SchemaModelEvent event) {
        if (event.getChange() == SchemaModelEvent.STRUCTURE_CHANGED)

        {
            nameSpacePanel.setStructureChanged(true);
        }
    }

    @Override
    public void loadConfiguration(CBRPropertyModel configuration) {
        this.model = configuration;

    }

    @Override
    public CBRPropertyModel getConfiguration() {

        ESBRecordDefinition esb = schemaController.getSchemaModel().getRecordDefFromModel();
        if (esb != null && !StringUtil.isEmpty(esb.getStructure())) {
            nameSpacePanel.addNameSpace(esb.getStructure(), esb, false);
            try {
                nameSpacePanel.validatePrefix();
                nameSpacePanel.saveNameSpace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            xPathConfigurationStep.setResourceResolver(esb);
        }
        model.setSchemaDefinition(esb);
        return model;
    }

    @Override
    public void validate(ErrorListener errorListener) {
        Exceptions exceptions = new Exceptions();
        if (schemaController.getSchemaModel() != null) {
            try {
                schemaController.getSchemaModel().validate();
            } catch (Exception e) {
                exceptions.add(new Exception(
                        RBUtil.getMessage(Bundle.class, Bundle.SCHEMA_VALIDATION_ERROR,
                                new Object[]{e.getLocalizedMessage()})));
            }
        }
        try {
            model.validateSchema(schemaController.getSchemaDefinition());
        } catch (ServiceConfigurationException e) {
            exceptions.add(new Exception(e.getMessage()));
        }

        try {
            for (Object e : exceptions) {
                errorListener.error((Exception) e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}