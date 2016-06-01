/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine;

import com.fiorano.services.cache.engine.storage.CacheEntry;

import java.util.Iterator;

/**
 * An interface which has to be implemented by Classes which perform a specific operation on the storage
 *
 * @author Venkat
 */
public interface ICacheCommand {

    /**
     * performs the operation the implementation is intended to perform on Cache
     *
     * @throws CacheException any exception operations on Storage might throw
     */
    CacheEntry execute(CacheEntry entry) throws CacheException;

    /**
     * performs operation the implementationis intended to perform on all entires in Storage
     */
    Iterator executeAll() throws CacheException;
}
