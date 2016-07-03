/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.storage;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.services.cache.engine.CacheException;
import com.fiorano.services.common.util.RBUtil;

/**
 * Removes the Least Recently Accessed entries from the Cache
 *
 * @author Venkat
 */
public class LRAccessedHashtableStorage extends AbstractLRUHashtableStorage {
    public LRAccessedHashtableStorage(CachePM configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_CONFIGURATION));
        }
        if (configuration.getInitialCapaity() > 0) {
            this.hashmap = new LRUHashMap(configuration.getInitialCapaity(), configuration.getThresholdSize(), true);
        } else {
            this.hashmap = new LRUHashMap(configuration.getThresholdSize(), true);
        }
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
}
