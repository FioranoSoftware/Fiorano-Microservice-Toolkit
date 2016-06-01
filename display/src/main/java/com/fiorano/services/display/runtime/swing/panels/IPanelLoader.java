/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.display.runtime.swing.panels;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 13 Aug, 2010.
 */
public interface IPanelLoader<M extends Message> {

    void load(M Message) throws Exception;

    void saveMessage(M message, File file) throws IOException, JMSException;

}
