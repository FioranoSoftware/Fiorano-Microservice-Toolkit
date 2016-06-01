/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.cps.swing.panels;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.fiorano.openide.wizard.WizardStep;
import fiorano.tifosi.common.TifosiException;
import fiorano.esb.utils.RBUtil;

import com.fiorano.bc.display.model.ConfigurationPM;
import com.fiorano.uif.wizard.WizardPanel;
import org.openide.WizardValidationException;

/**
 *  Description of the Class
 *
 * @author Administrator
 * @created March 12, 2002
 * @version 2.0
 */

/**
 * @bundle $class.title=Finish Properties
 * @bundle $class.summary=Finish Properties
 */
public class FinishWizardPanel extends WizardStep {
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel northPanel = new JPanel();
    JPanel centerPanel = new JPanel();
    JPanel southPanel = new JPanel();
    JLabel headerLbl = new JLabel();
    GridLayout gridLayout1 = new GridLayout();
    GridLayout gridLayout2 = new GridLayout();
    JPanel centerPanelTopPanel = new JPanel();
    GridLayout gridLayout3 = new GridLayout();
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    FlowLayout flowLayout2 = new FlowLayout();
    GridLayout gridLayout4 = new GridLayout();
    TitledBorder titledBorder1;
    GridLayout gridLayout7 = new GridLayout();
    Border border1;
    Border border2;
    Border border3;
    Border border4;
    Border border5;
    JLabel jLabel4 = new JLabel();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    ConfigurationPM m_model;
    WizardPanel panel = new WizardPanel();

    /**
     * Constructor for the FinishWizardPanel object
     *
     * @param model Description of the Parameter
     * @throws TifosiException Description of the Exception
     */
    public FinishWizardPanel(ConfigurationPM model)
            throws TifosiException {
        m_model = model;
        try {
            jbInit();
        }
        catch (Throwable thr) {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.FINISH_FAILED),
                    RBUtil.getMessage(Bundle.class, Bundle.FINISH_FAILED_DESC), thr);
        }
    }


    /**
     * Returns description for object
     *
     * @return
     */
    public String getDescription() {
        return "Finish Properties";
    }


    /**
     * Description of the Method
     *
     * @throws Exception Description of the Exception
     */
    void jbInit()
            throws Exception {
        try {
            titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), "");
            border1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            border2 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            border3 = BorderFactory.createEmptyBorder(0, 25, 0, 25);
            border4 = BorderFactory.createEmptyBorder(0, 25, 0, 25);
            border5 = BorderFactory.createEmptyBorder(0, 25, 0, 25);

            panel.setLayout(borderLayout1);
            headerLbl.setBackground(Color.white);
            headerLbl.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
            headerLbl.setBorder(border2);
            headerLbl.setOpaque(true);
            headerLbl.setText("Congratulations");
            northPanel.setLayout(gridLayout1);
            northPanel.setMinimumSize(new Dimension(41, 17));
            northPanel.setPreferredSize(new Dimension(41, 54));
            centerPanel.setLayout(gridLayout2);
            gridLayout2.setRows(3);
            centerPanelTopPanel.setLayout(gridLayout3);
            jPanel1.setLayout(gridLayout4);
            southPanel.setLayout(flowLayout2);
            flowLayout2.setHgap(10);
            centerPanel.setBorder(border2);
            gridLayout3.setRows(5);
            jPanel2.setLayout(gridLayout7);
            gridLayout7.setRows(3);
            gridLayout7.setHgap(8);
            gridLayout4.setRows(3);
            gridLayout4.setHgap(8);
            jPanel1.setBorder(border3);
            centerPanelTopPanel.setBorder(border4);
            jPanel2.setBorder(border5);
            jLabel4.setFont(new java.awt.Font("Dialog", 0, 11));
            jLabel4.setText("You have successfully entered all the information required.");
            jLabel2.setFont(new java.awt.Font("Dialog", 0, 11));
            jLabel2.setText("To save these settings, click Finish.");
            panel.add(northPanel, BorderLayout.NORTH);
            panel.add(centerPanel, BorderLayout.CENTER);
            panel.add(southPanel, BorderLayout.SOUTH);
            northPanel.add(headerLbl, null);
            centerPanel.add(centerPanelTopPanel, null);
            centerPanelTopPanel.add(jLabel4, null);
            centerPanelTopPanel.add(jLabel1, null);
            centerPanelTopPanel.add(jLabel2, null);
            centerPanel.add(jPanel1, null);
            centerPanel.add(jPanel2, null);
        }
        catch (Exception e) {
            throw e;
        }
    }

    @Override
    protected WizardPanel createComponent() {
        return panel;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void fastValidate() throws WizardValidationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void lazyValidate() throws WizardValidationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
