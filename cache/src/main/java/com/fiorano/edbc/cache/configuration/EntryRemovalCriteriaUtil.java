/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.cache.configuration;

/**
 * @author Venkat
 */
final class EntryRemovalCriteriaUtil {
    private static final String LEAST_RECENTLY_ADDED = "Least recently added";
    private static final String LEAST_RECENTLY_UPDATED = "Least recently updated";
    private static final String LEAST_RECENTLY_ACCESSED = "Least recently accessed";

    static int getCriteria(String criteriaAsString) {
        if (LEAST_RECENTLY_ACCESSED.equals(criteriaAsString)) {
            return CachePM.LEAST_RECENTLY_ACCESSED;
        } else if (LEAST_RECENTLY_UPDATED.equals(criteriaAsString)) {
            return CachePM.LEAST_RECENTLY_UPDATED;
        } else {
            return CachePM.LEAST_RECENTLY_ADDED;
        }
    }

    static String getCriteriaAsString(int criteria) {
        switch (criteria) {
            case CachePM.LEAST_RECENTLY_ACCESSED:
                return LEAST_RECENTLY_ACCESSED;
            case CachePM.LEAST_RECENTLY_UPDATED:
                return LEAST_RECENTLY_UPDATED;
            default:
                return LEAST_RECENTLY_ADDED;
        }
    }

}
