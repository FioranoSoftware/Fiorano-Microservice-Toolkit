/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat;

import com.fiorano.bc.chat.model.ChatPM;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import fiorano.esb.utils.RBUtil;

/**
 * Used to display messages received by ChatService
 *
 * @author fiorano Team
 * @version 1.0
 * @created March 13, 2002
 */

public class ChatFrame extends JFrame {
    final static int CHARS_TO_DISPLAY = 16000;

    private static String CLEAR_OPTION = "Clear All";
    // owning application.
    ChatService m_owner;
    String m_instanceName;

    //variables used in ChatFrame
    BorderLayout borderLayout1 = new BorderLayout();
    Border borderEmptyInset4444;
    Border borderMainPanel;
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    Border borderTextEntryPanel;
    Border border1;
    Border border2;
    Border border3;
    Border border4;
    Border border5;
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    JPanel jPanel4 = new JPanel();
    JPanel jPanel5 = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JPanel jPanel7 = new JPanel();
    JPanel jPanel8 = new JPanel();
    JScrollPane scrollPanel = new JScrollPane();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel textEntryPanel = new JPanel();
    BorderLayout borderLayout6 = new BorderLayout();
    BorderLayout borderLayout7 = new BorderLayout();
    JScrollPane scrollPanel2 = new JScrollPane();
    JTextPane textDisplay = new JTextPane();
    JEditorPane textEntry = new JEditorPane();
    JPanel jPanel6 = new JPanel();
    JPanel jPanel9 = new JPanel();
    BorderLayout borderLayout8 = new BorderLayout();
    JLabel jLabel1 = new JLabel();
    BorderLayout borderLayout9 = new BorderLayout();
    JPanel jPanel10 = new JPanel();

    SimpleAttributeSet inSet = new SimpleAttributeSet();
    SimpleAttributeSet outSet = new SimpleAttributeSet();

