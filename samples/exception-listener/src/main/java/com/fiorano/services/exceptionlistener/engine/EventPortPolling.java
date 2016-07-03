/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.exceptionlistener.engine;

import com.fiorano.services.exceptionlistener.Bundle;
import com.fiorano.services.exceptionlistener.Constants;
import com.fiorano.services.exceptionlistener.configuration.ExceptionListenerConfiguration;
import fiorano.esb.utils.RBUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: bhuvang
 * Date: Jun 2, 2006
 * Time: 1:50:12 AM
 * To change this template use File | Settings | File Templates.
 *
 * @author FSIPL
 * @created June 2, 2006
 * @version 1.0
 */
public class EventPortPolling extends Thread
{
    private boolean running = true;
    private ExceptionListenerJob exceptionListenerJob;
    private Logger  m_logger;
    private int timeSlice = 10000;
    private ExceptionListenerConfiguration model;

    public void setRunning(boolean running){
        this.running = running;
    }
    public boolean getRunning(){
        return running;
    }
    /**
     * @param exceptionListenerJob
     */
    public EventPortPolling(ExceptionListenerJob exceptionListenerJob)
    {
        this.exceptionListenerJob = exceptionListenerJob;
        m_logger = exceptionListenerJob.getLogger();
        this.model = exceptionListenerJob.getModel();
        if (model.getTimeSliceForPing() != null)
            timeSlice = model.getTimeSliceForPing();
    }

    /**
     * Main processing method for the EventPortPolling object
     */
    public void run()
    {
        while (running)
        {
            try
            {
                sleep(timeSlice);
            }
            catch (Exception e)
            {
            }
            try
            {
                if (getRunning()) {
                    if (Constants.ACTIVE_MQ.equalsIgnoreCase(model.getJmsProvider())) {
                        exceptionListenerJob.subscribeToActiveMqTopics();
                    } else {
                        exceptionListenerJob.subscribeToFMQTopics();
                    }
                }
            }
            catch (Exception e)
            {
                this.setRunning(false);
                m_logger.log(Level.SEVERE, RBUtil.getMessage(Bundle.class, Bundle.POLLING_STOPED), e);
            }

        }

    }

}