/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.edbc.framework.service.cps;

import com.fiorano.util.ErrorListener;
import org.openide.ErrorManager;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public class ValidationErrorListener implements ErrorListener {
    private boolean errorOccured = false;
    private Exception exception = null;

    public void warning(Exception exception) throws Exception {
        errorOccured = true;
        this.exception = exception;
        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exception);
    }

    public void error(Exception exception) throws Exception {
        errorOccured = true;
        this.exception = exception;
        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exception);
    }

    public void fatalError(Exception exception) throws Exception {
        errorOccured = true;
        this.exception = exception;
        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exception);
    }

    public boolean isErrorOccured() {
        return errorOccured;
    }

    public Exception getException() {
        return exception;
    }

    public void reset() {
        this.errorOccured = false;
        this.exception = null;
    }

}
