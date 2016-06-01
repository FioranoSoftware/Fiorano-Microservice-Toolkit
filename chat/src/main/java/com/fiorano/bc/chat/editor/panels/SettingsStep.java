/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat.editor.panels;

import com.fiorano.bc.chat.model.ChatPM;
import com.fiorano.bc.chat.Bundle;
import com.fiorano.openide.wizard.WizardPanel;
import com.fiorano.openide.wizard.WizardStep;
import fiorano.esb.util.CPSUtil;
import fiorano.esb.utils.RBUtil;
import org.openide.WizardValidationException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.logging.Level;

/**
 * Specify font to be used for messages
 *
 * @author FSIPL
 * @version 1.0
 * @created June 29, 2005
 * @bundle $class.title=Font Properties
 * @bundle $class.summary=Specify font to be used for messages
 */
public class SettingsStep extends WizardStep {
    private ChatPM m_propertyModel;

    private SettingsPanel m_panel = null;

    /**
     * @param propertyModel
     */
    public SettingsStep(ChatPM propertyModel) {
        m_propertyModel = propertyModel;
    }

    /**
     * Validate Component
     */
    public void fastValidate() throws WizardValidationException {
    }

    /**
     * @exception WizardValidationException
     */
    public void lazyValidate() throws WizardValidationException {
    }

    /**
     * @return
     */
    public Component createComponent() {
        if (m_panel == null) {
            m_panel = new SettingsPanel();
        }

        return m_panel;
    }

    /**
     * @exception Exception
     */
    /**
     * @exception Exception
     */
    public void model2Component() throws Exception {
        m_panel.model2Component(m_propertyModel);
    }

    public void component2Model() {
        m_panel.component2Model(m_propertyModel);
    }

    /**
     * The actual settings panel
     *
     * @author FSIPL
     * @version 1.0
     * @created June 30, 2005
     */
    class SettingsPanel extends WizardPanel {
        private BorderLayout borderLayout1 = new BorderLayout();
        private JPanel jPanel1 = new JPanel();
        private GridBagLayout gridBagLayout1 = new GridBagLayout();
        private FontPanel fontPanel1 = new FontPanel();
        private FontPanel inFontPanel = new FontPanel();
        private FontPanel outFontPanel = new FontPanel();
        private Border border2;
        private TitledBorder titledBorder1;
        private Border border3;
        private TitledBorder titledBorder2;

        /**
         */
        public SettingsPanel() {
            try {
                jbInit();
            }
            catch (Exception ex) {
                CPSUtil.getAnonymousLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class,Bundle.ERROR_INITIALISING_SETTINGS_PANEL), ex);
            }
        }

        void jbInit() throws Exception {
            border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
            titledBorder1 = new TitledBorder(border2, "Incoming Message");
            border3 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
            titledBorder2 = new TitledBorder(border3, "Outgoing Message");
            this.setLayout(borderLayout1);
            jPanel1.setLayout(gridBagLayout1);
            inFontPanel.setBorder(titledBorder1);
            outFontPanel.setBorder(titledBorder2);
            this.add(jPanel1, BorderLayout.CENTER);
            jPanel1.add(inFontPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(15, 0, 0, 0), 0, 0));
            jPanel1.add(outFontPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                    , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 0), 0, 0));
        }

        void model2Component(ChatPM model) {
            try {
                inFontPanel.nameCombo.setSelectedItem(model.getInFontName());
                inFontPanel.sizeCombo.setSelectedItem(model.getInFontSize());
                inFontPanel.colorButton.setForeground(new Color(Integer.parseInt(model.getInFontColor())));

                int style = Integer.parseInt(model.getInFontStyle());

                inFontPanel.bold.setSelected((style & Font.BOLD) != 0);
                inFontPanel.italic.setSelected((style & Font.ITALIC) != 0);
            }
            catch (NumberFormatException ignore) {
            }

            try {
                outFontPanel.nameCombo.setSelectedItem(model.getOutFontName());
                outFontPanel.sizeCombo.setSelectedItem(model.getOutFontSize());
                outFontPanel.colorButton.setForeground(new Color(Integer.parseInt(model.getOutFontColor())));

                int style = Integer.parseInt(model.getOutFontStyle());

                outFontPanel.bold.setSelected((style & Font.BOLD) != 0);
                outFontPanel.italic.setSelected((style & Font.ITALIC) != 0);
            }
            catch (NumberFormatException ignore) {
            }
        }

        void component2Model(ChatPM model) {
            model.setInFontName(inFontPanel.nameCombo.getSelectedItem().toString());
            model.setInFontSize(inFontPanel.sizeCombo.getSelectedItem().toString());
            model.setInFontColor(inFontPanel.colorButton.getForeground().getRGB() + "");

            int style = Font.PLAIN;

            if (inFontPanel.bold.isSelected()) {
                style |= Font.BOLD;
            }
            if (inFontPanel.italic.isSelected()) {
                style |= Font.ITALIC;
            }
            model.setInFontStyle(style + "");

            model.setOutFontName(outFontPanel.nameCombo.getSelectedItem().toString());
            model.setOutFontSize(outFontPanel.sizeCombo.getSelectedItem().toString());
            model.setOutFontColor(outFontPanel.colorButton.getForeground().getRGB() + "");
            style = Font.PLAIN;
            if (outFontPanel.bold.isSelected()) {
                style |= Font.BOLD;
            }
            if (outFontPanel.italic.isSelected()) {
                style |= Font.ITALIC;
            }
            model.setOutFontStyle(style + "");
        }
    }
}
