/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.feeder;

import com.fiorano.bc.feeder.model.FeederPM;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Nov 16, 2007
 * Time: 12:02:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationConverter {
    public static FeederPM convert(Object properties) {
        Properties prop;
        FeederPM configuration = new FeederPM();
        if (properties instanceof Properties) {
            prop = (Properties) properties;
            configuration.setDefaultMessage(prop.getProperty("Message"));
            configuration.setHistorySize(Integer.parseInt(prop.getProperty("HistorySize")));
            configuration.setMessageFormat(FeederPM.convertFormat(prop.getProperty("MessageFormat")));
        } else {
            configuration = (FeederPM) properties;
        }
        return configuration;
    }
}
