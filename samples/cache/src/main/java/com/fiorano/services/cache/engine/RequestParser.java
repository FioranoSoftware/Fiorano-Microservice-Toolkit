/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.cache.engine;

import com.fiorano.services.common.util.RBUtil;
import com.fiorano.xml.sax.SAXUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;

/**
 * parses the request.
 *
 * @author FSTPL
 */
public class RequestParser {

    RequestHandler handler;

    public RequestParser(RequestHandler handler) {
        if (handler == null) {
            throw new IllegalStateException(RBUtil.getMessage(Bundle.class, Bundle.NULL_HANDLER));
        }
        this.handler = handler;
    }

    /**
     * parses the input request and returns the list of entries
     *
     * @param inputSource the source containing the xml input to parse
     * @throws CacheException if parser creation fails or parsing fails
     */
    public void parseRequest(InputSource inputSource) throws CacheException {
        SAXParser parser;
        try {
            parser = SAXUtil.createSAXParserFactory(true, true, false).newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.PARSER_CREATION_FAILED), e);
        } catch (SAXException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.PARSER_CREATION_FAILED), e);
        }
        try {
            parser.parse(inputSource, handler);
        } catch (SAXException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.PARSE_FAILED), e);
        } catch (IOException e) {
            throw new CacheException(RBUtil.getMessage(Bundle.class, Bundle.PARSE_FAILED), e);
        }
    }
}
