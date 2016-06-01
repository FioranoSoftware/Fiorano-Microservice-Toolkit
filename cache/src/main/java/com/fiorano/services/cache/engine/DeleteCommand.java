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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Operation which delete given entry from the storage
 *
 * @author Venkat
 */
public class DeleteCommand implements ICacheCommand {
    private IStorage storage;
    private Logger logger;

    public DeleteCommand(IStorage storage, Logger logger) {
        this.storage = storage;
        this.logger = logger;
    }

    /**
     * remove the given entry from storage
     *
     * @param entry
     * @return
     * @throws CacheException
     */
    public CacheEntry execute(CacheEntry entry) throws CacheException {
        try {
            CacheEntry cacheEntry = storage.remove(entry.getKey());
            logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.DELETE_CACHE_ENTRY, new Object[]{cacheEntry}));
            return cacheEntry;
        } catch (CacheException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * removes all entries from storage
     */
    public Iterator executeAll() throws CacheException {
        Iterator entries = storage.getEntries();
        List entryList = new LinkedList();
        while (entries.hasNext()) {
            entryList.add(entries.next());
        }
        storage.clear();
        logger.log(Level.FINE, RBUtil.getMessage(Bundle.class, Bundle.DELETE_CACHE));
        return entryList.iterator();
    }
}
