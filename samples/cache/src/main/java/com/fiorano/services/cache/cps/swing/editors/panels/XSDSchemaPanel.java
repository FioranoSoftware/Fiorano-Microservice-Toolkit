/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.cps.swing.editors.panels;

import com.fiorano.adapter.jca.editors.schema.SchemaController;
import com.fiorano.adapter.jca.editors.schema.SchemaEditor;
import com.fiorano.adapter.jca.editors.schema.SchemaEditorInterface;
import com.fiorano.adapter.jca.editors.schema.SchemaModel;
import com.fiorano.services.common.swing.ConfigurationPanel;
import com.fiorano.util.ErrorListener;
import fiorano.esb.record.ESBRecordDefinition;

import javax.swing.*;
import java.awt.*;


/**
 * Created by IntelliJ IDEA.
 * User: ramesh
 * Date: 09/13/12
 * Time: 7:39 PM
 * To change this template use File | Settings | File Templates.
 */


public class XSDSchemaPanel extends ConfigurationPanel<ESBRecordDefinition> {

    private SchemaEditorInterface iSchemaEditor = null;
    private SchemaModel schemaModel;
    private SchemaController controller;
    private ESBRecordDefinition config;

    public XSDSchemaPanel() {
        super(new BorderLayout());
        createUI();
    }

    private void createUI() {
        setBorder(BorderFactory.createTitledBorder("XSD Schema Editor"));

        ESBRecordDefinition recordDefinition = new ESBRecordDefinition();
        schemaModel = new SchemaModel(recordDefinition);
        schemaModel.addOption(SchemaModel.OPTION_ALLOW_EMPTY_DEFINITION, Boolean.TRUE);
        schemaModel.addOption(SchemaModel.OPTION_DTD_SUPPORTED, Boolean.FALSE);
        controller = new SchemaController(schemaModel, iSchemaEditor);
        SchemaEditor schemaEditor = (SchemaEditor) controller.getSchemaEditor();
        add(schemaEditor, BorderLayout.NORTH);
    }

    @Override
    public ESBRecordDefinition getConfiguration() {
        config = schemaModel.getRecordDefFromModel();
        return config;
    }

    @Override
    public void loadConfiguration(ESBRecordDefinition configuration) {
        if (configuration == null) {
            configuration = new ESBRecordDefinition();
            return;
        }
        this.config = configuration;
        schemaModel.load(config);
        controller.loadModel();
    }

    @Override
    public void validate(ErrorListener errorListener) {

    }

}
