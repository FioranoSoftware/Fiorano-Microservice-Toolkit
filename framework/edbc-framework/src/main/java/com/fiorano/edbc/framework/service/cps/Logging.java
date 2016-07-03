/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.cps;

import fiorano.esb.util.CPSUtil;

import java.util.logging.Logger;

/**
 * Date: Mar 11, 2007
 * Time: 3:46:25 PM
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Logging {
    Logger logger = CPSUtil.getAnonymousLogger();
}
