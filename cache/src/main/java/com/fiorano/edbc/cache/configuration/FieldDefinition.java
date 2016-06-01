/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.cache.configuration;

import com.fiorano.services.cache.engine.dmi.XML;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.util.lang.ClassUtil;
import fiorano.esb.record.ESBRecordDefinition;

import java.util.Date;

/**
 * This class contains the definition of the Field. the definition includes name, type and whether the field is a key or not.
 *
 * @author Venkat
 */

public class FieldDefinition {
    private String name;
    private Class clazz;
    private boolean isKey;
    private ESBRecordDefinition xsd = new ESBRecordDefinition();

    /**
     * returns the class name of type in short form
     */
    public static String getShortClassName(Class clazz) {
        if (clazz.isPrimitive()) {
            return clazz.getName();
        } else {
            return ClassUtil.getShortClassName(clazz).toLowerCase();
        }
    }

    /**
     * returns name of the field
     */
    public String getName() {
        return name;
    }

    /**
     * set the name of the field
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_FIELD_NAME));
        }
        this.name = name;
    }

    /**
     * returns the type of the field. returned class will one of the following classes: int, long, short, double, float, boolean, Date, String
     */
    public Class getClazz() {
        return clazz;
    }

    /**
     * sets the type of Field. the clazz should be one of int, long, short, double, float, boolean, Date, String and cannot be null
     */
    public void setClazz(Class clazz) {
        if (clazz == null || !isValidClass(clazz)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_FIELD_VALUE_CLASS, new Object[]{clazz == null ? "null" : clazz.getName()}));
        }
        this.clazz = clazz;
    }

    /**
     * determines if the Field is a KeyField or not.
     */
    public boolean isKey() {
        return isKey;
    }

    /**
     * set true to mark this field as a key field, else set false
     *
     * @param key
     */
    public void setKey(boolean key) {
        isKey = key;
    }

    public ESBRecordDefinition getXsd() {
        return xsd;
    }

    public void setXsd(ESBRecordDefinition xsd) {
        this.xsd = xsd;
    }

    /**
     * supports only boolean, long, short, float, double, int, String, Date
     *
     * @param clazz field type - java class
     * @return valid class or not - whether the class of the field key / value is supported or not
     */
    private boolean isValidClass(Class clazz) {
        boolean isPrimitiveAndNotCharByteVoid = clazz.isPrimitive() && clazz != char.class && clazz != byte.class
                && clazz != void.class;
        return isPrimitiveAndNotCharByteVoid || clazz == Date.class || clazz == String.class || clazz == XML.class;
    }

    /**
     * returns the class name of type in short form
     */
    public String getShortClassName() {
        if (clazz.isPrimitive()) {
            return clazz.getName();
        } else {
            return ClassUtil.getShortClassName(clazz).toLowerCase();
        }
    }

    public String toString() {
        return "{Name:" + name + "::Class:" + clazz.toString() + "::isKey:" + isKey + "}" + StringUtil.LINE_SEP;
    }
}

