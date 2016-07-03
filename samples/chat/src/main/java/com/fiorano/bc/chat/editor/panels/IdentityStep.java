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
import java.awt.*;
import java.util.logging.Level;

/**
 * Information used to identify the user
 *
 * @author FSIPL
 * @version 1.0
 * @created June 29, 2005
 * @bundle $class.title=User Information
 * @bundle $class.summary=Information used to identify the user
 */
public class IdentityStep extends WizardStep {
    private ChatPM m_propertyModel;

    private IdentityPanel m_panel = null;

    /**
     * @param propertyModel
     */
    public IdentityStep(ChatPM propertyModel) {
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
            m_panel = new IdentityPanel();
        }

        return m_panel;
    }

    /**
     * @exception Exception
     */
    public void model2Component()
            throws Exception {
        m_panel.model2Component(m_propertyModel);
    }

    /**
     */
    /**
     */
    public void component2Model() {
        m_panel.component2Model(m_propertyModel);
    }

    /**
     * The actual Indentity Panel
     *
     * @author FSIPL
     * @version 1.0
     * @created June 30, 2005
     */
    class IdentityPanel extends WizardPanel {
        private BorderLayout borderLayout1 = new BorderLayout();
        private JPanel jPanel1 = new JPanel();
        private GridBagLayout gridBagLayout1 = new GridBagLayout();
        private JLabel jLabel2 = new JLabel();
        private JTextField nameField = new JTextField();
        private JLabel jLabel3 = new JLabel();
        private JLabel jLabel4 = new JLabel();
        private JTextField emailField = new JTextField();
        private JLabel jLabel5 = new JLabel();

        /**
         */
        public IdentityPanel() {
            try {
                jbInit();
            }
            catch (Exception ex) {
                CPSUtil.getAnonymousLogger().log(Level.SEVERE, RBUtil.getMessage(Bundle.class,Bundle.ERROR_INITIALISING_IDENTITY_PANEL),ex);
            }
        }

        /**
         * @exception WizardValidationException
         */
        public void lazyValidate() throws WizardValidationException {
            if (nameField.getText().trim().length() == 0) {
                throw new WizardValidationException(nameField, RBUtil.getMessage(Bundle.class, Bundle.NAME_MISSING),
                                                    RBUtil.getMessage(Bundle.class, Bundle.NAME_MISSING_DESC));
            }
            if (emailField.getText().trim().length() == 0) {
                throw new WizardValidationException(emailField, RBUtil.getMessage(Bundle.class, Bundle.EMAIL_MISSING),
                                                    RBUtil.getMessage(Bundle.class, Bundle.EMAIL_MISSING_DESC));
            }
            if (emailField.getText().trim().indexOf("@") == -1 ||
                emailField.getText().trim().indexOf(".") == -1) {
                throw new WizardValidationException(emailField, RBUtil.getMessage(Bundle.class, Bundle.INVALID_EMAIL),
                                                    RBUtil.getMessage(Bundle.class, Bundle.INVALID_EMAIL_DESC));
            }
        }

        /**
         * @param model
         * @exception Exception
         */
        public void model2Component(ChatPM model) throws Exception {
            nameField.setText(model.getDisplayName());
            emailField.setText(model.getEmailAddress());
        }

        /**
         * @param model
         */
        public void component2Model(ChatPM model) {
            model.setDisplayName(nameField.getText().trim());
            model.setEmailAddress(emailField.getText().trim());
        }

        void jbInit() throws Exception {
            this.setLayout(borderLayout1);
            jPanel1.setLayout(gridBagLayout1);
            jLabel2.setText("Display Name");
            jLabel3.setText("Ex: Aryton");
            jLabel4.setText("Email Address");
            jLabel5.setText("Ex: ayrton@fiorano.com");
            this.add(jPanel1, BorderLayout.CENTER);
            jPanel1.add(jLabel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
            jPanel1.add(nameField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20, 5, 0, 0), 0, 0));
            jPanel1.add(jLabel3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                    , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
            jPanel1.add(jLabel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
            jPanel1.add(emailField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 0));
            jPanel1.add(jLabel5, new GridBagConstraints(1, 3, 1, 1, 0.0, 1.0
                    , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
        }
    }
}
