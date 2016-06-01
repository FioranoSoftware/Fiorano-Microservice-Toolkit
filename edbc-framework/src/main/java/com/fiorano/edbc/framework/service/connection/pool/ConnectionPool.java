/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.connection.pool;

import com.fiorano.edbc.framework.service.connection.IConnection;
import org.apache.commons.pool2.ObjectPool;

public class ConnectionPool {

    private ObjectPool<IConnection> objectPool;

    public ConnectionPool(ObjectPool<IConnection> objectPool) {
        this.objectPool = objectPool;
    }

    public IConnection getConnection() throws Exception {
        return objectPool.borrowObject();
    }

    public void returnConnection(IConnection connection) throws Exception {
        objectPool.returnObject(connection);
    }

    public void close() {
        objectPool.close();
    }
}
