/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

import com.fiorano.services.common.util.RBUtil;

/**
 * A field which can be stored as a key in the storage
 *
 * @author Venkat
 */
public class KeyField extends Field {

    public KeyField(String name, Object value) {
        setName(name);
        setValue(value);
    }

    public void setValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException(RBUtil.getMessage(Bundle.class, Bundle.NULL_KEY_FIELD_VALUE));
        }
        super.setValue(value);
    }
}
