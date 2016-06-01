/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xslt.cps.editor;

import com.fiorano.adapter.jca.editors.JMXBasedPropertyEditor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Phani
 * Date: Jun 25, 2009
 * Time: 11:38:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class XsltModelEditor extends JMXBasedPropertyEditor implements ExPropertyEditor {

    private PropertyEnv m_propertyEnv;

    private Object m_inputXSDs;

    private Object m_outputXSDs;

    /**
     * The main program for the XsltModelEditor class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        XsltModelEditor ed = new XsltModelEditor();

        Component comp = ed.getCustomEditor();

        if (comp instanceof Window) {
            comp.setVisible(true);
        } else {
            JDialog dlg = new JDialog(new JFrame(), "Custom Editor", true);

            dlg.getContentPane().add(comp);
            dlg.setLocationRelativeTo(null);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setVisible(true);
        }
    }

    /**
     * Returns as text for object
     */
    public String getAsText() {
        //return "Click ... to continue";
        return "Click ... to view/edit mappings in the Mapper";
    }

    /**
     * Sets as text for object
     */
    public void setAsText(String text)
            throws IllegalArgumentException {
    }

    /**
     * Sets input XS ds for object
     */
    public void setInputXSDs(Object inputXSDs) {
        m_inputXSDs = inputXSDs;
    }

    /**
     * Sets output XS ds for object
     */
    public void setOutputXSDs(Object outputXSDs) {
        m_outputXSDs = outputXSDs;
    }

    /**
     * Returns custom editor for object
     */
    public Component _getCustomEditor() {

        XsltPsPanel xsltPsPanel = new XsltPsPanel(this, m_propertyEnv);

        return xsltPsPanel.getMapper();

    }

    /**
     * @return
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * @param propertyEnv
     */
    public void attachEnv(PropertyEnv propertyEnv) {
        m_propertyEnv = propertyEnv;
    }

}
