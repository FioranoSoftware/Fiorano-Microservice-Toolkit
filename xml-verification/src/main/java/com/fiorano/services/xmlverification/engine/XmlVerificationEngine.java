/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xmlverification.engine;

import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.connection.ConnectionManager;
import com.fiorano.edbc.framework.service.internal.engine.Engine;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.xmlverification.model.XmlVerificationPM;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 2 Dec, 2010
 * Time: 7:29:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class XmlVerificationEngine<CM extends ConnectionManager> extends Engine<XmlVerificationPM, CM> {
    public XmlVerificationEngine(IModule parent, XmlVerificationPM configuration) {
        super(parent, configuration);
    }

    public IRequestProcessor createRequestProcessor(IModule parent, String type) {
        return new XmlVerificationRequestProcessor(parent, configuration);
    }
}
