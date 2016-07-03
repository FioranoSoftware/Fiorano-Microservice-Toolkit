/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.feeder.ps.panels;

import com.fiorano.util.Util;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Oct 31, 2007
 * Time: 5:48:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Header {
    private String name;
    private String type;
    private String value;

    public Header() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Object header1) {
        if (header1 == null || !(header1 instanceof Header)) {
            return false;
        }

        String name = ((Header) header1).getName();
        return Util.equals(this.name, name);
    }

    public int hashCode() {
        return name == null ? -1 : name.hashCode();
    }
}
