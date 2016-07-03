/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.util.Util;

/**
 * An abstract implementation for IField. This overrides equals and hashCode methods so as to treat two
 * field objects as same if the name and value are equal
 *
 * @author Venkat
 */
public abstract class Field implements IField {
    protected String name;
    protected Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_FIELD_NAME));
        }
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * This is overriden so that object containing same data may not be duplicated in Collections such as Set
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(this.getClass().isInstance(obj))) {
            return false;
        }
        IField field = (IField) obj;
        return Util.equals(name, field.getName()) && Util.equals(value, field.getValue());
    }

    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (name == null ? 0 : name.hashCode());
        hashCode = 31 * hashCode + (value == null ? 0 : value.hashCode());

        return hashCode;
    }

    public String toString() {
        return "{" + name + "::" + value + "}" + StringUtil.LINE_SEP;
    }
}
