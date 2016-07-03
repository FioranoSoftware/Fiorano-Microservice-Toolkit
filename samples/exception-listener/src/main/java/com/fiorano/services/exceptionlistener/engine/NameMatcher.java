/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.exceptionlistener.engine;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: Apr 1, 2008
 * Time: 3:14:42 PM
 * To change this template use File | Settings | File Templates.
 */
public final class NameMatcher {
    public static boolean matches(String name, Map patterns ) {
        if(patterns == null || patterns.isEmpty())  {
            return false;
        }
        Iterator regexIter = patterns.entrySet().iterator();
        while (regexIter.hasNext()) {
            Map.Entry regexEntry = (Map.Entry) regexIter.next();
            final String shouldMatch = (String) regexEntry.getValue();
            final String pattern = (String) regexEntry.getKey();
            if (!validate(name, pattern, shouldMatch != null && Boolean.parseBoolean(shouldMatch))) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchesOR(String name, Map patterns ) {
        if(patterns == null || patterns.isEmpty())  {
            return false;
        }
        Iterator regexIter = patterns.entrySet().iterator();
        while (regexIter.hasNext()) {
            Map.Entry regexEntry = (Map.Entry) regexIter.next();
            final String shouldMatch = (String) regexEntry.getValue();
            final String pattern = (String) regexEntry.getKey();
            if (validate(name, pattern, shouldMatch != null && Boolean.parseBoolean(shouldMatch))) {
                return true;
            }
        }
        return false;
    }

    public static boolean validate(String name, String pattern, boolean shouldMatch) {
        return shouldMatch ? name.matches(pattern) : !name.matches(pattern);
    }
}
