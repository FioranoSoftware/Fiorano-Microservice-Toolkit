/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.cache.configuration;

/**
 * Bundle interface for configuration
 *
 * @author FSTPL
 */
public interface Bundle {

    /**
     * @msg.message msg="Object of type {0} is not supported for value of Field"
     */
    String INVALID_FIELD_VALUE_CLASS = "invalid_field_value_class";

    /**
     * @msg.message msg="Expects Object of type FieldDefinition, found {0}"
     */
    String INVALID_FIELD_DEFINITION = "invalid_field_definition";

    /**
     * @msg.message msg="Cannot remove field definition using iterator's remove method"
     */
    String FIELD_DEFS_ITER_DOES_NOT_SUPPORT_REMOVE = "field_defs_iter_does_not_support_remove";

    /**
     * @msg.message msg="Field name cannot be null"
     */
    String NULL_FIELD_NAME = "null_field_name";
    /**
     * @msg.message msg="Input cannot be Empty"
     */

    String EMPTY_INPUT = "empty_input";

    /**
     * @msg.message msg="No fields defined"
     */
    String NO_FIELDS = "no_fields";

    /**
     * @msg.message msg="No key fields defined. Atleast one key field must be defined"
     */
    String NO_KEY_FIELDS = "no_key_fields";

    /**
     * @msg.message msg="No data fields defined. Atleast one data field must be defined"
     */
    String NO_DATA_FIELDS = "no_data_fields";

    /**
     * @msg.message msg="Insufficient number of fields defined"
     */
    String INSUFFICIENT_FIELDS = "insufficient_fields";

    /**
     * @msg.message msg="service configuration is invalid"
     */
    String INVALID_CONFIGURATION = "invalid_configuration";

    /**
     * @msg.message msg="Could not serialize service configuration. Reason: {0}"
     */
    String SERIALIZATION_FAILED = "serialization_failed";

    /**
     * @msg.message msg="Could not deserialize servce confinguration. Reason: {0}"
     */
    String DESERIALIZATION_FAILED = "deserialization_failed";

    /**
     * @msg.message msg="Threshold size should either be -1 or be greater than 0. Current value {0}"
     */
    String INVALID_THRESHOLD_SIZE = "invalid_threshold_size";

    /**
     * @msg.message msg="Entry removal criteria is invalid. Found {0}"
     */
    String INVALID_ENTRY_REMOVAL_CRITERIA = "invalid_entry_removal_criteria";

    /**
     * @msg.message msg="Initial capicity should be greater than 0 and less than threshold size. Found {0}"
     */
    String INVALID_CAPACITY_VALUE = "invalid_capacity_value";

    //Attributes

    /**
     * @msg.message msg="Field Definition Table"
     */
    String FIELD_DEFINITIONS_NAME = "field_definitions_name";
    /**
     * @msg.message msg="The different key and data fields which is part of each cache entry"
     */
    String FIELD_DEFINITIONS_DESC = "field_definitions_desc";

    /**
     * @msg.message msg="Cache threshold"
     */
    String THRESHOLD_SIZE_NAME = "threshold_size_name";
    /**
     * @msg.message msg="The threshold limit of the cache before older entries are lost"
     */
    String THRESHOLD_SIZE_DESC = "threshold_size_desc";

    /**
     * @msg.message msg="Criteria to remove entry"
     */
    String ENTRY_REMOVAL_CRITERIA_NAME = "entry_removal_criteria_name";
    /**
     * @msg.message msg="When a threshold is defined this property determines which entry should be removed after the threshold is reached.
     * Least recently added - The entry which is added first will be removed
     * Least recently updated - The enrty which is added first and never updated or if all entries are updated then the least recently updated entry will be removed
     * Least recently accessed = The entry which is least recently added or updated or looked up will be removed"
     */
    String ENTRY_REMOVAL_CRITERIA_DESC = "entry_removal_criteria_desc";

    /**
     * @msg.message msg="Initial cache size"
     */
    String INITIAL_CAPACITY_NAME = "initial_capacity_name";
    /**
     * @msg.message msg="Approximate /Initial size with which Cache storage has to be initialized. the storage will be resized
     * depending on the underlying implementation, providing an appropriate will minimize the resizes"
     */
    String INITIAL_CAPACITY_DESC = "initial_capacity_desc";

    /**
     * @msg.message msg="Root Element not selected for the structure"
     */
    String NO_ROOT_ELEMENT = "no_root_element";
}
