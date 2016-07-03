/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.dmi;

/**
 * interface which can be implemented by Class which have to be treated as fields which can be stored into
 * the database. Such classes should override equals and hashcode if the different objects with same data
 * have to be treated as equal.
 *
 * @author Venkat
 */
public interface IField {

    /**
     * returns name of the field
     */
    String getName();

    /**
     * set the name for the field
     */
    void setName(String name);

    /**
     * returns value of the field
     */
    Object getValue();

    /**
     * set the value for the field
     */
    void setValue(Object value);
}
