/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.cps.swing.panels;

import com.fiorano.bc.feeder.model.FeederPM;
import com.fiorano.bc.feeder.ps.panels.Attachment;
import com.fiorano.bc.feeder.ps.panels.Header;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.uif.wizard.WizardPanel;
import fiorano.esb.utils.RBUtil;
import org.openide.WizardValidationException;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Oct 26, 2007
 * Time: 2:03:43 PM
 */

/**
 * @bundle $class.title=Headers & Attachments
 * @bundle $class.summary=Enter the details of Headers and Attachments
 */

public class HeaderPanel extends WizardStep {
    private GridBagLayout gridBagLayout = new GridBagLayout();
    private HeaderAndAttachments headerAndAttachments;
    private HeaderAndAttachments.HeaderTableModel hmodel;
    private HeaderAndAttachments.AttachmentTableModel amodel;
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private WizardPanel panel = new WizardPanel();
    private FeederPM configuration;


    public HeaderPanel(FeederPM configuration) {
        this.configuration = configuration;
        panel.setLayout(gridBagLayout);
        headerAndAttachments = new HeaderAndAttachments(panel);
        jTabbedPane1.add(headerAndAttachments.getHeaderPanel(), "Headers");
        jTabbedPane1.add(headerAndAttachments.getAttachPanel(), "Attachments");
        panel.add(jTabbedPane1, new GridBagConstraints(0, 0, 0, 0, 0.5, 0.5, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        hmodel = headerAndAttachments.getHmodel();
        amodel = headerAndAttachments.getAmodel();
    }

    @Override
    protected WizardPanel createComponent() {
        return panel;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void lazyValidate() throws WizardValidationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }




    /**
     * Gets the helpID attribute of the ConfirmationPanel object
     *
     * @return The helpID value
     */
    public String getHelpID() {
        return "com.FeederPM";
    }

 
    public void component2Model()  {

        //get all values from table into a hashtable
        //serialize the the hastable
        //fetch the model using getModel()
        //put the the serialized object with somee name (say headers)

        Map headerTable = new LinkedHashMap();
        Map attachTable = new LinkedHashMap();

        if (hmodel.getRowCount() > 0) {

            for (int i = 0; i < hmodel.getRowCount(); i++) {
                Header header = new Header();
                header.setName((String) hmodel.getValueAt(i, 0));
                header.setType((String) hmodel.getValueAt(i, 1));
                header.setValue((String) hmodel.getValueAt(i, 2));
                headerTable.put(header.getName(), header);
            }
        }
        configuration.setHeader(headerTable);

        // attachment properties
        if (amodel.getRowCount() > 0) {
            for (int i = 0; i < amodel.getRowCount(); i++) {
                Attachment attachment = new Attachment();
                attachment.setName((String) amodel.getValueAt(i, 0));
                attachment.saveValue((byte[]) amodel.getValueAt(i, 1));
                attachTable.put(attachment.getName(), attachment);
            }
        }

        configuration.setAttachment(attachTable);

    }


    public void model2Component() {
        hmodel.removeAllRows();
        Map headerTable = configuration.getHeader();
        if (headerTable != null) {
            Set entries = headerTable.entrySet();
            for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Header header = (Header) entry.getValue();
                if (header != null) {
                    hmodel.addRow(new Object[]{header.getName(), header.getType(), header.getValue()});
                }
            }
        }
        amodel.removeAllRows();
        Map attachTable = configuration.getAttachment();
        if (attachTable != null) {
            Set entries = attachTable.entrySet();
            for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Attachment attachment = (Attachment) entry.getValue();
                if (attachment != null) {
                    amodel.addRow(new Object[]{attachment.getName(), attachment.fetchValue()});
                }
            }
        }
        headerAndAttachments.setButtonsVisibility();
    }


    public void fastValidate() throws WizardValidationException {
        String str = "";
        for (int i = 0; i < hmodel.getRowCount(); i++) {

            String name = (String) hmodel.getValueAt(i, FeederConstants.NAME_COLUMN);
            String type = (String) hmodel.getValueAt(i, FeederConstants.TYPE_COLUMN);
            String value = (String) hmodel.getValueAt(i, FeederConstants.VALUE_COLUMN);
            try {
                if (FeederConstants.INT_PROPERTY_TYPE.equalsIgnoreCase(type)) {
                    Integer.parseInt(value);
                } else if (FeederConstants.FLOAT_PROPERTY_TYPE.equalsIgnoreCase(type)) {
                    Float.parseFloat(value);
                } else if (FeederConstants.DOUBLE_PROPERTY_TYPE.equalsIgnoreCase(type)) {
                    Double.parseDouble(value);
                } else if (FeederConstants.LONG_PROPERTY_TYPE.equalsIgnoreCase(type)) {
                    Long.parseLong(value);
                } // No validation required for String and Object. Empty string is also valid in this case.
            } catch (Exception ex) {
                String exMessage = RBUtil.getMessage(com.fiorano.services.feeder.runtime.swing.Bundle.class, com.fiorano.services.feeder.runtime.swing.Bundle.INVALID_VALUE,
                        new Object[]{name,value,type});
                str = str + exMessage + "\n";
            }
        }
        if(!"".equals(str)) {
            try {
                throw new Exception(str);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
