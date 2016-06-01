/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.engine;

/**
 * Created by IntelliJ IDEA.
 * Date: Aug 25, 2007
 * Time: 11:44:51 PM
 *
 * @author Venkat
 * @version 1.0, 25 August 2007
 */
public class CBRConfiguration {
    private String condition;
    private boolean xpath1_0;
    private String field;
    private boolean fioranoCBR;
    private String destination;

    public CBRConfiguration(String condition, String field, String destination, boolean fioranoCBR, boolean xpath1_0) {
        this.condition = condition;
        this.field = field;
        this.fioranoCBR = fioranoCBR;
        this.xpath1_0 = xpath1_0;
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean useXpath1_0() {
        return xpath1_0;
    }

    public void setXpath1_0(boolean xpath1_0) {
        this.xpath1_0 = xpath1_0;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isFioranoCBR() {
        return fioranoCBR;
    }

    public void setFioranoCBR(boolean fioranoCBR) {
        this.fioranoCBR = fioranoCBR;
    }

}
