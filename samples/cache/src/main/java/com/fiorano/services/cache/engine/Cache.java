/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.services.cache.engine.storage.IStorage;
import com.fiorano.services.cache.engine.storage.StorageFactory;

/**
 * A class which holds all the data for operation of Cache
 *
 * @author Venkat
 */
public class Cache {

    private IStorage storage;
    private CachePM configuration;

    /**
     * creats a cache for given configuration
     */
    public Cache(CachePM configuration) {
        this.configuration = configuration;
        storage = StorageFactory.createStorage(configuration);
    }

    /**
     * returns the storage object
     */
    public IStorage getStorage() {
        return storage;
    }

    /**
     * returns the cache configuration
     */
    public CachePM getConfiguration() {
        return configuration;
    }

    /**
     * removes all entries from the cache
     */
    public void clear() throws CacheException {
        try {
            storage.clear();
        } finally {
            storage = null;
            configuration = null;
        }
    }
}
