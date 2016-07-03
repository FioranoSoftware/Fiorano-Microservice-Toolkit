/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat.editor;

import java.awt.*;

import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.net.URL;
import javax.swing.*;
import javax.help.HelpSet;
import javax.help.DefaultHelpBroker;
import javax.help.HelpSetException;

import org.openide.*;

import org.openide.WizardDescriptor;
import org.openide.windows.WindowManager;
import com.fiorano.bc.chat.editor.panels.IdentityStep;
import com.fiorano.bc.chat.editor.panels.SettingsStep;
import com.fiorano.bc.chat.model.ChatPM;
import com.fiorano.bc.chat.model.ChatPropertyModel;
import com.fiorano.openide.util.ImageUtil;
import com.fiorano.openide.wizard.StaticWizardIterator;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.openide.wizard.WizardUtil;
import com.fiorano.uif.images.ImageReference;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @created June 21, 2005
 * @version 1.0
 */
public class ChatModelEditor extends PropertyEditorSupport
{
    private ChatPM  model;

    /**
     */
    public ChatModelEditor()
    {
        model = new ChatPM();
    }
    WizardDescriptor wizrdDescriptor;
    private Component createComponent(ChatPM model, String title)
    {
        Image titleImage = null;

        StaticWizardIterator wizardItr = new StaticWizardIterator(createWizardSteps(model));

        wizrdDescriptor = WizardUtil.creatorWizardDescriptor(model, wizardItr, title, true);
        WizardUtil.setImage(wizrdDescriptor, ImageUtil.getImage(ImageReference.class, "cps_wizard"));
        WizardUtil.setContentBackground(wizrdDescriptor, new Color(196, 221, 249));

        JButton helpBtn = new JButton("Help");
        initHelp(helpBtn);
        wizrdDescriptor.setAdditionalOptions(new Object[]{helpBtn});

        final Dialog dlg = DialogDisplayer.getDefault().createDialog(wizrdDescriptor);
        try
        {
            titleImage = ImageUtil.getImage(com.fiorano.bc.chat.editor.ChatModelEditor.class, "chat");
        }
        catch (Exception e)
        {
            // its ok if the icon is not set
        }


        final JFrame frame = new JFrame(title);

        frame.setLocation(-1000, -1000);
        frame.addWindowFocusListener(
            new WindowFocusListener()
            {
                public void windowGainedFocus(WindowEvent e)
                {
                    dlg.toFront();
                }

                public void windowLostFocus(WindowEvent e)
                {
                }
            });
        dlg.addWindowListener(
            new WindowListener()
            {
                public void windowActivated(WindowEvent e)
                {
                }

                public void windowClosed(WindowEvent e)
                {
                    frame.dispose();
                }

                public void windowClosing(WindowEvent e)
                {
                    frame.dispose();
                }

                public void windowDeactivated(WindowEvent e)
                {
                }

                public void windowDeiconified(WindowEvent e)
                {
                }

                public void windowIconified(WindowEvent e)
                {
                }

                public void windowOpened(WindowEvent e)
                {
                }
            });

        // Fix for Bug# 5617 - Show the frame only in case of windows.
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
            frame.setVisible(true);

        frame.setIconImage(titleImage);

        try
        {
            ((Frame) dlg.getOwner()).setIconImage(titleImage);
        }
        catch (Throwable e)
        {
            //its ok if the icon is not set
        }
//
//		dlg.setModal(true);
//		dlg.show();

        return dlg;
    }

    private void initHelp(JButton helpBtn) {
        boolean isHelpAvailable = false;
        HelpSet helpSet = null;
        try {
            URL helpUrl = getClass().getClassLoader().getResource(getHelpSetName());
            if(helpUrl != null) {
                helpSet = new HelpSet(getClass().getClassLoader(), helpUrl);
                DefaultHelpBroker helpBroker = new DefaultHelpBroker();
                helpBroker.setHelpSet(helpSet);
                if(helpSet.getHomeID() != null){
                    helpBroker.enableHelpOnButton(helpBtn, helpSet.getHomeID().getIDString(), helpSet);
                    isHelpAvailable = true;
                }
            }
        } catch (HelpSetException e) {
        }
        if(!isHelpAvailable)
            helpBtn.setAction(
                new AbstractAction("Help"){
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "No help available");
                    }
                }
            );
    }

    private String getHelpSetName() {
        return "com/fiorano/bc/chat/model/ChatPM.hs";
    }


    /**
     * Returns wizard steps for object
     *
     * @param model
     * @return
     */
    private static WizardStep[] createWizardSteps(ChatPM model)
    {
        ArrayList wizardSteps = new ArrayList();

        wizardSteps.add(new IdentityStep(model));
        wizardSteps.add(new SettingsStep(model));

        return (WizardStep[]) wizardSteps.toArray(new WizardStep[0]);
    }

    // get the current value of the property
    /**
     * Returns value for object
     *
     * @return
     */
    public Object getValue()
    {
        // return the object version of the primitive type
        return wizrdDescriptor != null && wizrdDescriptor.getValue() == WizardDescriptor.FINISH_OPTION
                ? WizardUtil.getSettings(wizrdDescriptor)
                : model;
    }

    // return the custom editor
    /**
     * Returns custom editor for object
     *
     * @return
     */
    public Component getCustomEditor()
    {
        // create an instance of the custom editor
		ChatPM cloneModel = null;
		if (model == null)
			cloneModel = new ChatPM();
		else
			cloneModel = (ChatPM) model.clone();
        return createComponent(cloneModel, "Chat Service");
        //new FeederPanel(this);
    }

    // set the object being edited
    /**
     * Sets value for object
     *
     * @param obj
     */
    public void setValue(Object obj)
    {
        // get the primitive data value from the object version
        if(obj instanceof ChatPropertyModel)
        {
            ChatPropertyModel config=(ChatPropertyModel) obj;
            model = new ChatPM();
			model.setDisplayName(config.getDisplayName());
			model.setEmailAddress(config.getEmailAddress());
			model.setInFontColor(config.getInFontColor());
			model.setOutFontColor(config.getOutFontColor());
			model.setInFontSize(config.getInFontSize());
			model.setOutFontSize(config.getOutFontSize());
			model.setInFontName(config.getInFontName());
			model.setOutFontName(config.getOutFontName());
			model.setInFontStyle(config.getInFontStyle());
			model.setOutFontStyle(config.getOutFontStyle());
        }
        else
           model = (ChatPM) obj;
    }

    // we support a custom editor
    /**
     * @return
     */
    public boolean supportsCustomEditor()
    {
        return true;
    }

}
