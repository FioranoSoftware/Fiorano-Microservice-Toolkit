/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.storage;

import com.fiorano.services.cache.engine.CacheException;
import com.fiorano.services.cache.engine.dmi.KeyField;

import java.io.PrintStream;
import java.util.Iterator;

/**
 * This is the storage interface for
 *
 * @author FSTPL
 */
public interface IStorage {
    CacheEntry add(CacheEntry entry) throws CacheException;

    CacheEntry remove(CacheEntry entry) throws CacheException;

    CacheEntry get(KeyField key) throws CacheException;

    boolean contains(KeyField key) throws CacheException;

    CacheEntry remove(KeyField key) throws CacheException;

    boolean contains(CacheEntry entry) throws CacheException;

    Iterator /* CacheEntry objects iterator */ getEntries();

    void printEntries(PrintStream outputStream);

    CacheEntry update(CacheEntry entry) throws CacheException;

    void clear() throws CacheException;
}
