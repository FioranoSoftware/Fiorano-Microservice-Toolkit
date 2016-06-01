/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.feeder.ps.panels;

/**
 * Created by IntelliJ IDEA.
 * User: geetha
 * Date: Oct 31, 2007
 * Time: 5:49:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Attachment {
    private String name;
    private byte[] value;
    private String attachmentVal;

    public byte[] fetchValue() {
        return value;
    }

    public void saveValue(byte[] value) {
        this.value = value;
        attachmentVal = base64.Base64.encodeBytes(value);
    }

    public String getName() {
        return name;
    }

    public String getAttachmentVal() {
        return attachmentVal;
    }

    public void setAttachmentVal(String attachmentVal) {
        this.attachmentVal = attachmentVal;
        value = base64.Base64.decode(attachmentVal);
    }

    public void setName(String name) {
        this.name = name;
    }

}
