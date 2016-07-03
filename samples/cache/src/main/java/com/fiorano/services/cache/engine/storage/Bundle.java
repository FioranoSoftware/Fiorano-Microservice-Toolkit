/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine.storage;

/**
 * Created by IntelliJ IDEA.
 * Date: Jan 25, 2007
 * Time: 4:22:48 PM
 *
 * @author
 */
public interface Bundle {

    /**
     * @msg.message msg="Entry cannot be null"
     */
    String NULL_ENTRY = "null_entry";

    /**
     * @msg.message msg="Key cannot be null"
     */
    String NULL_KEY = "null_key";

    /**
     * @msg.message msg="Value cannot be null"
     */
    String NULL_VALUE = "null_value";

    /**
     * @msg.message msg="Value cannot be null"
     */
    String REMOVE_NOT_SUPPORTED_STORAGE_ITERATOR = "remove_not_supported_storage_iterator";

    /**
     * @msg.message msg="Configuration is null, cannot instantiate LRU storage"
     */
    String NULL_CONFIGURATION = "null_configuration";
}
