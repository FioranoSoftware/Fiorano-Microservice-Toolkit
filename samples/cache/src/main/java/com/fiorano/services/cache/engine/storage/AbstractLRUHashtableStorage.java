/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.storage;

import com.fiorano.services.cache.engine.CacheException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Date: Mar 2, 2007
 * Time: 9:10:08 AM
 *
 * @author Venkat
 */
public abstract class AbstractLRUHashtableStorage extends HashtableStorage {
//    private int entryRemovalCriteria;

    public abstract CacheEntry update(CacheEntry entry) throws CacheException;

    /**
     * HashMap whose maximum size is constant. If a new entry is added such that the size
     * of hashMap is greater than the threshold it removes the oldest entry added
     */
    protected static class LRUHashMap extends LinkedHashMap {
        private int maxSize;

        public LRUHashMap(int maxSize) {
            this(maxSize, false);
        }

        public LRUHashMap(int maxSize, boolean accessOrder) {
            super(16, 0.75f, accessOrder);
            this.maxSize = maxSize;
        }

        public LRUHashMap(int initialCapacity, int maxSize, boolean accessOrder) {
            super(initialCapacity, 0.75f, accessOrder);
            this.maxSize = maxSize;
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxSize;
        }
    }

}