    //sets the LookAndFeel for the frame
    static {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Throwable thr) {
        }
    }

    /**
     * Constructor for the ChatFrame object
     *
     * @param owner Owning ChatService
     */
    public ChatFrame(ChatService owner, String instName) throws Exception {
        m_instanceName = instName;

        m_owner = owner;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        }
        catch (Throwable thr) {
            throw new Exception(RBUtil.getMessage(Bundle.class, Bundle.CHAT_FRAME_INIT_ERROR,
                    new Object[]{thr.getMessage()}));
        }
    }

    /**
     * Initializes the ChatFrame
     *
     * @throws Exception If init fails
     */
    public void jbInit() throws Exception {
        border5 = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
                BorderFactory.createEmptyBorder(10, 0, 0, 0));
        border4 = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        border3 = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        border2 = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        border1 = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
                BorderFactory.createEmptyBorder(0, 0, 15, 0));
        try {
            setIconImage(Toolkit.getDefaultToolkit().createImage(ChatFrame.class.getResource("ChatIcon.gif")));
        }
        catch (Throwable thr) {
            //Ignore
        }
        try {

            borderEmptyInset4444 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
            borderMainPanel = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SystemColor.controlText, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2));
            borderTextEntryPanel = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(
                    BevelBorder.LOWERED, Color.white, Color.white, new Color(142, 142, 142), new Color(99, 99, 99)),
                                                                      BorderFactory.createEmptyBorder(4, 2, 4, 2));

            this.setSize(new Dimension(400, 320));

            ChatPM model = m_owner.m_model;

            this.setTitle(m_instanceName + " : " + model.getDisplayName());

            textEntryPanel.setBorder(borderTextEntryPanel);
            textEntryPanel.setPreferredSize(new Dimension(10, 36));
            textEntryPanel.setLayout(borderLayout3);

            textEntry.setFont(new java.awt.Font(model.getOutFontName(), Font.BOLD, 10));
            textDisplay.setEditable(false);

            try {
                StyleConstants.setForeground(inSet, new Color(Integer.parseInt(m_owner.m_model.getInFontColor())));
                StyleConstants.setFontSize(inSet, Integer.parseInt(m_owner.m_model.getInFontSize()));
                StyleConstants.setFontFamily(inSet, m_owner.m_model.getInFontName());

                int inprop = Integer.parseInt(m_owner.m_model.getInFontStyle());

                if ((inprop & Font.BOLD) != 0) {
                    StyleConstants.setBold(inSet, true);
                }
                if ((inprop & Font.ITALIC) != 0) {
                    StyleConstants.setItalic(inSet, true);
                }

                StyleConstants.setForeground(outSet, new Color(Integer.parseInt(m_owner.m_model.getOutFontColor())));
                StyleConstants.setFontSize(outSet, Integer.parseInt(m_owner.m_model.getOutFontSize()));
                StyleConstants.setFontFamily(outSet, m_owner.m_model.getOutFontName());

                int outprop = Integer.parseInt(m_owner.m_model.getOutFontStyle());

                if ((outprop & Font.BOLD) != 0) {
                    StyleConstants.setBold(outSet, true);
                }
                if ((outprop & Font.ITALIC) != 0) {
                    StyleConstants.setItalic(outSet, true);
                }
            }
            catch (NumberFormatException ex) {
                m_owner.getLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class,Bundle.INGNORING_NFE,
                                    new Object[]{ ex.getLocalizedMessage()}));
            }

            textEntry.setPreferredSize(new Dimension(400, 55));
            scrollPanel2.getViewport().add(textEntry, null);
            textEntry.addKeyListener(new Symkey());

            jPanel1.setLayout(borderLayout4);
            jPanel8.setLayout(borderLayout5);
            jPanel7.setLayout(borderLayout6);
            textEntryPanel.setLayout(borderLayout7);
            jPanel8.setBorder(BorderFactory.createEtchedBorder());
            jPanel8.setPreferredSize(new Dimension(380, 210));
            scrollPanel.setBorder(null);
            scrollPanel.setPreferredSize(new Dimension(380, 210));
            textDisplay.setBorder(null);
            textDisplay.setPreferredSize(new Dimension(376, 210));
            textDisplay.addMouseListener(
                    new java.awt.event.MouseAdapter() {
                        public void mouseReleased(MouseEvent e) {
                            textDisplay_mouseReleased(e);
                        }
                    });
            textEntryPanel.setBorder(BorderFactory.createEtchedBorder());
            textEntryPanel.setPreferredSize(new Dimension(380, 55));
            scrollPanel2.setBorder(null);
            scrollPanel2.setPreferredSize(new Dimension(380, 55));
            textEntry.setBorder(null);
            textEntry.setPreferredSize(new Dimension(376, 55));

            jPanel6.setLayout(borderLayout8);
            jPanel2.setPreferredSize(new Dimension(10, 20));
            jPanel2.setLayout(borderLayout9);
            jLabel1.setText("Enter text and press <enter> to send message");
            this.getContentPane().add(jPanel1, BorderLayout.CENTER);
            jPanel1.add(jPanel7, BorderLayout.SOUTH);
            jPanel7.add(textEntryPanel, BorderLayout.CENTER);
            textEntryPanel.add(scrollPanel2, BorderLayout.CENTER);
            jPanel7.add(jPanel6, BorderLayout.NORTH);
            jPanel6.add(jPanel9, BorderLayout.CENTER);
            scrollPanel2.getViewport().add(textEntry, null);
            jPanel1.add(jPanel8, BorderLayout.CENTER);
            jPanel8.add(scrollPanel, BorderLayout.CENTER);
            scrollPanel.getViewport().add(textDisplay, null);
            this.getContentPane().add(jPanel2, BorderLayout.SOUTH);
            jPanel2.add(jPanel10, BorderLayout.WEST);
            jPanel2.add(jLabel1, BorderLayout.CENTER);
            this.getContentPane().add(jPanel3, BorderLayout.WEST);
            this.getContentPane().add(jPanel4, BorderLayout.EAST);
            this.getContentPane().add(jPanel5, BorderLayout.NORTH);
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Overridden so that ChatService can exit when window is closed
     *
     * @param e Window event
     */
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            try {
                m_owner.onClose();
            }
            catch (Throwable thr) {
                JOptionPane.showMessageDialog(this,
                                              "HELP_LAUNCH_ERROR :: could not exit from chat", "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Displays the text in the frame
     *
     * @param text Text to be displayed
     */
    protected void showText(String text, boolean in) {
        //If the frame contains more than CHARS_TO_DISPLAY characters, then
        //delete the excess characters.
        //This is done so that OutoOfMemory does not occur on running ChatService
        //for a long time with lots of messages being dispalyed.
        Document doc = textDisplay.getDocument();

        //bug 4723
        int charsInFrame = doc.getLength();
        int charsInMessage = text.length();
        int diff = charsInFrame + charsInMessage - CHARS_TO_DISPLAY;

        if (diff > 0) {
            //delete message from the frame
            try {
                if (diff > charsInFrame) {
                    doc.remove(0, charsInFrame);
                } else {
                    doc.remove(0, diff);
                }
            }
            catch (BadLocationException ex) {
                m_owner.getLogger().log(Level.WARNING, RBUtil.getMessage(Bundle.class,Bundle.INGNORING_BLE,
                                    new Object[]{ ex.getLocalizedMessage()}));
            }
        }        

        try {
            doc.insertString(doc.getLength(), text + "\n", in ? inSet : outSet);
            textDisplay.setCaretPosition(doc.getLength());
            textDisplay.repaint();
        }
        //BadLocationException
        catch (Throwable thr) {
        }
    }

    // message entered.

    /**
     * Handles action raised on entry of text in message box.
     */
    void textEntry_actionPerformed() {
        String text = textEntry.getText().trim();

        m_owner.sendMessage(text);
        textEntry.setText("");
        textEntry.setCaretPosition(0);

        textEntry.grabFocus();
    }

    /**
     * MouseEventListener. Used to show pop up menu in case of right-click
     *
     * @param e MouseEvent
     */
    void textDisplay_mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
        }
    }


    /**
     * Class used for listening on Key strokes.
     *
     * @author Administrator
     * @version 2.0
     * @created March 13, 2002
     */
    class Symkey extends java.awt.event.KeyAdapter implements TextListener {
        /**
         * Just to implement TextListener
         *
         * @param k TextEvent
         */
        public void textValueChanged(TextEvent k) {
        }

        /**
         * Method on key pressed
         *
         * @param k KeyEvent
         */
        public void keyPressed(KeyEvent k) {
            //If control is pressed then 'enter' means new line
            //else enter is used to send message
            if (k.isControlDown()) {
                try {
                    if (k.getKeyCode() == KeyEvent.VK_ENTER) {
                        int caretPos = textEntry.getCaretPosition();

                        if (caretPos == textEntry.getText().length()) {
                            textEntry.setText(textEntry.getText() + "\n");
                        } else {
                            textEntry.getDocument().insertString(caretPos, "\n", null);
                            textEntry.setCaretPosition(caretPos + 2);
                        }
                    }
                }
                catch (Exception ex) {
                }
            } else if (k.getKeyCode() == KeyEvent.VK_ENTER && !textEntry.getText().trim().equals("")) {
                textEntry_actionPerformed();
            }

        }

        /**
         * Method when a key is released
         *
         * @param k KeyEvent
         */
        public void keyReleased(KeyEvent k) {
            //If control is pressed then enter is new line
            //else enter is 'send message'
            if (!(k.isControlDown())) {

                if (k.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (textEntry.getText().trim().length() == 0) {
                        textEntry.setText("");
                        textEntry.setCaretPosition(0);
                    }

                    textEntry.grabFocus();
                }
            }
        }
    }

    /**
     * ActionListener for right click of mouse
     *
     * @author Pushkar
     * @version 2.0
     * @created January 27, 2003
     */
    class RightClickActionListener implements ActionListener {
        /**
         * actionPerformed
         *
         * @param ev Action Event
         */
        public void actionPerformed(ActionEvent ev) {
            if (ev.getActionCommand().equals("Clear All")) {
                //clear all the messages in the frame
                textDisplay.setText("");
                textDisplay.setCaretPosition(0);
            }
        }
    }
}
