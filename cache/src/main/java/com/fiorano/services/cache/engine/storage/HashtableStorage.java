/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.storage;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.services.cache.engine.CacheException;
import com.fiorano.services.cache.engine.dmi.IField;
import com.fiorano.services.cache.engine.dmi.KeyField;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.Util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Storage which holds all the entries in a Hashtable, if the threshold size is greater than 0 it removes
 * oldest entry when a new entry is added which causes the size to go beyond the threshold value
 *
 * @author
 */
public class HashtableStorage implements IStorage {
    protected final Object lock = new Object();
    protected Map hashmap;

    protected HashtableStorage() {
    }

    public HashtableStorage(CachePM configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_CONFIGURATION));
        }
        if (configuration.getInitialCapaity() > 0) {
            this.hashmap = new HashMap(configuration.getInitialCapaity());
        } else {
            this.hashmap = new HashMap();
        }

    }

    /**
     * Adds a given entry (key-value pair) to the storage
     *
     * @param entry the key-value pair which has to be added into the storage
     * @return the previous value of the specified key in this hashmap, or <code>entry</code> if it did not have one.
     */
    public CacheEntry add(CacheEntry entry) throws CacheException {
        if (entry == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_ENTRY));
        }
        if (entry.getKey() == null) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.NULL_KEY));
        }
        if (entry.getValue() == null) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.NULL_VALUE));
        }
        Map<String, IField> value;
        synchronized (lock) {
            value = (Map<String, IField>) hashmap.put(entry.getKey(), entry.getValue());
        }
        return new CacheEntry(entry.getKey(), value != null ? value : entry.getValue());
    }

    /**
     * Adds a given entry (key-value pair) to the storage if key is not present. updates the dataMap with value of new dataMap if the key is already present
     * It differs from the add in the way entry is updated if exists. update will add the entries of dataMap into existing dataMap, where as add will
     * replace the old dataMap with the new dataMap
     *
     * @param entry the key-value pair which has to be added into the storage
     * @return the previous value of the specified key in this hashmap, or <code>entry</code> if it did not have one.
     */
    public CacheEntry update(CacheEntry entry) throws CacheException {
        if (entry == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_ENTRY));
        }
        if (entry.getKey() == null) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.NULL_KEY));
        }
        if (entry.getValue() == null) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.NULL_VALUE));
        }
        CacheEntry oldEntry;
        synchronized (lock) {
            oldEntry = get(entry.getKey());
            if (oldEntry != null) {
                oldEntry.getValue().putAll(entry.getValue());
            } else {
                hashmap.put(entry.getKey(), entry.getValue());
            }
        }
        return new CacheEntry(entry.getKey(), oldEntry == null ? entry.getValue() : oldEntry.getValue());
    }

    /**
     * Removes the entry whose key matches the <code>key</code> from this storage (underlying hashmap).
     * This method does nothing if the key is not in this storage (underlying hashmap).
     *
     * @param key the key whose corresponding entry has to be removed.
     * @return the entry(key-value pair) to which the key had been mapped in this storage (underlying hashmap),
     * or <code>null</code> if the key did not have a mapping.
     */
    public CacheEntry remove(KeyField key) throws CacheException {
        if (key == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_KEY));
        }
        synchronized (lock) {
            if (contains(key)) {
                Map<String, IField> value = (Map<String, IField>) hashmap.remove(key);
                return new CacheEntry(key, value);
            }
        }
        return null;
    }

    /**
     * Removes the entry (key-value) pair which matches the <code>entry</code>
     *
     * @param entry the entry (key-value) pair which has to removed from this storage
     * @return entry if the <code>entry</code> exists and is removed. <code>Null</code>
     * if the <code>entry</code> does not satisfy <code>equals</code> condition with the entry for the
     * corresponding key present in the hasmap
     */
    public CacheEntry remove(CacheEntry entry) throws CacheException {
        if (entry == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_ENTRY));
        }

        KeyField key = entry.getKey();
        synchronized (lock) {
            if (contains(key)) {
                Map<String, IField> value = (Map<String, IField>) hashmap.get(key);
                Map<String, IField> valueToRemove = entry.getValue();
                if (value == valueToRemove || Util.equals(value, valueToRemove)) {
                    hashmap.remove(key);
                    return new CacheEntry(key, value);
                }
            }
        }
        return null;

    }

    /**
     * returns the key-value pair for the give key if it exists in this storage
     *
     * @return key-value pair if the corresponding key is present or <code>Null</code> if the key is not contained in the storage
     */
    public CacheEntry get(KeyField key) throws CacheException {
        if (key == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_KEY));
        }
        synchronized (lock) {
            if (contains(key)) {
                Map<String, IField> value = (Map<String, IField>) hashmap.get(key);
                return new CacheEntry(key, value);
            }
        }
        return null;
    }

    /**
     * return true if this storage contains an entry with the given key, false otherwise
     */
    public boolean contains(KeyField key) throws CacheException {
        if (key == null) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.NULL_KEY));
        }
        synchronized (lock) {
            return hashmap.containsKey(key);
        }
    }

    /**
     * return true if this storage contains an entry with the given key-value pair, false otherwise
     */
    public boolean contains(CacheEntry entry) throws CacheException {
        if (entry == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_ENTRY));
        }
        CacheEntry existingEntry;
        synchronized (lock) {
            existingEntry = get(entry.getKey());
        }
        return entry.equals(existingEntry);
    }

    public Iterator getEntries() {
        synchronized (lock) {
            return new CacheEntriesIterator(hashmap.entrySet().iterator());
        }
    }

    public void clear() throws CacheException {
        synchronized (lock) {
            hashmap.clear();
        }
    }

    public void printEntries(PrintStream outputStream) {
        Iterator entryIter = hashmap.entrySet().iterator();
        while (entryIter.hasNext()) {
            Map.Entry entry = (Map.Entry) entryIter.next();
            outputStream.println(entry.getKey() + "::" + entry.getValue());
        }
    }

    private static class CacheEntriesIterator implements Iterator {

        private Iterator delegate;

        public CacheEntriesIterator(Iterator delegate) {
            this.delegate = delegate;
        }

        public void remove() {
            throw new UnsupportedOperationException(RBUtil.getMessage(Bundle.class, Bundle.REMOVE_NOT_SUPPORTED_STORAGE_ITERATOR));
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public Object next() {
            return nextCacheEntry();
        }

        private Object nextCacheEntry() {
            Map.Entry entry = (Map.Entry) delegate.next();
            return new CacheEntry((KeyField) entry.getKey(), (Map<String, IField>) entry.getValue());
        }
    }
}
