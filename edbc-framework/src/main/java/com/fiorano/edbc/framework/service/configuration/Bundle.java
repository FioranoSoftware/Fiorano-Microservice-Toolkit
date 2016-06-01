/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.edbc.framework.service.configuration;

/**
 * Resource bundle for configuration package
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public interface Bundle {

    /**
     * @msg.message msg="Could not deserialize the configuration.Reason: {0}."
     */
    public final static String DESERIALIZATION_FAILED = "deserialization_failed";

    /**
     * @msg.message msg="Could not serialize the configuration.Reason: {0}."
     */
    public final static String SERIALIZATION_FAILED = "serialization_failed";

    /**
     * @msg.message msg="unknown serialization done"
     */
    public final static String UNKNOWN_SERIALIZATION = "unknown_serialization";

    /**
     * @msg.message msg="Null error cannot be added"
     */
    public final static String NULL_ERROR_ADDED = "null_error_added";

    /**
     * @msg.message msg="Null error handling action list cannot be added"
     */
    public final static String NULL_ERROR_ACTION_LIST_ADDED = "null_error_action_list_added";

    /**
     * @msg.message msg="Error handling actions map cannot be null"
     */
    public final static String NULL_ERROR_ACTION_MAP = "null_error_action_map";

    //Attributes

    /**
     * @msg.message msg="Error handling configuration"
     */
    public final static String ERROR_HANDLING_CONFIGURATION_NAME = "error_handling_configuration_name";
    /**
     * @msg.message msg="Allows to configure actions to be taken when an exception occurs during the execution."
     */
    public final static String ERROR_HANDLING_CONFIGURATION_DESC = "error_handling_configuration_desc";

    /**
     * @msg.message msg="Validate input"
     */
    public final static String INPUT_VALIDATION_ENABLED_NAME = "input_validation_enabled_name";

    /**
     * @msg.message msg="The service tries to validate the input received if set to true. If this is set to false,
     * service will not validate the input and hence the performance increases. CAUTION: Setting this to false may cause undesired results if the input xml is
     * not valid"
     */
    public final static String INPUT_VALIDATION_ENABLED_DESC = "input_validation_enabled_desc";

    /**
     * @msg.message msg="Store imported schemas"
     */
    public final static String STORE_IMPORTED_SCHEMAS_NAME = "store_imported_schemas_name";
    /**
     * @msg.message msg="Save imported schemas in schema repository"
     */
    public final static String STORE_IMPORTED_SCHEMAS_DESC = "store_imported_schemas_desc";

    /**
     * @msg.message msg="Elements to Encrypt"
     */
    String ELEMS_TO_ENCRYPT_NAME = "elems_to_encrypt_name";
    /**
     * @msg.message msg="Select elements to encrypt in output"
     */
    String ELEMS_TO_ENCRYPT_DESC = "elems_to_encrypt_desc";

    /**
     * @msg.message msg="Elements to Decrypt"
     */
    String ELEMS_TO_DECRYPT_NAME = "elems_to_decrypt_name";
    /**
     * @msg.message msg="Select elements to decrypt in input"
     */
    String ELEMS_TO_DECRYPT_DESC = "elems_to_decrypt_desc";

    /**
     * @msg.message msg="Input Elements to Encrypt/Decrypt"
     */
    String INPUT_ELEMS_TO_ENCRYPT_NAME = "input_elems_to_encrypt_name";
    /**
     * @msg.message msg="Select elements in input XML to encrypt/decrypt"
     */
    String INPUT_ELEMS_TO_ENCRYPT_DESC = "input_elems_to_encrypt_desc";

    /**
     * @msg.message msg="Output Elements to Encrypt/Decrypt"
     */
    String OUTPUT_ELEMS_TO_ENCRYPT_NAME = "output_elems_to_encrypt_name";
    /**
     * @msg.message msg="Select elements in output XML to encrypt/decrypt"
     */
    String OUTPUT_ELEMS_TO_ENCRYPT_DESC = "output_elems_to_encrypt_desc";

    /**
     * @msg.message msg="Encrypt (If enabled, elements in XML message received on inport will be encrypted before
     * sending to server. Else, decrypted.)"
     */
    String INPUT_ENCRYPT = "input_encrypt";

    /**
     * @msg.message msg="Encrypt (If disabled, elements in XML message received from server will be decrypted before
     * sending to outport. Else, encrypted.)"
     */
    String OUTPUT_ENCRYPT = "output_encrypt";

    /**
     * @msg.message msg="Process Message Based On a Property"
     */
    String PROCESS_MESSAGE_NAME = "process_message_name";

    /**
     * @msg.message msg="When enabled, only messages with property configured below will be processed"
     */
    String PROCESS_MESSAGE_DESC = "process_message_desc";

    /**
     * @msg.message msg="Message Property Name"
     */
    String PROCESS_PROPERTY_NAME_NAME = "process_property_name_name";

    /**
     * @msg.message msg="Enabled only when 'Process Message Based On a Property' is enabled.
     * Specify the name of the property based on which messages will be processed"
     */
    String PROCESS_PROPERTY_NAME_DESC = "process_property_name_desc";

    /**
     * @msg.message msg="Message Property Value"
     */
    String PROCESS_PROPERTY_VALUE_NAME = "process_property_value_name";

    /**
     * @msg.message msg="Enabled only when 'Process Message Based On a Property' is enabled.
     * Specify the value of the property based on which messages will be processed"
     */
    String PROCESS_PROPERTY_VALUE_DESC = "process_property_value_desc";

    /**
     * @msg.message msg="Pre Processing XSL Configuration"
     */
    String INPUT_XSL_NAME = "input_xsl_name";

    /**
     * @msg.message msg="Provide XSL Configuration to be applied on input Request before processing"
     */
    String INPUT_XSL_DESC = "input_xsl_desc";

    /**
     * @msg.message msg="Post Processing XSL Configuration"
     */
    String OUTPUT_XSL_NAME = "output_xsl_name";

    /**
     * @msg.message msg="Provide XSL Configuration to be applied on response after processing request"
     */
    String OUTPUT_XSL_DESC = "output_xsl_desc";

    /**
     * @msg.message msg="The file {0} configured for the property  {1}  does not exist in this system. Please make sure the File Path is correct in the system in which peer server is launched."
     */
    public static String WARNING_FILE_NOT_PRESENT_IN_CURRENT_SYSTEM = "warning_file_not_present_in_current_system";


    /**
     * @msg.message msg="The directory {0}  does not exist in this system. Please make sure the File Path is correct in the system in which peer server is launched."
     */
    public static String WARNING_DIRECTORY_NOT_PRESENT_IN_CURRENT_SYSTEM = "warning_directory_not_present_in_current_system";

    /**
     * @msg.message msg="File path cant be empty"
     */
    public static String FILE_PATH_CANT_BE_NULL = "file_path_cant_be_null";


    /**
     * @msg.message msg="Directory path cant be empty"
     */
    public static String DIRECTORY_PATH_CANT_BE_NULL = "directory_path_cant_be_null";

    /**
     * @msg.message msg="Threadpool configuration"
     */

    public String THREADPOOL_CONFIGURATION = "threadpool_configuration";

    /**
     * @msg.message msg="Threadpool configuration"
     */

    public String THREADPOOL_CONFIGURATION_DESC = "threadpool_configuration_desc";

    /**
     * @msg.message msg="Connection Pool Configuration"
     */
    String CONN_POOL_CONFIG_NAME = "conn_pool_config_name";
    /**
     * @msg.message msg="Connection Pool Configuration"
     */
    String CONN_POOL_CONFIG_DESC = "conn_pool_config_desc";
}
