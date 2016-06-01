/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.feeder.engine;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: Dec 23, 2009
 * Time: 6:41:29 PM
 * To change this template use File | Settings | File Templates.
 */

public interface Bundle {
    /**
       * @msg.message msg="Model String"
       */
      public final static String MODEL_STRING = "model_string";
      /**
       * @msg.message msg="Message"
       */
      public final static String MESSAGE = "message";

      /**
       * @msg.message msg="Exception while creating components"
       */
      public final static String EXCEP_CREATING_COMP = "excep_creating_comp";

}