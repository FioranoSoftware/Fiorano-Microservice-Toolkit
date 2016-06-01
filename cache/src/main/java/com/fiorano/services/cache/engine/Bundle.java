/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine;

/**
 * Created by IntelliJ IDEA.
 * Date: Jan 25, 2007
 * Time: 5:09:33 PM
 *
 * @author
 */
public interface Bundle {
    /**
     * @msg.message msg="Unable to create parser to parse the request"
     */
    String PARSER_CREATION_FAILED = "parser_creation_failed";

    /**
     * @msg.message msg="Handler to handle sax events generated while parsing is null."
     */
    String NULL_HANDLER = "null_handler";

    /**
     * @msg.message msg="Unable to parse the request"
     */
    String PARSE_FAILED = "parse_failed";

    /**
     * @msg.message msg="Cannot lookup value for key {0}"
     */
    String CANNOT_LOOKUP = "cannot_lookup";

    /**
     * @msg.message msg="Request message does not confirm to the schema provided"
     */
    String INVALID_REQUEST = "invalid_request";

    /**
     * @msg.message msg="Failed to create XMLWriter which constructs xml messages"
     */
    String XML_WRITER_CREATION_FAILED = "xml_writer_creation_failed";

    /**
     * @msg.message msg="Failed to create output XML"
     */
    String XML_CREATION_FAILED = "xml_creation_failed";

    /**
     * @msg.message msg="prefix mapping failed"
     */
    String PREFIX_MAP_FAILED = "prefix_map_failed";

    /**
     * @msg.message msg="failed to add Cache Entry to response XML. Cache Entry: {0}, Reason: {1}"
     */
    String ADD_ENTRY_TO_RESPONSE_FAILED = "add_entry_to_response_failed";

    /**
     * @msg.message msg="Cannot add entries to response XML as ResponseBuilder is not initialized"
     */
    String BUILDER_NOT_INITIALIZED = "builder_not_initialized";

    /**
     * @msg.message msg="entry contains data without keys ignoring"
     */
    String NO_KEYS_SKIP_DATA = "no_keys_skip_data";

    /**
     * @msg.message msg="Could not process the entry. Entry: {0}"
     */
    String FAILED_TO_PROCESS_ENTRY = "failed_to_process_entry";

    /**
     * @msg.message msg="Failed to fetch all entries from storage"
     */
    String FAILED_TO_FETCH_ENTRIES = "failed_to_fetch_entries";

    /**
     * @msg.message msg="Start time: {0}"
     */
    String START_TIME = "start_time";

    /**
     * @msg.message msg="End time: {0}"
     */
    String END_TIME = "end_time";

    /**
     * @msg.message msg="Could not create event publisher. User events will not be sent"
     */
    String FAILED_TO_CREATE_EVENT_PUBLISHER = "failed_to_create_event_publisher";

    /**
     * @msg.message msg="Unable to clear the cache. Reason: {0}"
     */
    String CACHE_CLEAR_FAILED = "cache_clear_failed";

    /**
     * @msg.message msg="Add / Update - Entry : {0}"
     */
    String UPDATE_CACHE_ENTRY = "update_cache_entry";

    /**
     * @msg.message msg="Lookup - Key: {0} Entry: {1}"
     */
    String LOOKUP_CACHE_ENTRY = "lookup_cache_entry";

    /**
     * @msg.message msg="Delete - Entry : {0}"
     */
    String DELETE_CACHE_ENTRY = "delete_cache_entry";

    /**
     * @msg.message msg="Deleted all entries"
     */
    String DELETE_CACHE = "delete_cache";
}
