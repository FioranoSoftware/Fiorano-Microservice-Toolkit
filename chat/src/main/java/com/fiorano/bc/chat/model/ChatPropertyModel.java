/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.chat.model;

import java.awt.Color;
import java.awt.Font;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @created April 18, 2005
 * @version 1.0
 */
public class ChatPropertyModel implements Cloneable
{
    private final static String DEFAULT_DISPLAY_NAME = "FioranoESB Demo";
    private final static String DEFAULT_EMAIL_ADDRESS = "fesb@fiorano.com";

    private final static String DEFAULT_IN_FONT_NAME = "Dialog";
    private final static String DEFAULT_IN_FONT_COLOR = Color.blue.getRGB() +
        "";
    private final static String DEFAULT_IN_FONT_SIZE = "10";
    private final static String DEFAULT_IN_FONT_STYLE = Font.PLAIN + "";

    private final static String DEFAULT_OUT_FONT_NAME = "Dialog";
    private final static String DEFAULT_OUT_FONT_COLOR = Color.black.getRGB() +
        "";
    private final static String DEFAULT_OUT_FONT_SIZE = "10";
    private final static String DEFAULT_OUT_FONT_STYLE = Font.PLAIN + "";

    private String  displayName;
    private String  emailAddress;
    private String  inFontName;
    private String  inFontColor;
    private String  outFontColor;
    private String  inFontSize;
    private String  outFontSize;
    private String  inFontStyle;
    private String  outFontStyle;
    private String  outFontName;

    /**
     */
    public ChatPropertyModel()
    {
        displayName = DEFAULT_DISPLAY_NAME;
        emailAddress = DEFAULT_EMAIL_ADDRESS;

        inFontName = DEFAULT_IN_FONT_NAME;
        inFontColor = DEFAULT_IN_FONT_COLOR;
        inFontSize = DEFAULT_IN_FONT_SIZE;
        inFontStyle = DEFAULT_IN_FONT_STYLE;

        outFontColor = DEFAULT_OUT_FONT_COLOR;
        outFontSize = DEFAULT_OUT_FONT_SIZE;
        outFontStyle = DEFAULT_OUT_FONT_STYLE;
        outFontName = DEFAULT_OUT_FONT_NAME;

    }

    /**
     * Returns display name for object
     *
     * @return
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns email address for object
     *
     * @return
     */
    public String getEmailAddress()
    {
        return emailAddress;
    }

    /**
     * Returns in font color for object
     *
     * @return
     */
    public String getInFontColor()
    {
        return inFontColor;
    }

    /**
     * Returns out font color for object
     *
     * @return
     */
    public String getOutFontColor()
    {
        return outFontColor;
    }

    /**
     * Returns in font size for object
     *
     * @return
     */
    public String getInFontSize()
    {
        return inFontSize;
    }

    /**
     * Returns out font size for object
     *
     * @return
     */
    public String getOutFontSize()
    {
        return outFontSize;
    }

    /**
     * Returns in font name for object
     *
     * @return
     */
    public String getInFontName()
    {
        return inFontName;
    }

    /**
     * Returns out font name for object
     *
     * @return
     */
    public String getOutFontName()
    {
        return outFontName;
    }

    /**
     * Returns in font style for object
     *
     * @return
     */
    public String getInFontStyle()
    {
        return inFontStyle;
    }

    /**
     * Returns out font style for object
     *
     * @return
     */
    public String getOutFontStyle()
    {
        return outFontStyle;
    }

    /**
     * Sets display name for object
     *
     * @param strDisplayName
     */
    public void setDisplayName(String strDisplayName)
    {
        displayName = strDisplayName;
    }

    /**
     * Sets email address for object
     *
     * @param strEmailAddress
     */
    public void setEmailAddress(String strEmailAddress)
    {
        emailAddress = strEmailAddress;
    }

    /**
     * Sets in font color for object
     *
     * @param strFontColor
     */
    public void setInFontColor(String strFontColor)
    {
        inFontColor = strFontColor;
    }

    /**
     * Sets out font color for object
     *
     * @param strFontColor
     */
    public void setOutFontColor(String strFontColor)
    {
        outFontColor = strFontColor;
    }

    /**
     * Sets in font size for object
     *
     * @param strFontSize
     */
    public void setInFontSize(String strFontSize)
    {
        inFontSize = strFontSize;
    }

    /**
     * Sets out font size for object
     *
     * @param strFontSize
     */
    public void setOutFontSize(String strFontSize)
    {
        outFontSize = strFontSize;

    }

    /**
     * Sets in font name for object
     *
     * @param fontname
     */
    public void setInFontName(String fontname)
    {
        inFontName = fontname;
    }

    /**
     * Sets out font name for object
     *
     * @param fontname
     */
    public void setOutFontName(String fontname)
    {
        outFontName = fontname;
    }

    /**
     * Sets in font style for object
     *
     * @param strFontStyle
     */
    public void setInFontStyle(String strFontStyle)
    {
        inFontStyle = strFontStyle;
    }

    /**
     * Sets out font style for object
     *
     * @param strFontStyle
     */
    public void setOutFontStyle(String strFontStyle)
    {
        outFontStyle = strFontStyle;
    }

    public Object clone() {
        ChatPropertyModel clone = new ChatPropertyModel();
        clone.setDisplayName(getDisplayName());
        clone.setEmailAddress(getEmailAddress());
        clone.setInFontColor(getInFontColor());
        clone.setOutFontColor(getOutFontColor());
        clone.setInFontSize(getInFontSize());
        clone.setOutFontSize(getOutFontSize());
        clone.setInFontName(getInFontName());
        clone.setOutFontName(getOutFontName());
        clone.setInFontStyle(getInFontStyle());
        clone.setOutFontStyle(getOutFontStyle());
        return clone;
    }

}
