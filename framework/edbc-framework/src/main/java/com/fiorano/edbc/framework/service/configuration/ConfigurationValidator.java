/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.service.CollectingErrorListener;
import com.fiorano.swing.layout.EqualsLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ConfigurationValidator {

    public static boolean validateConfiguration(IServiceConfigurationDetail configuration) {
        CollectingErrorListener errorListener = new CollectingErrorListener();
        try {
            configuration.validate(errorListener);
        } catch (ServiceConfigurationException e) {
            //ignore this should not happen
            e.printStackTrace();
        }
        Exceptions exceptions = errorListener.getCollectedExceptions();
        if (exceptions == null || exceptions.isEmpty()) {
            return true;
        }
        final JDialog errorDialog = new JDialog();
        errorDialog.setTitle("Validation Errors");
        errorDialog.setModal(true);
        JTextArea textArea = new JTextArea(10, 80);
        textArea.setEditable(false);
        textArea.setText(exceptions.getMessage());
        textArea.setBackground(new JLabel().getBackground());
        errorDialog.getContentPane().setLayout(new BorderLayout());
        errorDialog.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        JPanel errorDlgButtonPanel = new JPanel(new EqualsLayout(5));
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                errorDialog.setVisible(false);
            }
        });
        errorDlgButtonPanel.add(okButton);
        errorDialog.setLocationRelativeTo(null);
        errorDialog.getContentPane().add(errorDlgButtonPanel, BorderLayout.SOUTH);
        errorDialog.pack();
        errorDialog.show();
        return exceptions.isEmpty();
    }

}
