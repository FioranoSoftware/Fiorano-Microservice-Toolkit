/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

import com.fiorano.services.common.util.RBUtil;

import java.io.Serializable;
import java.util.*;

/**
 * List implemenation to hold Fields. This can be used to avoid casting
 *
 * @author Venkat
 */
public class FieldList extends AbstractList<IField> {
    /**
     * The empty list (immutable).  This list is serializable.
     */
    public static final FieldList EMPTY_LIST = new EmptyList();
    private List<IField> list = null;

    public static FieldList getAsList(IField[] fields) {
        if (fields == null) {
            return null;
        }
        FieldList list = new FieldList();
        list.addAll(Arrays.asList(fields));
        return list;
    }

    public static IField[] getAsArray(FieldList list) {
        if (list == null) {
            return null;
        }
        return list.toArray(new IField[list.size()]);
    }

    private List<IField> getList() {
        if (list == null) {
            list = new LinkedList<IField>();
        }
        return list;
    }

    public IField get(int index) {
        return getList().get(index);
    }

    /**
     * returns the Field stored at given index.
     *
     * @return Field at given index
     */
    public IField getField(int index) {
        return getList().get(index);
    }

    public int size() {
        return getList().size();
    }

    /**
     * Replaces the Field at the specified position in this list with the
     * specified Field.
     *
     * @return Field at given index
     */
    public IField setField(int index, IField element) {
        return getList().set(index, element);
    }

    public IField set(int index, IField element) {
        if (!(element instanceof IField)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_ELEMENT,
                    new Object[]{element.getClass().getName(), IField.class.getName()}));
        }
        return getList().set(index, (IField) element);
    }

    /**
     * Removes the Field at the specified position in this list (optional
     * operation).  Shifts any subsequent Field to the left (subtracts one
     * from their indices).  Returns the Field that was removed from the
     * list.
     */
    public IField removeField(int index) {
        return getList().remove(index);
    }

    public IField remove(int index) {
        return getList().remove(index);
    }

    public void add(int index, IField element) {
        if (!(element instanceof IField)) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_ELEMENT,
                    new Object[]{element.getClass().getName(), IField.class.getName()}));
        }
        getList().add(index, element);
    }

    public void reset() {
        list = null;
    }

    /**
     * Inserts the specified Field at the specified position in this list
     * Shifts the element currently at that position
     * (if any) and any subsequent Fields to the right (adds one to their
     * indices).<p>
     */
    public void addField(int index, IField element) {
        getList().add(index, element);
    }

    private static class EmptyList extends FieldList
            implements RandomAccess, Serializable {
        public int size() {
            return 0;
        }

        public boolean contains(Object obj) {
            return false;
        }

        public IField get(int index) {
            throw new IndexOutOfBoundsException(RBUtil.getMessage(Bundle.class, Bundle.INDEX_OUT_OF_BOUNDS,
                    new Object[]{index}));
        }

        // Preserves singleton property
        private Object readResolve() {
            return EMPTY_LIST;
        }
    }

}
