/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.internal;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 14-Feb-2011
 * Time: 14:09:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class StateListener implements Observer {

    public final void update(Observable module, Object stateChangeEvent) {
        if (stateChangeEvent instanceof IModule.StateChangeEvent) {
            stateChanged((IModule.StateChangeEvent) stateChangeEvent);
        }
    }

    protected abstract void stateChanged(IModule.StateChangeEvent stateChangeEvent);

}
