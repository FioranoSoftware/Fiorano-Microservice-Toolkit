/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.util;



import com.fiorano.bc.display.model.Configuration;
import com.fiorano.bc.display.model.ConfigurationPM;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Spurthy
 * Date: Feb 3, 2011
 * Time: 12:02:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationConverter {

    public static ConfigurationPM convert(Object properties) {
        Configuration prop;
        ConfigurationPM configuration = new ConfigurationPM();
        if (properties instanceof Configuration) {
            prop = (Configuration) properties;
            configuration.setMaxBufferedMessages(prop.getMaxBufferedMessages());
        } 
        return configuration;
    }
}