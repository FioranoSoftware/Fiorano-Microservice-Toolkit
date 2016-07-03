/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.feeder.cps.swing.panels;

import com.fiorano.adapter.esb.editors.schema.ESBSchemaEditor;
import com.fiorano.adapter.jca.editors.schema.SchemaController;
import com.fiorano.adapter.jca.editors.schema.SchemaModel;
import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.edbc.framework.service.cps.steps.ConfigurationStep;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.openide.wizard.WizardUtil;
import com.fiorano.services.common.swing.ConfigurationPanel;
import com.fiorano.util.ErrorListener;
import fiorano.esb.record.ESBRecordDefinition;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * Panel to specify the DTD/XSD of outgoing messages
 *
 * @author FSIPL
 * @version 1.0
 * @created April 18, 2005
 */

/**
 * @bundle $class.title=Message Format
 * @bundle $class.summary=Specify the DTD/XSD of outgoing messages
 */
public class FeederDTDPanel extends ConfigurationStep<FeederPM> {

    public FeederDTDPanel(boolean readOnly, CPSESBUtil cpsesbUtil) {
        super(readOnly, new FeederSchemaPanel());
        ((FeederSchemaPanel)getComponent()).createUI(cpsesbUtil);
    }

    protected FeederPM fetchConfigurationToLoad() {
        return (FeederPM) WizardUtil.getSettings(wizard);
    }

    protected void updateConfiguration(FeederPM configuration) {
         WizardUtil.getSettings(wizard);
    }
}

class FeederSchemaPanel extends ConfigurationPanel<FeederPM> {
    private JPanel jPanel2 = new JPanel(new GridBagLayout());
    private JLabel jLabel2 = new JLabel();
    private JRadioButton xmlRadio = new JRadioButton();
    private JRadioButton textRadio = new JRadioButton();
    private ButtonGroup buttonGroup1 = new ButtonGroup();
    private SchemaController controller = null;
    private FeederPM model;
    private ESBSchemaEditor feederSchemaEditor;

    FeederSchemaPanel() {
        super(new BorderLayout());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension preferredSize = new Dimension((int) (screenSize.getWidth() * .5), (int) (screenSize.getHeight() * .5));
        setPreferredSize(preferredSize);
    }

    public FeederPM getConfiguration() {
        model.setMessageFormat(xmlRadio.isSelected() ? 1 : 0);
        model.setSchema(controller.getSchemaDefinition() != null ? controller.getSchemaDefinition() : new ESBRecordDefinition());
        return model;
    }

    public void loadConfiguration(FeederPM configuration) {
        this.model = configuration;
        Integer format = model.getMessageFormat();

        if (FeederPM.XML==format) {
            xmlRadio.setSelected(true);
        } else {
            textRadio.setSelected(true);
        }

       controller.getSchemaModel().load(model.getSchema());
       controller.loadModel();
    }

    @SuppressWarnings({"unchecked"})
    public void validate(ErrorListener errorListener) {
        try {
            if (!xmlRadio.isSelected()) {
                return;
            }
            if (controller.getSchemaModel() != null) {
                controller.getSchemaModel().validate();
            }

            ESBRecordDefinition recordDef = controller.getSchemaDefinition();
            model.validateSchema(recordDef);
        } catch (Exception e) {
            try {
                errorListener.error(e);
            } catch (Exception e1) {
                //
            }
        }
    }


    public void createUI(CPSESBUtil cpsesbUtil) {
        System.setProperty("LAUNCH_MODE", "FEPO");
        feederSchemaEditor = new ESBSchemaEditor(cpsesbUtil);
        controller = new SchemaController(new SchemaModel(), feederSchemaEditor);
        jLabel2.setText("Output Message Format:   ");
        xmlRadio.setSelected(true);
        xmlRadio.setText("XML");
        xmlRadio.addChangeListener(
                new javax.swing.event.ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        feederSchemaEditor.setVisible(xmlRadio.isSelected());
                    }
                });
        textRadio.setText("Plain Text");
        textRadio.addChangeListener(
                new javax.swing.event.ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        feederSchemaEditor.setVisible(xmlRadio.isSelected());
                    }
                });
        buttonGroup1.add(xmlRadio);
        buttonGroup1.add(textRadio);

        add(jPanel2, BorderLayout.NORTH);
        jPanel2.add(jLabel2, new GridBagConstraints(0,0,2,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,5,2,5),0,0));
        jPanel2.add(xmlRadio,  new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(2,5,2,5),0,0));
        jPanel2.add(textRadio,  new GridBagConstraints(3,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(2,5,2,5),0,0));
        add(feederSchemaEditor, BorderLayout.CENTER);

    }
}
