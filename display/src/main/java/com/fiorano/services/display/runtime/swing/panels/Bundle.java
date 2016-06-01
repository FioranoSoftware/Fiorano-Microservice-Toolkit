/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.display.runtime.swing.panels;

/**
 * Created by IntelliJ IDEA.
 * User: chaitanya
 * Date: Jan 31, 2008
 * Time: 11:39:12 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Bundle {

     /**
     * @msg.message msg="Failed to create BytesMessagePanel"
     */
    public final static String ERROR_CREATING_BYTES_MESSAGE_PANEL = "error_creating_bytes_message_panel";
    /**
     * @msg.message msg="Unable to create the document panel"
     */
    public final static String ERROR_CREATING_DOC_PANEL = "error_creating_doc_panel";
    /**
     * @msg.message msg="Failed to save message selected"
     */
    public final static String ERROR_SAVING_MESSAGE_DOC_BODY = "error_saving_message_doc_body";
    /**
     * @msg.message msg="Failed to create textMessagePanel"
     */
    public final static String ERROR_CREATING_TEXT_MESSAGE_PANEL = "error_creating_text_message_panel";
    /**
     * @msg.message msg="Unable to load html/xml view in view"
     */
    public final static String ERROR_CHANGING_VIEW_TEXT_MESSAGE_PANEL = "error_changing_view_text_message_panel";
    /**
     * @msg.message msg="Failed to get bytes from bytesEditor while saving TextMessage"
     */
    public final static String ERROR_SAVING_TEXT_MESSAGE = "error_saving_text_message";
    /**
     * @msg.message msg="Failed to create ObjectMessagePanel"
     */
    public final static String ERROR_CREATING_OBJECT_MESSAGE_PANEL = "error_creating_object_message_panel";
    /**
     * @msg.message msg="Failed to create MapMessagePanel"
     */
    public final static String ERROR_CREATING_MAP_MESSAGE_PANEL = "error_creating_map_message_panel";
    /**
     * @msg.message msg="Unable to read MapMessage in correct format"
     */
    public final static String ERROR_MESSAGE_FORMAT_LOAD_MAP_MESSAGE_PANEL = "error_message_format_load_map_message_panel";
    /**
     * @msg.message msg="Unable to read MapMessage in correct format"
     */
    public final static String ERROR_MESSAGE_FORMAT_SAVE_MAP_MESSAGE_PANEL = "error_message_format_save_map_message_panel";
    /**
     * @msg.message msg="Failed to create streamMessagePanel"
     */
    public final static String ERROR_CREATING_STREAM_MESSAGE_PANEL = "error_creating_stream_message_panel";
    /**
     * @msg.message msg="Text for message type prefix label"
     */
    public final static String  MESSAGE_TYPE_PREFIX_LABEL_TEXT = "Message type :";
    /**
     * @msg.message msg="Unable to clear document body"
     */
    public final static String ERROR_CLEARING_DOC_BODY = "error_clearing_doc_body";
    /**
     * @msg.message msg="Unable to load document body"
     */
    public final static String ERROR_LOADING_DOC_BODY = "error_loading_doc_body";
    /**
     * @msg.message msg="Message not selected"
     */
    public final static Object WARNING_NO_MESSAGE_SELECTED = "Please select message first!";
    /**
     * @msg.message msg="Unable to load document panel"
     */
    public final static String ERROR_LOADING_DOC_PANEL = "error_loading_doc_panel";
   
}