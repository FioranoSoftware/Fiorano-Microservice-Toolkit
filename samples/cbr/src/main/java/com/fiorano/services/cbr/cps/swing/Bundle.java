/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.cbr.cps.swing;

/**
 * This class contains the messages
 * used by the service in logging or events
 *
 * @author Venkat
 * @author Deepthi
 * @version 1.6, 19 January 2010
 * @created Wed, 31 Jan 2007
 */

public interface Bundle {

    /**
     * @msg.message msg="Error in schema validation. Reason: {0}"
     */
    String SCHEMA_VALIDATION_ERROR = "schema_validation_error";

    /**
     * @msg.message msg="Schema Validation Failed. Reason: {0}"
     */
    String SCHEMA_VALIDATION_FAILED = "schema_validation_failed";

    /**
     * @msg.message msg="Atleast one XPath must be specified"
     */
    String ATLEAST_ONE_XPATH = "atleast_one_xpath";

    /**
     * @msg.message msg="Invalid port details"
     */
    String INVALID_PORT_DETAILS = "invalid_port_details";

    /**
     * @msg.message msg="Port number {0} : {1} is a reserved port name."
     */
    String RESERVED_PORT = "reserved_port";

    /**
     * @msg.message msg="DUPLICATE PORT NAME"
     */
    String DUPLICATE_PORT = "duplicate_port";

    /**
     * @msg.message msg="Port number {0} has the same name ( {1} ) as port number {2}"
     */
    String SAME_NAME = "same_name";

    /**
     * @msg.message msg="PortName Missing"
     */
    String NAME_MISSING = "name_missing";

    /**
     * @msg.message msg="PortName is not specifed at Row : {0}"
     */
    String NO_PORT_NAME = "no_port_name";

    /**
     * @msg.message msg="Invalid PortName specifed at Row : {0} PortName cannot have these characters !,@,#,%.__,...and use of '_' at the begining and end is 		restricted"
     */
    String INVALID_PORT_NAME = "invalid_port_name";

    /**
     * @msg.message msg="XPATH is not specified for port : {0}"
     */
    String NO_XPATH = "no_xpath";

    /**
     * @msg.message msg="Invalid XPath Provided"
     */
    String INVALID_XPATH = "invalid_xpath";

    /**
     * @msg.message msg="Invalid XPath provided in Row No : {0}"
     */
    String INVALID_XPATH_ROW = "invalid_xpath_row";

    /**
     * @msg.message msg="Error saving configuration & setting port details"
     */
    String ERROR_SAVING_CONFIG = "error_saving_config";

    /**
     * @msg.message msg="Exception while creating XPath."
     */
    String EXCEP_CREATING_XPATH = "excep_creating_xpath";

    /**
     * @msg.message msg="Error while creating XPath Editor"
     */
    String ERROR_CREATING_XPATH = "error_creating_xpath";

    /**
     * @msg.message msg="Error creating namespace panel"
     */
    String ERROR_CREATING_NAMESPACE = "error_creating_namespace";

    /**
     * @msg.message msg="DUPLICATE_PREFIX"
     */
    String DUPLICATE_PREFIX = "duplicate_prefix";

    /**
     * @msg.message msg="DUPLICATE_NAMESPACE"
     */
    String DUPLICATE_NAMESPACE = "duplicate_namespace";

    /**
     * @msg.message msg="Prefix {0} is used more than once"
     */
    String DUPLICATE_PREFIX_DESC = "duplicate_prefix_desc";

    /**
     * @msg.message msg="Namespace {0} is used more than once"
     */
    String DUPLICATE_NAMESPACE_DESC = "duplicate_namespace_desc";

    /**
     * @msg.message msg="Error adding namespaces"
     */
    String ERROR_ADDING = "error_adding";

    /**
     * @msg.message msg="DTD schema is not supported"
     */
    String DTD_NOT_SUPPORTED = "dtd_not_supported";

    /**
     * @msg.message msg="Delete"
     */
    String DELETE = "delete";

    /**
     * @msg.message msg="Delete All"
     */
    String DELETE_ALL = "delete_all";

    /**
     * @msg.message msg="Use XPath 1.0"
     */
    String USE_XPATH_10 = "use_xpath_10";

    /**
     * @msg.message msg="Apply XPath on context"
     */
    String APPLY_ON_CTXT = "apply_on_ctxt";

    /**
     * @msg.message msg="Routing rules"
     */
    String ROUTING_RULES = "routing_rules";

    /**
     * @msg.message msg="Processing Configuration"
     */
    String PROCESSING_CONFIG = "processing_config";

    /**
     * @msg.message msg="Processor"
     */
    String PROCESSOR = "processor";
    /**
     * @msg.message msg="Port Name"
     */
    String PORT_NAME = "port_name";

    /**
     * @msg.message msg="Cancel"
     */
    String CANCEL = "cancel";

    /**
     * @msg.message msg="XPath Editor"
     */
    String XPATH_EDITOR = "xpath_editor";

    /**
     * @msg.message msg="Namespaces"
     */
    String NAMESPACES = "namespaces";

    /**
     * @msg.message msg="Prefix"
     */
    String PREFIX = "prefix";

    /**
     * @msg.message msg="Unable to load Encrypt/Decrypt Configuration Panel "
     */
    String LOAD_ENCRYPT_PANEL_FAIL = "load_encrypt_panel_fail";

    /**
     * @msg.message msg="Enable Thread pool"
     */
    String ENABLE_THREAD_POOL = "enable_thread_pool";

    /**
     * @msg.message msg="Pool Size"
     */
    String POOL_SIZE = "pool_size";


    /**
     * @msg.message msg="Batch Eviction Interval"
     */
    String BATCH_EVICTION_INTERVAL = "batch_eviction_interval";

}
