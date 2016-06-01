/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.exception;

import fiorano.esb.common.ESBException;
import fiorano.esb.utils.BeanUtils;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * <code>ServiceErrorID</code> is used to indicate type of exception occured.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public final class ServiceErrorID implements Comparable, Serializable {
    /**
     * <code>-3<code> - value to create <code>ServiceErrorID</code> that indicates configuration is invalid
     */
    public static final int INVALID_CONFIGURATION_ERROR_ID = -3;
    /**
     * <code>-2<code> - value to create <code>ServiceErrorID</code> that indicates an exception occured in serialization
     */
    public static final int SERIALIZATION_ERROR_ID = -2;
    /**
     * <code>-1<code> - value to create <code>ServiceErrorID</code> that indicates an exception occured during the launch of service
     */
    public static final int SERVICE_LAUNCH_ERROR_ID = -1;
    /**
     * <code>0<code> - value to create <code>ServiceErrorID</code> that indicates the request is invalid
     */
    public static final int INVALID_REQUEST_ERROR_ID = 0;
    /**
     * <code>1<code> - value to create <code>ServiceErrorID</code> that indicates an exception occured while executing the request
     */
    public static final int REQUEST_EXECUTION_ERROR_ID = 1;
    /**
     * <code>2<code> - value to create <code>ServiceErrorID</code> that indicates an exception occured while generting response
     */
    public static final int RESPONSE_GENERATION_ERROR_ID = 2;
    /**
     * <code>3<code> - value to create <code>ServiceErrorID</code> that indicates a JMS related exception has occured
     */
    public static final int TRANSPORT_ERROR_ID = 3;
    /**
     * <code>4<code> - value to create <code>ServiceErrorID</code> that indicates a connection related exception has occured
     */
    public static final int CONNECTION_ERROR_ID = 4;

    /**
     * <code>ServiceErrorID</code> that indicates configuration is invalid
     */
    public static final ServiceErrorID INVALID_CONFIGURATION_ERROR = new ServiceErrorID(INVALID_CONFIGURATION_ERROR_ID);
    /**
     * <code>ServiceErrorID</code> that indicates an exception occured in serialization
     */
    public static final ServiceErrorID SERIALIZATION_ERROR = new ServiceErrorID(SERIALIZATION_ERROR_ID);
    /**
     * <code>ServiceErrorID</code> that indicates an exception occured during the launch of service
     */
    public static final ServiceErrorID SERVICE_LAUNCH_ERROR = new ServiceErrorID(SERVICE_LAUNCH_ERROR_ID);
    /**
     * <code>ServiceErrorID</code> that indicates the request is invalid
     */
    public static final ServiceErrorID INVALID_REQUEST_ERROR = new ServiceErrorID(INVALID_REQUEST_ERROR_ID);
    /**
     * <code>ServiceErrorID</code> that indicates an exception occured while executing the request
     */
    public static final ServiceErrorID REQUEST_EXECUTION_ERROR = new ServiceErrorID(REQUEST_EXECUTION_ERROR_ID);
    /**
     * <code>ServiceErrorID</code> that indicates an exception occured while generting response
     */
    public static final ServiceErrorID RESPONSE_GENERATION_ERROR = new ServiceErrorID(RESPONSE_GENERATION_ERROR_ID);
    /**
     * <code>ServiceErrorID</code> that indicates a JMS related exception has occured
     */
    public static final ServiceErrorID TRANSPORT_ERROR = new ServiceErrorID(TRANSPORT_ERROR_ID);
    /**
     * <code>ServiceErrorID</code> that indicates a connection related exception has occured
     */
    public static final ServiceErrorID CONNECTION_ERROR = new ServiceErrorID(CONNECTION_ERROR_ID);

    private static final ServiceErrorID[] ERROR_IDS = {
            INVALID_CONFIGURATION_ERROR,
            SERIALIZATION_ERROR,
            SERVICE_LAUNCH_ERROR,
            INVALID_REQUEST_ERROR,
            REQUEST_EXECUTION_ERROR,
            RESPONSE_GENERATION_ERROR,
            TRANSPORT_ERROR,
            CONNECTION_ERROR
    };
    private final static int NAME_DETAIL_INDEX = 0;
    private final static int DESC_DETAIL_INDEX = 1;
    private final static int ERRORS_COUNT = 8;
    private final static int DETAIL_COUNT = 2;
    private static final ServiceErrorIDDeletgate serviceErrorIDDelegate = new ServiceErrorIDDeletgate();
    private static int INDEX_OFFSET = -INVALID_CONFIGURATION_ERROR_ID;
    private static String[][] ERROR_DETAILS = new String[ERRORS_COUNT][DETAIL_COUNT];

    static {
        ERROR_DETAILS[INVALID_CONFIGURATION_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "Invalid configuration Error";
        ERROR_DETAILS[INVALID_CONFIGURATION_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX] = "Service configuration is invalid.";
        ERROR_DETAILS[SERIALIZATION_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "Serialization / deserialization error";
        ERROR_DETAILS[SERIALIZATION_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX] = "Unable to serialize / deserialize configuration.";
        ERROR_DETAILS[SERVICE_LAUNCH_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "Service launch Error";
        ERROR_DETAILS[SERVICE_LAUNCH_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX] = "Error occurred during service launch. Service launch is failed.";
        ERROR_DETAILS[INVALID_REQUEST_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "Invalid Request Error";
        ERROR_DETAILS[INVALID_REQUEST_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX] = "Invalid request received for processing i.e. request validation failed.";
        ERROR_DETAILS[REQUEST_EXECUTION_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "Request Processing Error";
        ERROR_DETAILS[REQUEST_EXECUTION_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX] = "Error occurred while processing input request.";
        ERROR_DETAILS[RESPONSE_GENERATION_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "Response Generation Error";
        ERROR_DETAILS[RESPONSE_GENERATION_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX] = "Unable build a response for the processed request.";
        ERROR_DETAILS[TRANSPORT_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "JMS Error";
        ERROR_DETAILS[TRANSPORT_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX] = "Error occured in transport (JMS).";
        ERROR_DETAILS[CONNECTION_ERROR_ID + INDEX_OFFSET][NAME_DETAIL_INDEX] = "Connection Error";
        ERROR_DETAILS[CONNECTION_ERROR_ID + INDEX_OFFSET][DESC_DETAIL_INDEX]
                = "Error occurred when creating connection or connection is lost during execution.";
    }

    static {
        BeanUtils.registerClassDelegate(ServiceErrorID.class, serviceErrorIDDelegate);
    }

    private int id;

    private ServiceErrorID(int id) {
        this.id = id;
    }

    /**
     * Returns <code>ServiceErrorID</code> for given <code>id</code>
     *
     * @param id value indicate the type of <code>ServiceErrorID</code> to be returned
     * @return <code>ServiceErrorID</code> for given <code>id</code>
     */
    public static ServiceErrorID getServiceErrorId(int id) {
        if (id >= INVALID_CONFIGURATION_ERROR_ID && id <= CONNECTION_ERROR_ID) {
            return ERROR_IDS[id + INDEX_OFFSET];
        }
        return null;
    }

    public static void main(String[] args) throws ESBException {
        ServiceErrorID id = getServiceErrorId(-3);
        String xml = BeanUtils.serialiseBean(id);
        System.out.println(xml);
        System.out.println(BeanUtils.deserialiseBean(xml));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ServiceErrorID)) {
            return false;
        }
        return id == ((ServiceErrorID) obj).id;
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        return String.valueOf(id);
    }

    public int compareTo(Object obj) {
        int other = ((ServiceErrorID) obj).id;
        if (id == other) {
            return 0;
        }
        if (id < other) {
            return -1;
        }
        return 1;
    }

    /**
     * returns the integer value for <code>this ServiceErrorID</code>.
     *
     * @return
     */
    public int getId() {
        return id;
    }

    private Object readResolve() throws ObjectStreamException {
        return ERROR_IDS[id + INDEX_OFFSET];
    }

    /**
     * Returns name for <code>ServiceErrorID</code>
     *
     * @return name
     */
    public String getName() {
        return ERROR_DETAILS[id + INDEX_OFFSET][NAME_DETAIL_INDEX];
    }

    /**
     * Returns description for <code>ServiceErrorID</code>
     *
     * @return description
     */
    public String getDescription() {
        return ERROR_DETAILS[id + INDEX_OFFSET][DESC_DETAIL_INDEX];
    }

    private static class ServiceErrorIDDeletgate extends DefaultPersistenceDelegate {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object[] constructorArgs = new Object[]{new Integer(((ServiceErrorID) oldInstance).getId())};
            return new Expression(oldInstance, ServiceErrorID.class, "getServiceErrorId", constructorArgs);
        }
    }
}
