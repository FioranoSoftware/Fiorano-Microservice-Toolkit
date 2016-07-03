/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.feeder.model;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created April 18, 2005
 */
public class FeederPropertyModel {
    private String strInputText = "Input Text";

    /**
     * @param string
     */
    public FeederPropertyModel(String string) {
        strInputText = string;
    }

    /**
     */
    public FeederPropertyModel() {
    }

    /**
     * Gets the input text attribute of the FeederPropertyModel object
     *
     * @return The dBPath value
     */
    public String getInputText() {
        return strInputText;
    }

    /**
     * Sets the text attribute of the FeederPropertyModel object
     *
     * @param strInputText The new inputText value
     */
    public void setInputText(String strInputText) {
       this.strInputText = strInputText;
    }
}
