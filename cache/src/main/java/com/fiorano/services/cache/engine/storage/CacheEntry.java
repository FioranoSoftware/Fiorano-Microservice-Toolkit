/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.storage;

import com.fiorano.services.cache.engine.dmi.IField;
import com.fiorano.services.cache.engine.dmi.KeyField;
import com.fiorano.util.StringUtil;
import com.fiorano.util.Util;

import java.util.Map;

/**
 * An entry which can be stored into storage. it is a <key, value> map, where key is a KeyField and value is a FieldMap
 *
 * @author FSTPL
 */
public class CacheEntry {
    private KeyField key;
    private Map<String, IField> value;

    public CacheEntry(KeyField key, Map<String, IField> value) {
        this.key = key;
        this.value = value;
    }

    /**
     * returns the KeyField (Key NV pair)
     */
    public KeyField getKey() {
        return key;
    }

    /**
     * returns the Value that the key in KeyField should map to. the returned value is FieldMap<name,Field> where Field is a DataField (Data NVPair)
     *
     * @return
     */
    public Map<String, IField> getValue() {
        return value;
    }

    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (key == null ? 0 : key.hashCode());
        hashCode = 31 * hashCode + (value == null ? 0 : value.hashCode());

        return hashCode;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CacheEntry)) {
            return false;
        }
        CacheEntry entry = (CacheEntry) obj;
        return Util.equals(key, entry.getKey()) && Util.equals(value, entry.getValue());
    }

    public String toString() {
        return "{" + key + "::" + value + "}" + StringUtil.LINE_SEP;
    }

}
