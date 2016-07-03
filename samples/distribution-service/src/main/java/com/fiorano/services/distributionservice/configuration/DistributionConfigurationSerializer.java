/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.configuration;

import com.fiorano.edbc.framework.service.configuration.Bundle;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.internal.configuration.IConfigurationSerializer;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.StringUtil;
import fiorano.tifosi.util.xmlutils.XMLBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class DistributionConfigurationSerializer implements IConfigurationSerializer<DistributionServicePM> {
    private static final String DATA = "Data";
    private static final String MODEL = "Model";

    private static void addDataElement(int value, XMLBuilder doc) {
        doc.addElement(DATA, null, String.valueOf(value), false);
    }

    private static int readInt(List<Integer> modelAsList) {
        return modelAsList.isEmpty() ? -1 : modelAsList.remove(0);
    }

    public DistributionServicePM deserializeFromString(String stringRepresentation) throws ServiceConfigurationException {
        List<Integer> modelAsQueue = new ArrayList<>();
        if (!StringUtil.isEmpty(stringRepresentation)) {
            try {
                SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new StringReader(stringRepresentation)),
                        new PortInfoHandler(modelAsQueue));
            } catch (Exception ex) {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.DESERIALIZATION_FAILED, new Object[]{ex.getMessage()}), ex,
                        ServiceErrorID.SERIALIZATION_ERROR);
            }
        }
        int portCount = readInt(modelAsQueue);
        int[] weightsOfPorts = new int[portCount];
        for (int i = 0; i < portCount; ++i) {
            weightsOfPorts[i] = readInt(modelAsQueue);
        }

        boolean isPropagateSchema = (readInt(modelAsQueue) == 0);

        DistributionServicePM distributionServicePM = new DistributionServicePM();
        distributionServicePM.setPortCount(portCount);
        distributionServicePM.setPortWeights(weightsOfPorts);
        distributionServicePM.setPropagateSchema(isPropagateSchema);
        return distributionServicePM;
    }

    public String serializeToString(DistributionServicePM configuration) {
        XMLBuilder doc = new XMLBuilder();
        doc.startDocument(null, null);
        doc.startElement(MODEL);
        int portCount = configuration.getPortCount();
        int[] weightsOfPorts = configuration.getPortWeights();
        addDataElement(portCount, doc);
        for (int i = 0; i < portCount; ++i) {
            addDataElement(weightsOfPorts[i], doc);
        }

        addDataElement(configuration.isPropagateSchema() ? 0 : 1, doc);

        doc.endElement(MODEL);
        doc.endDocument();

        return doc.getXMLDocument();
    }

    private static class PortInfoHandler extends DefaultHandler {
        private StringBuffer content = new StringBuffer();
        private List<Integer> modelAsQueue;

        private PortInfoHandler(List<Integer> modelAsQueue) {
            this.modelAsQueue = modelAsQueue;
        }

        public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) {
            content.setLength(0);
        }

        public void characters(char[] buf, int offset, int len) {
            content.append(buf, offset, len);
        }

        public void endElement(String namespaceURI, String sName, String qName) {
            if (DATA.equals(qName)) {
                modelAsQueue.add(Integer.valueOf(content.toString()));
            }
        }
    }

}
