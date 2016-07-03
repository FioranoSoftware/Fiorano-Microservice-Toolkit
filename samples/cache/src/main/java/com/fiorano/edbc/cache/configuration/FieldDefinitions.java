/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.cache.configuration;

import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.services.common.Exceptions;
import com.fiorano.services.common.collections.ImmutableIterator;
import com.fiorano.services.common.util.RBUtil;

import java.util.*;

/**
 * FieldDefinitions is a {@link Collection} of {@link FieldDefinitions}. Fields can be added or removed either using the Collections add/ remove emthods or using
 * addFieldDefinition and getFieldDefinition methods. However, it cannot be modified using iterator returned by {@link Collection#iterator()} method.
 *
 * @author Venkat
 */

public class FieldDefinitions implements Collection, Cloneable {

    private Hashtable<String, FieldDefinition> fieldDefinitiontable;

    public FieldDefinitions() {
        setToDefaults();
    }

    public void setToDefaults() {

        fieldDefinitiontable = new Hashtable<>();
    }

    /**
     * returns hashtable containing <name, fieldDefinition> pairs.
     *
     * @return
     */
    public Hashtable<String, FieldDefinition> getFieldDefinitiontable() {
        return fieldDefinitiontable;
    }

    /**
     * set the field definition table. A hashtable containing <name, fieldDefinition>
     *
     * @param fieldDefinitiontable
     */
    public void setFieldDefinitiontable(Hashtable<String, FieldDefinition> fieldDefinitiontable) {
        if (fieldDefinitiontable == this.fieldDefinitiontable) {
            return;
        }
        this.fieldDefinitiontable = fieldDefinitiontable == null ? new Hashtable<String, FieldDefinition>() : fieldDefinitiontable;
    }

    /**
     * returns definition for the field by the given name
     *
     * @param fieldName name of thee field whose definition should be returned
     */
    public FieldDefinition getFieldDefinition(String fieldName) {
        return fieldDefinitiontable.get(fieldName);
    }

    /**
     * adds a FieldDefinition into table containing Field definitions. This is same as add(Object)
     *
     * @param fieldDefinition definition of field which has to be added
     */
    public void addFieldDefinition(FieldDefinition fieldDefinition) {
        add(fieldDefinition);
    }

    /**
     * removes the field definition whose name is same as name of fieldDefinition
     */
    public void removeFieldDefinition(FieldDefinition fieldDefinition) {
        remove(fieldDefinition);
    }

    /**
     * removes the field definition with given name from the field definitions table
     */
    public FieldDefinition removeField(String fieldName) {
        return fieldDefinitiontable.remove(fieldName);
    }

    /**
     * returns a {@link Collection} of field definitions for data fields
     */
    public Collection getDataFieldDefinitions() {
        return getFieldDefinitions(false);
    }

    /**
     * returns a {@link Collection} of field definitions for key fields
     */
    public Collection getKeyFieldDefinitions() {
        return getFieldDefinitions(true);
    }

    private Collection getFieldDefinitions(boolean key) {
        Iterator allFieldsIterator = fieldDefinitiontable.values().iterator();
        FieldDefinition fieldDefinition;
        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        while (allFieldsIterator.hasNext()) {
            fieldDefinition = (FieldDefinition) allFieldsIterator.next();
            boolean add;
            add = (key && fieldDefinition.isKey()) || (!key && !fieldDefinition.isKey());
            if (add) {
                fieldDefinitions.add(fieldDefinition);
            }
        }
        return fieldDefinitions;
    }

    public int size() {
        return fieldDefinitiontable.size();
    }

    public void clear() {
        fieldDefinitiontable.clear();
    }

    public boolean isEmpty() {
        return fieldDefinitiontable.isEmpty();
    }

    public Object[] toArray() {
        return fieldDefinitiontable.values().toArray();
    }

    public boolean add(Object fieldDefinition) {
        if (!(fieldDefinition instanceof FieldDefinition)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_FIELD_DEFINITION, new Object[]{fieldDefinition.getClass()}));
        }
        Object oldValue = fieldDefinitiontable.put(((FieldDefinition) fieldDefinition).getName(), (FieldDefinition) fieldDefinition);
        return oldValue == null || !oldValue.equals(fieldDefinition);
    }

    public boolean contains(Object fieldDefinition) {
        if (!(fieldDefinition instanceof FieldDefinition)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_FIELD_DEFINITION, new Object[]{fieldDefinition.getClass()}));
        }
        return fieldDefinitiontable.containsKey(((FieldDefinition) fieldDefinition).getName());
    }

    public boolean remove(Object fieldDefinition) {
        if (!(fieldDefinition instanceof FieldDefinition)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_FIELD_DEFINITION, new Object[]{fieldDefinition.getClass()}));
        }
        return fieldDefinitiontable.remove(((FieldDefinition) fieldDefinition).getName()) != null;
    }

    public boolean addAll(Collection collection) {
        boolean modified = false;
        if (collection == null) {
            return false;
        }
        for (Object obj : collection) {
            if (add(obj)) {
                modified = true;
            }
        }
        return modified;
    }

    public boolean containsAll(Collection collection) {
        if (collection == null) {
            return true;
        }
        for (Object obj : collection) {
            if (!contains(obj)) {
                return false;
            }
        }
        return true;
    }

    public boolean removeAll(Collection collection) {
        boolean modified = false;
        if (collection == null) {
            return false;
        }
        Iterator e = iterator();
        while (e.hasNext()) {
            if (collection.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    public boolean retainAll(Collection collection) {
        boolean modified = false;
        if (collection == null) {
            return false;
        }
        Iterator iterator = iterator();
        while (iterator.hasNext()) {
            if (!collection.contains(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    public Iterator iterator() {
        return new ImmutableIterator(fieldDefinitiontable.values().iterator());
    }

    public Object[] toArray(Object[] a) {
        return fieldDefinitiontable.values().toArray(a);
    }

    /**
     * Validate this object
     * field definition table should have atleast two field definitions (one key field definition for a key field and one for a data field)
     *
     * @throws Exceptions
     */
    public void validate() throws Exceptions {
        Exceptions exceptions = new Exceptions();
        if (fieldDefinitiontable.isEmpty()) {
            exceptions.add(new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.NO_FIELDS), ServiceErrorID.INVALID_CONFIGURATION_ERROR));
        } else if (fieldDefinitiontable.size() == 1) {
            exceptions.add(new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.INSUFFICIENT_FIELDS), ServiceErrorID.INVALID_CONFIGURATION_ERROR));
        }
        if (getKeyFieldDefinitions().isEmpty()) {
            exceptions.add(new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.NO_KEY_FIELDS), ServiceErrorID.INVALID_CONFIGURATION_ERROR));
        }
        if (getDataFieldDefinitions().isEmpty()) {
            exceptions.add(new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.NO_DATA_FIELDS), ServiceErrorID.INVALID_CONFIGURATION_ERROR));
        }
        if (exceptions.size() > 0) {
            throw exceptions;
        }
    }

    public String toString() {
        return fieldDefinitiontable.toString();
    }

    public Object clone() throws CloneNotSupportedException {

        FieldDefinitions clone = (FieldDefinitions) super.clone();

        if (getFieldDefinitiontable() != null) {
            clone.setFieldDefinitiontable((Hashtable) getFieldDefinitiontable().clone());
        }

        return clone;
    }
}