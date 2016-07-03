/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine;

import com.fiorano.services.cache.engine.storage.CacheEntry;
import com.fiorano.services.cache.engine.storage.IStorage;
import com.fiorano.services.common.util.RBUtil;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs lookup / Add / Update operations from the storage for a given CacheEntry.
 *
 * @author Venkat
 */
public class AddUpdateLookupCommand implements ICacheCommand {
    private IStorage storage;
    private Logger logger;

    public AddUpdateLookupCommand(IStorage storage, Logger logger) {
        this.storage = storage;
        this.logger = logger;
    }

    /**
     * if value is not null and key does not exists add the entry to storage
     * if value is not null and key exists update the entry in the storage
     * if value is null and key is not null, performs the lookup in the storage for given key
     *
     * @throws CacheException exception while performing operations on storage
     */
    public CacheEntry execute(CacheEntry entry) throws CacheException {
        if (entry.getValue() != null) {
            //if key is null exception is thrown
            //if key is unique the new entry is returned
            //if key is no unqiue then this entry is added and returned
            try {
                storage.update(entry);
                logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.UPDATE_CACHE_ENTRY, new Object[]{entry}));
            } catch (CacheException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            return entry;
        } else {
            //throws error if key is null
            boolean containsValue = false;
            try {
                containsValue = storage.contains(entry.getKey());
            } catch (CacheException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            if (containsValue) {
                CacheEntry lookup = null;
                try {
                    lookup = storage.get(entry.getKey());
                    logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.LOOKUP_CACHE_ENTRY, new Object[]{entry.getKey(), lookup}));
                } catch (CacheException e) {
                    logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                }
                return lookup;
            } else {
                logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.CANNOT_LOOKUP, new Object[]{entry.getKey()}));
                return null;
            }
        }
    }

    /**
     * used to lookup all entries
     */
    public Iterator executeAll() throws CacheException {
        return storage.getEntries();
    }
}
