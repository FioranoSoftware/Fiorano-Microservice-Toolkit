/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

/**
 * Field is a object which holds a name value pair which can be contained in a CacheEntry. This is a non
 * key field
 *
 * @author Venkat
 */
public class DataField extends Field {
    public DataField(String name, Object value) {
        setName(name);
        setValue(value);
    }

}
