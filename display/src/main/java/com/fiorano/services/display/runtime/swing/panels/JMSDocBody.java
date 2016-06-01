/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing.panels;


import com.fiorano.uif.filechooser.TifosiFileChooser;
import com.fiorano.uif.filechooser.TifosiFileFilter;
import com.fiorano.uif.images.ImageReference;
import com.fiorano.uif.ui.TifosiImage;
import com.fiorano.util.lang.ClassUtil;
import fiorano.esb.utils.RBUtil;

import javax.jms.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 11 Aug, 2010
 * Time: 6:10:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class JMSDocBody extends JPanel {

    private ImageIcon saveIcon = new ImageIcon(TifosiImage.loadImage(ImageReference.class, "save.gif"));
    private JPanel cardPanel = new JPanel();
    private JLabel messageTypePrefixLabel = new JLabel();
    private JLabel messageTypeLabel = new JLabel();
    private JButton saveMessageButton = new JButton();
    private GridLayout gridLayout = new GridLayout();
    private BorderLayout borderLayout = new BorderLayout();
    private JPanel headPanel = new JPanel();
    private JPanel jPanel = new JPanel();
    private CardLayout cardLayout = new CardLayout();
    private MapMessagePanel mapPanel;
    private StreamMessagePanel streamPanel;
    private ObjectMessagePanel objectPanel;
    private BytesMessagePanel bytePanel;
    private TextMessagePanel textPanel;
    private Message loadedMessage = null;
    private Logger logger;

    private static final String MAP_MESSAGE_TYPE;
    private static final String BYTES_MESSAGE_TYPE;
    private static final String TEXT_MESSAGE_TYPE;
    private static final String OBJECT_MESSAGE_TYPE;
    private static final String STREAM_MESSAGE_TYPE;

    static {
        MAP_MESSAGE_TYPE = getMessageType(MapMessage.class);
        BYTES_MESSAGE_TYPE = getMessageType(BytesMessage.class);
        STREAM_MESSAGE_TYPE = getMessageType(StreamMessage.class);
        OBJECT_MESSAGE_TYPE = getMessageType(ObjectMessage.class);
        TEXT_MESSAGE_TYPE = getMessageType(TextMessage.class);
    }

    private Map<String, JPanel> typePanelMap = new HashMap<String, JPanel>();


    public JMSDocBody(Logger logger) {
        this.logger = logger;
        try {
            jbInit();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CREATING_DOC_PANEL), ex);
        }
    }


    void jbInit() throws Exception {
        mapPanel = new MapMessagePanel(logger);
        textPanel = new TextMessagePanel(logger);
        objectPanel = new ObjectMessagePanel(logger);
        streamPanel = new StreamMessagePanel(logger);
        bytePanel = new BytesMessagePanel(logger);


        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        jPanel.setLayout(gridLayout);
        headPanel.setLayout(borderLayout);
        headPanel.setBorder(border);
        headPanel.add(jPanel, BorderLayout.WEST);
        headPanel.add(saveMessageButton, BorderLayout.EAST);
        jPanel.add(messageTypePrefixLabel);
        jPanel.add(messageTypeLabel);
        saveMessageButton.setIcon(saveIcon);
        saveMessageButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveButton_actionPerformed();
                    }
                });

        typePanelMap.put(TEXT_MESSAGE_TYPE, textPanel);
        typePanelMap.put(MAP_MESSAGE_TYPE, mapPanel);
        typePanelMap.put(BYTES_MESSAGE_TYPE, bytePanel);
        typePanelMap.put(STREAM_MESSAGE_TYPE, streamPanel);
        typePanelMap.put(OBJECT_MESSAGE_TYPE, objectPanel);

        cardPanel.setLayout(cardLayout);

        for (Map.Entry<String, JPanel> typePanelEntry : typePanelMap.entrySet()) {
            cardPanel.add(typePanelEntry.getKey(), typePanelEntry.getValue());
        }
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        add(headPanel, BorderLayout.NORTH);

        cardLayout.show(cardPanel, TEXT_MESSAGE_TYPE);
        messageTypePrefixLabel.setText("");
        messageTypeLabel.setText("");
        saveMessageButton.setEnabled(false);
    }


    public void loadBody(Message message) {
        try {
            String messageType = getMessageType(message.getClass());
            cardLayout.show(cardPanel, messageType);
            messageTypePrefixLabel.setText(Bundle.MESSAGE_TYPE_PREFIX_LABEL_TEXT);
            messageTypeLabel.setText(messageType);
            saveMessageButton.setEnabled(true);
            ((IPanelLoader) typePanelMap.get(messageType)).load(message);
            loadedMessage = message;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_LOADING_DOC_BODY), ex);
        }

    }

    private void saveButton_actionPerformed() {
        String messageType = getMessageType(loadedMessage.getClass());
        try {
            if (loadedMessage == null) {
                JOptionPane.showMessageDialog(this, Bundle.WARNING_NO_MESSAGE_SELECTED);
                return;
            } else {
                File file = TifosiFileChooser.showSaveFileDialog(this, null, (TifosiFileFilter) null, "Save to file...", ".",
                        "outMessage");
                if (file == null) {
                    return;
                }
                ((IPanelLoader) typePanelMap.get(messageType)).saveMessage(loadedMessage,file);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_SAVING_MESSAGE_DOC_BODY), ex);
        }
    }

    public void clear() {
        try {
            messageTypePrefixLabel.setText("");
            messageTypeLabel.setText("");
            saveMessageButton.setEnabled(false);
            cardLayout.show(cardPanel, TEXT_MESSAGE_TYPE);
            textPanel.clear();
            mapPanel.clear();
            objectPanel.clear();
            streamPanel.clear();
            bytePanel.clear();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.ERROR_CLEARING_DOC_BODY), ex);
        }
    }

    private static String getMessageType(Class<? extends Message> messageClass) {
        Class targetClass;
        if (MapMessage.class.isAssignableFrom(messageClass)) {
            targetClass = MapMessage.class;
        } else if (StreamMessage.class.isAssignableFrom(messageClass)) {
            targetClass = StreamMessage.class;
        } else if (BytesMessage.class.isAssignableFrom(messageClass)) {
            targetClass = BytesMessage.class;
        } else if (TextMessage.class.isAssignableFrom(messageClass)) {
            targetClass = TextMessage.class;
        } else if (ObjectMessage.class.isAssignableFrom(messageClass)) {
            targetClass = ObjectMessage.class;
        } else {
            return null;
        }

        return ClassUtil.getShortClassName(targetClass);
    }

}
