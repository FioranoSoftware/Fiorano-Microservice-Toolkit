/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache;

import com.fiorano.xml.ClarkName;

public interface CacheConstants {
    String CACHE = "Cache";
    String CACHE_ENTRY = "CacheEntry";
    String DATA = "Data";
    String KEY = "Key";
    String KEYS = "Keys";
    String TNS = "http://www.fiorano.com/SOA/bc/cache";
    String VALUE = "Value";
    String ALL = "All";
    String ROOT_ELEMENT_CN = ClarkName.toClarkName(TNS, CACHE);

    String FIORANO_XML_DELIMITER = "$$FIORANO__DELIMITER$$";
    String NS_ELEMENT_DELIMITER = "$$FIORANO_NS_ELEMENT_DELIMITER$$";
    String CURLY_BRACES_START_REPLACER = "$$CURLY__BRACES__START$$";
    String CURLY_BRACES_END_REPLACER = "$$CURLY__BRACES__END$$";

    String ADD_PORT = "ADD_PORT";
    String DEL_PORT = "DEL_PORT";
    String OUT_PORT = "OUT_PORT";
}
