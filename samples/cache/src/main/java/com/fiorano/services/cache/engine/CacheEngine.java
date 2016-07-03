/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cache.engine;

import com.fiorano.edbc.cache.configuration.CachePM;
import com.fiorano.edbc.framework.service.exception.ServiceExecutionException;
import com.fiorano.edbc.framework.service.internal.IModule;
import com.fiorano.edbc.framework.service.internal.connection.ConnectionManager;
import com.fiorano.edbc.framework.service.internal.engine.Engine;
import com.fiorano.edbc.framework.service.internal.engine.IRequestProcessor;
import com.fiorano.services.cache.CacheConstants;
import com.fiorano.services.common.util.RBUtil;

import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: Venkat
 * Date: 29-Nov-2010
 * Time: 18:28:13
 * To change this template use File | Settings | File Templates.
 */
public class CacheEngine<CM extends ConnectionManager> extends Engine<CachePM, CM> {
    private Cache cache;

    public CacheEngine(IModule parent, CachePM configuration) {
        super(parent, configuration);
    }

    @Override
    protected void internalCreate() throws ServiceExecutionException {
        super.internalCreate();
        cache = new Cache(configuration);
    }

    @Override
    protected void internalDestroy() throws ServiceExecutionException {
        if (cache != null) {
            try {
                cache.clear();
            } catch (CacheException e) {
                logger.log(Level.WARNING, RBUtil.getMessage(Bundle.class, Bundle.CACHE_CLEAR_FAILED, new Object[]{e.getLocalizedMessage()}), e);
            } finally {
                cache = null;
            }
        }
        super.internalDestroy();
    }

    public IRequestProcessor createRequestProcessor(IModule parent, String type) {
        ICacheCommand cacheCommand;
        if (CacheConstants.ADD_PORT.equals(type)) {
            cacheCommand = new AddUpdateLookupCommand(cache.getStorage(), logger);
        } else {
            cacheCommand = new DeleteCommand(cache.getStorage(), logger);
        }
        return new CacheRequestProcessor(parent, configuration, cacheCommand, logger);
    }

    public Cache getCache() {
        return cache;
    }

}
