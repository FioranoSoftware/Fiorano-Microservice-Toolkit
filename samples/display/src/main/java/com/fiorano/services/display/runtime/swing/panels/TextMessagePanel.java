/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing.panels;

import com.fiorano.services.display.runtime.swing.HexaDecimalLineNumberBorder;
import com.fiorano.uif.images.ImageReference;
import com.fiorano.uif.ui.*;
import com.fiorano.uif.ui.BytesEditorPane;
import com.fiorano.uif.util.TextEditor;
import com.fiorano.uif.xml.util.XMLTreeTable;
import fiorano.esb.util.MessageUtil;
import fiorano.esb.utils.RBUtil;

import javax.jms.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Panel showing the JMS message
 *
 * @author FSIPL
 * @version 1.0
 * @created August 10, 2010
 */

public class   TextMessagePanel extends JPanel implements IPanelLoader<TextMessage> {

    private JTabbedPane viewTab = new JTabbedPane(JTabbedPane.BOTTOM);
    private TextEditor textEditor = new TextEditor(TextEditor.XML);
    private JScrollPane jScrollPane = new JScrollPane();
    private BytesEditorPane bytesEditor = new BytesEditorPane(jScrollPane);
    private JScrollPane htmlViewer = new JScrollPane();
    private JTextPane htmlPane = new JTextPane();
    private XMLTreeTable tree = new XMLTreeTable();
    private JPanel textPanel = new JPanel();
    private GridLayout gridLayoutText = new GridLayout(2, 1);
    private Logger logger;
    private static final int TEXT = 0;
    private static final int XML_TREE = 1;
    private static final int HTML = 2;
    private static final int BYTES = 3;

    final HexaDecimalLineNumberBorder hexaLineBorder = new HexaDecimalLineNumberBorder();

    public TextMessagePanel(Logger logger) {
        this.logger = logger;
        try {
            JbInit();
            textEditor.setEditable(false);
            ImageIcon textIcon = new ImageIcon(TifosiImage.loadImage(ImageReference.class, "textView.gif"));
            viewTab.setIconAt(0, textIcon);
            ImageIcon treeIcon = new ImageIcon(TifosiImage.loadImage(ImageReference.class, "treeView.gif"));
            viewTab.setIconAt(1, treeIcon);
            htmlPane.setEditable(false);
            htmlPane.setContentType("text/html");
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_TEXT_MESSAGE_PANEL), ex);
        }

    }

    private void JbInit() throws Exception {


        viewTab.add(textEditor, "");
        viewTab.add(tree, "");
        viewTab.add(htmlViewer, "HTML");
        viewTab.add(jScrollPane, "BYTES");

        JLabel lbl = new JLabel();
        lbl.setFont(new java.awt.Font("Monospaced", 0, 12));
        lbl.setForeground(Color.blue);
        lbl.setText(" 0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f");
        lbl.setBorder(
                new Border() {
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        g.setColor(Color.gray);
                        x += hexaLineBorder.getBorderInsets(bytesEditor).left - 5;
                        g.drawLine(x, y + height - 1, x + width, y + height - 1);
                    }

                    public Insets getBorderInsets(Component c) {
                        return new Insets(0, hexaLineBorder.getBorderInsets(bytesEditor).left, 0, 0);
                    }

                    public boolean isBorderOpaque() {
                        return true;
                    }

                });
        bytesEditor.setBackground(new Color(204, 204, 214));
        bytesEditor.setFont(new java.awt.Font("Monospaced", 0, 12));
        jScrollPane.getViewport().setView(bytesEditor);
        jScrollPane.setColumnHeaderView(lbl);
        bytesEditor.setBorder(hexaLineBorder);

        htmlViewer.getViewport().add(htmlPane, null);
        textPanel.setLayout(gridLayoutText);
        textPanel.add(viewTab);
        setLayout(new BorderLayout());
        add(viewTab, BorderLayout.CENTER);
        viewTab.addChangeListener(
                new javax.swing.event.ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        viewTab_stateChanged();
                    }
                });
    }

    public void clear() throws Exception {
        textEditor.setText("");
        bytesEditor.showBytes(null);
        viewTab.setSelectedIndex(TEXT);
    }


    private void viewTab_stateChanged() {
        lazyLoadViews();
    }
    //ToDo chnage these as local variables
    private void reloadHTMLTab() {
        int i,k,l;String text,ltext;StringBuffer textBuf;  
        text = textEditor.getText();

        if (text.trim().length() == 0) {
            text = "<html></html>";
        } else {
            try {
                // remove all meta tag
                ltext = text.toLowerCase();
                textBuf = new StringBuffer();

                 i = ltext.indexOf("<html");
                 k=-1;
                 l = ltext.indexOf("</html>", i);

                if (l == -1) {
                    l = ltext.length();
                }
                for (int j=i; j < l;) {
                    j = ltext.indexOf("<meta ", i);
                    if (j == -1) {
                        break;
                    }
                    k = ltext.indexOf(">", j);
                    if (k == -1) {
                        break;
                    }
                    textBuf.append(text.substring(i, j));
                    i = k + 1;
                }
                textBuf.append(text.substring(k+1));

                if (!textBuf.toString().toLowerCase().endsWith("</html>")) {
                    textBuf.append("</html>");
                }
                text = textBuf.toString();
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CHANGING_VIEW_TEXT_MESSAGE_PANEL), ex);
            }
        }
        htmlPane.setText(text);
        htmlPane.setCaretPosition(0);
    }

    private void reloadTreeView() {
        try {
            tree.setXML(textEditor.getText());
        }
        catch (Exception ignore) {
        }
    }

    public void load(TextMessage textMessage) throws Exception {

        String text = textMessage.getText();
        byte[] bytes = MessageUtil.getBytesData(textMessage);
        textEditor.setText(text);
        textEditor.setCaretPosition(0);
        bytesEditor.showBytes(bytes);
        lazyLoadViews();
        boolean hasText = text != null && text.length() > 0;
        boolean hasBytes = bytes != null && bytes.length > 0;

        if (!hasText && hasBytes) {
            viewTab.setSelectedIndex(BYTES);
        }
    }

    private void lazyLoadViews() {
        if (viewTab.getSelectedIndex() == XML_TREE) {
            reloadTreeView();
        } else if (viewTab.getSelectedIndex() == HTML) {
            reloadHTMLTab();
        }
    }

    public void saveMessage(TextMessage textMessage, File file) throws IOException, JMSException {

        String text = textMessage.getText();
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter;

        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);

            if (viewTab.getSelectedIndex() == BYTES) {
                try {
                    bufferedWriter.write(bytesEditor.getTextFormOfBytes());
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_SAVING_TEXT_MESSAGE), ex);
                }
            }
            else {
                if (text != null) {
                    bufferedWriter.write(text);
                }else{
                    bufferedWriter.write("");
                }
            }

        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }

}

