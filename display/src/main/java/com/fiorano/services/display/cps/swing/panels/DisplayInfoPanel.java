/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.cps.swing.panels;

import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.fiorano.openide.wizard.WizardStep;
import fiorano.tifosi.common.TifosiException;
import fiorano.esb.utils.RBUtil;

import com.fiorano.bc.display.model.ConfigurationPM;
import com.fiorano.uif.UIFException;
import com.fiorano.uif.util.PositiveIntegerSpinner;
import com.fiorano.uif.wizard.WizardPanel;
import org.openide.WizardValidationException;

/**
 *  Title: Tifosi Complete Description: Mother of all projects Copyright:
 *  Copyright (c) 2002 Company: Fiorano Software Technologies Pvt  Ltd.
 *
 * @author
 * @created March 12, 2002
 * @version 1.0
 */
/**
 * @bundle $class.title=Display Properties
 * @bundle $class.summary=Enter display Properties
 */

public class DisplayInfoPanel extends WizardStep
{
    BorderLayout    borderLayout1 = new BorderLayout();
    JPanel          northPanel = new JPanel();
    JPanel          centerPanel = new JPanel();
    JPanel          southPanel = new JPanel();
    JLabel          headerLbl = new JLabel();
    GridLayout      gridLayout1 = new GridLayout();
    FlowLayout      flowLayout2 = new FlowLayout();
    TitledBorder    titledBorder1;
    Border          border1;
    Border          border2;
    Border          border3;
    Border          border4;
    Border          border5;
    ConfigurationPM m_model;
    JPanel          jPanel1 = new JPanel();
    PositiveIntegerSpinner m_visibleMsgCountNS = new PositiveIntegerSpinner();
    JLabel          jLabel3 = new JLabel();
    BorderLayout    borderLayout2 = new BorderLayout();
    JCheckBox       m_infinityCheck = new JCheckBox();
    WizardPanel     panel = new WizardPanel();

    /**
     *  Constructor for the ServerInfoPanel object
     *
     * @param model Description of the Parameter
     * @exception TifosiException Description of the Exception
     */
    public DisplayInfoPanel(ConfigurationPM model)
        throws TifosiException
    {
        m_model =  model;

        try
        {
            jbInit();
        }
        catch (Throwable thr)
        {
            throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.DBINFO_INIT_FAILED),
                RBUtil.getMessage(Bundle.class, Bundle.DBINFO_INIT_FAILED_DESC), thr);
        }
    }
    @Override
    protected WizardPanel createComponent() {
        return panel;  //To change body of implemented methods use File | Settings | File Templates.
    }
    /**
     * Returns title for object
     *
     * @return
     */
    public String getTitle()
    {
        return "Display Properties";
    }

    /**
     * Returns description for object
     *
     * @return
     */
    public String getDescription()
    {
        return "Configure Display Properties";
    }

   

    @Override
    public void model2Component()
        throws UIFException
    {
        int iMsg = m_model.getMaxBufferedMessages();

        if (iMsg == -1)
            m_infinityCheck.setSelected(true);
        else
        {
            m_visibleMsgCountNS.setValue(iMsg);
            m_infinityCheck.setSelected(false);
        }
    }

    /**
     *  updates the state with defaults/old values if any in case of prev/next
     *  action
     *
     * @exception UIFException If validation fails
     */
    public void component2Model()

    {
        int iMsg = 0;
        try {
            iMsg = (m_infinityCheck.isSelected()) ? -1 : m_visibleMsgCountNS.getValue();
        } catch (UIFException e) {
            e.printStackTrace();
        }

        m_model.setMaxBufferedMessages(iMsg);
    }

    /**
     * @exception Exception Description of the Exception
     */
    void jbInit()
        throws Exception
    {
        try
        {
            titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), "");
            border1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            border2 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            border3 = BorderFactory.createEmptyBorder(0, 25, 0, 25);
            border4 = BorderFactory.createEmptyBorder(0, 25, 0, 25);
            border5 = BorderFactory.createEmptyBorder(0, 25, 0, 25);

            panel.setName("Display Properties");
            panel.setLayout(borderLayout1);
            headerLbl.setBackground(Color.white);
            headerLbl.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
            headerLbl.setBorder(border2);
            headerLbl.setOpaque(true);
            headerLbl.setText("No. of  Messages Visible");
            northPanel.setLayout(gridLayout1);
            northPanel.setMinimumSize(new Dimension(41, 17));
            northPanel.setPreferredSize(new Dimension(41, 54));
            centerPanel.setLayout(borderLayout2);
            southPanel.setLayout(flowLayout2);
            flowLayout2.setHgap(10);
            centerPanel.setBorder(border2);

            // totalMessagesTextField.setText("10");
//            refreshContent();

            m_visibleMsgCountNS.setValue(10);
//      m_visibleMsgCountNS.setMaximum(50);
            m_visibleMsgCountNS.setMinimum(1);
            m_visibleMsgCountNS.setPreferredSize(new Dimension(55, 21));
            jLabel3.setFont(new java.awt.Font("Dialog", 0, 11));
            jLabel3.setText("Specify Total No. of Last Messages Visible :");
            m_infinityCheck.setText("infinite");
            m_infinityCheck.addItemListener(
                new java.awt.event.ItemListener()
                {
                    public void itemStateChanged(ItemEvent e)
                    {
                        m_infinityCheck_itemStateChanged(e);
                    }
                });
            panel.add(northPanel, BorderLayout.NORTH);
            northPanel.add(headerLbl, null);
            panel.add(centerPanel, BorderLayout.CENTER);
            panel.add(southPanel, BorderLayout.SOUTH);
            centerPanel.add(jPanel1, BorderLayout.WEST);
            jPanel1.add(jLabel3, null);
            jPanel1.add(m_visibleMsgCountNS, null);
            jPanel1.add(m_infinityCheck, null);
            //  DisplayPropertySheet.setHelpKey(this, "ch08-5");

        }
        catch (Exception e)
        {
            throw e;
        }
    }

    void m_infinityCheck_itemStateChanged(ItemEvent e)
    {
        m_visibleMsgCountNS.setEnabled(e.getStateChange() != e.SELECTED);
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
