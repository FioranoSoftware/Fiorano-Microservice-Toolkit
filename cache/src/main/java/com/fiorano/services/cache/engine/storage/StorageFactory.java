/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.storage;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.services.common.util.RBUtil;

/**
 * Created by IntelliJ IDEA.
 * Date: Mar 2, 2007
 * Time: 9:27:45 AM
 *
 * @author Venkat
 */
public final class StorageFactory {
    public static IStorage createStorage(CachePM configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_CONFIGURATION));
        }
        if (configuration.getThresholdSize() <= 0) {
            return new HashtableStorage(configuration);
        }
        switch (configuration.fetchEntryRemovalCriteria()) {
            case CachePM.LEAST_RECENTLY_ACCESSED:
                return new LRAccessedHashtableStorage(configuration);
            case CachePM.LEAST_RECENTLY_UPDATED:
                return new LRUpdatedHashtableStorage(configuration);
            default:
                return new LRAddedHashtableStorage(configuration);
        }
    }
}
