/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat.editor.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @created June 29, 2005
 * @version 1.0
 */
public class FontPanel extends JPanel
{
    JComboBox       nameCombo, sizeCombo;
    JButton         colorButton;
    JToggleButton   bold, italic;
    JLabel          previewLabel = new JLabel("Preview Font", JLabel.CENTER);
    Font            font;
    private BorderLayout borderLayout1 = new BorderLayout();

    /**
     */
    public FontPanel()
    {
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String fonts[] =
        //{"Dialog", "SansSerif", "Serif", "Monospaced", "DialogInput"};
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        nameCombo = new JComboBox(new DefaultComboBoxModel(fonts));
        sizeCombo = new JComboBox(new DefaultComboBoxModel(new String[]{"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"}));
        colorButton = new JButton(new ColorIcon());
        colorButton.setMargin(new Insets(0, 0, 0, 0));
        bold = new JToggleButton(new ImageIcon(getClass().getResource("bold.gif")));
        bold.setMargin(new Insets(0, 0, 0, 0));
        italic = new JToggleButton(new ImageIcon(getClass().getResource("italic.gif")));
        italic.setMargin(new Insets(0, 0, 0, 0));

        FontNameRenderer fnr = new FontNameRenderer();

        fnr.setText("DialogInput");
        fnr.setFont(new Font("DialogInput", Font.PLAIN, 16));

        Dimension dim = new Dimension(0, 0);

        for (int i = 0; i < fonts.length; i++)
        {
            //fnr.setFont(new Font( (String) fonts[i], Font.PLAIN, 16));
            fnr.setText(fonts[i]);

            Dimension newDim = nameCombo.getPreferredSize();

            dim.width = Math.max(dim.width, newDim.width);
            dim.height = Math.max(dim.height, newDim.height);
        }
        nameCombo.setPreferredSize(dim);
        nameCombo.setRenderer(fnr);

        FontSizeRenderer fr = new FontSizeRenderer();

        dim = sizeCombo.getPreferredSize();
        dim.width = fr.getPreferredSize().width;
        sizeCombo.setPreferredSize(dim);
        sizeCombo.setRenderer(fr);

        p.add(nameCombo);
        p.add(sizeCombo);
        p.add(colorButton);
        p.add(bold);
        p.add(italic);
        add("North", p);

        p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder(new EtchedBorder(), "Preview"));
        previewLabel.setBackground(Color.white);
        previewLabel.setForeground(Color.black);
        previewLabel.setOpaque(true);
        previewLabel.setBorder(new LineBorder(Color.black));
        previewLabel.setPreferredSize(new Dimension(120, 40));
        p.add(previewLabel, BorderLayout.CENTER);
        add("Center", p);

        nameCombo.addItemListener(
            new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    updatePreview();
                }
            });
        sizeCombo.addItemListener(
            new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    updatePreview();
                }
            });
        bold.addChangeListener(
            new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    updatePreview();
                }
            });
        italic.addChangeListener(
            new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    updatePreview();
                }
            });
        colorButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    Color color = JColorChooser.showDialog(colorButton, "Select Color", colorButton.getForeground());

                    if (color != null)
                        colorButton.setForeground(color);
                    updatePreview();
                }
            });
    }

    /**
     */
    public void updatePreview()
    {
        int style = Font.PLAIN;

        if (bold.isSelected())
            style |= Font.BOLD;
        if (italic.isSelected())
            style |= Font.ITALIC;
        previewLabel.setForeground(colorButton.getForeground());
        previewLabel.setFont(new Font((String) nameCombo.getSelectedItem(), style, Integer.parseInt((String) sizeCombo.getSelectedItem())));
    }
}

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @created June 29, 2005
 * @version 1.0
 */
class FontNameRenderer extends DefaultListCellRenderer
{

    /**
     * Returns list cell renderer component for object
     *
     * @param list
     * @param obj
     * @param row
     * @param sel
     * @param hasFocus
     * @return
     */
    public Component getListCellRendererComponent(JList list, Object obj, int row, boolean sel, boolean hasFocus)
    {
        super.getListCellRendererComponent(list, obj, row, sel, hasFocus);
        //setFont(new Font((String)obj, Font.PLAIN, 16));
        setText(obj.toString());
        return this;
    }
}

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @created June 29, 2005
 * @version 1.0
 */
class FontSizeRenderer extends DefaultListCellRenderer
{

    /**
     */
    public FontSizeRenderer()
    {
        setFont(new Font("Verdana", Font.PLAIN, 72));
        setText("72");
    }

    /**
     * Returns list cell renderer component for object
     *
     * @param list
     * @param obj
     * @param row
     * @param sel
     * @param hasFocus
     * @return
     */
    public Component getListCellRendererComponent(JList list,
        Object obj, int row, boolean sel, boolean hasFocus)
    {
        super.getListCellRendererComponent(list, obj, row, sel, hasFocus);
        setFont(new Font("Verdana", Font.PLAIN, Integer.parseInt(obj.toString())));
        setText(obj.toString());
        return this;
    }
}

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @created June 29, 2005
 * @version 1.0
 */
class ColorIcon implements Icon
{

    /**
     * Returns  icon width for object
     *
     * @return
     */
    public int getIconWidth()
    {
        return 20;
    }

    /**
     * Returns icon height for object
     *
     * @return
     */
    public int getIconHeight()
    {
        return 20;
    }

    /**
     * @param comp
     * @param g
     * @param x
     * @param y
     */
    public void paintIcon(Component comp, Graphics g, int x, int y)
    {
        g.setColor(comp.getForeground());
        g.fillRect(x, y, 19, 19);
    }
}
