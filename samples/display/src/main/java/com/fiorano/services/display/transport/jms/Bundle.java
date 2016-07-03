/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.transport.jms;

/**
 * Created by IntelliJ IDEA.
 * User: chaitanya
 * Date: Jan 31, 2008
 * Time: 11:39:12 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Bundle {

    /**
     * @msg.message msg="Exception occured while getting text"
     */
    public final static String ERROR_GETTING_TEXT = "error_getting_text";
    /**
     * @msg.message msg="Error while message in view: "
     */
    public final static String ERROR_MSG_INVIEW = "error_msg_inview";
    /**
     * @msg.message msg="Display Service received message: {0}"
     */
    public final static String RECEIVED_MSG = "received_msg";

}