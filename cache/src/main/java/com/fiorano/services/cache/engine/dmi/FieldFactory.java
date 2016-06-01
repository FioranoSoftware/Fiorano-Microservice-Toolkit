/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

import com.fiorano.edbc.cache.configuration.FieldDefinition;
import com.fiorano.services.cache.engine.CacheException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.lang.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A factory class which can create a field based on the definition of the field provided and the value
 *
 * @author FSTPL
 */
public class FieldFactory {
    public static IField createField(FieldDefinition fieldDefinition, String value) throws CacheException {
        if (fieldDefinition == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_FIELD_DEFINITION));
        }

        Class clazz = fieldDefinition.getClazz();
        Object objValue;

        try {
            if (clazz.isPrimitive()) {
                clazz = (Class) ClassUtil.PRIMITIVE_WRAPPER_CLASSES.get(clazz);
            }
            Constructor constructor = clazz.getConstructor(new Class[]{String.class});
            if (clazz.getName().toLowerCase().contains("date")) {
                objValue = new SimpleDateFormat("yyyy-MM-dd").parse(value);
            } else {
                objValue = constructor.newInstance(new String[]{value});
            }
        } catch (IllegalAccessException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.CANNOT_CREATE_VALUE_OBJECT, new Object[]{fieldDefinition.getClazz()}), e);
        } catch (NoSuchMethodException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.CANNOT_CREATE_VALUE_OBJECT, new Object[]{fieldDefinition.getClazz()}), e);
        } catch (InvocationTargetException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.CANNOT_CREATE_VALUE_OBJECT, new Object[]{fieldDefinition.getClazz()}), e);
        } catch (InstantiationException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.CANNOT_CREATE_VALUE_OBJECT, new Object[]{fieldDefinition.getClazz()}), e);
        } catch (ParseException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.CANNOT_CREATE_VALUE_OBJECT, new Object[]{fieldDefinition.getClazz()}), new Exception("Got Date field value in different date format ", e));
        }

        IField field;
        if (fieldDefinition.isKey()) {
            field = new KeyField(fieldDefinition.getName(), objValue);
        } else {
            field = new DataField(fieldDefinition.getName(), objValue);
        }
        return field;
    }
}
