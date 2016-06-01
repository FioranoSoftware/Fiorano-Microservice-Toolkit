/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author FSIPL
 * @version 1.0
 * @created March 24, 2005
 */
public interface Bundle {

    /**
     * @msg.message msg="Received Message <MESSAGE>{0}</MESSAGE>"
     */
    String RECIEVED_MESSAGE = "recieved_message";

    /**
     * @msg.message msg="Sent Message <MESSAGE>{0}</MESSAGE>"
     */
    String SENT_MESSAGE = "sent_message";

    /**
     * @msg.message msg=" Created producer on {0}."
     */
    String PRODUCER_CREATED = "producer_created";

    /**
     * @msg.message msg=" Naming exception"
     */
    String NAMING_EXCEPTION = "naming_exception";

    /**
     * @msg.message msg="Time taken (milli sec) to process request: {0}."
     */
    String TRANSACTION_TIME = "transaction_time";

    /**
     * @msg.message msg="Invalid Number of Output Ports"
     */
    String INVALID_NO_OF_PORTS = "invalid_no_of_ports";

    /**
     * @msg.message msg="Please specify a value between {0} and {1}"
     */
    String VALUE_BET = "value_bet";

    /**
     * @msg.message msg="Invalid Weight"
     */
    String INVALID_WEIGHT = "invalid_weight";

    /**
     * @msg.message msg="Invalid weight {0} specified for {1}"
     */
    String INVALID_WEIGHT_DESC = "invalid_weight_desc";

}

