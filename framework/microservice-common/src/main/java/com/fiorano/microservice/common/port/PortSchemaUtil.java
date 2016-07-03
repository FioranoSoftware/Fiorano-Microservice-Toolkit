/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.microservice.common.port;

import com.fiorano.openesb.application.application.PortInstance;
import com.fiorano.openesb.application.service.Schema;
import com.fiorano.xml.ClarkName;
import fiorano.esb.record.ESBRecordDefinition;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class used in PortInstanceAdapter to extract fiorano.tifosi.dmi.service.Schema from PortInstance as fiorano.esb.record.ESBRecordDefinition
 * and to convert fiorano.esb.record.ESBRecordDefinition to fiorano.tifosi.dmi.service.Schema and set on PortInstance.
 *
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
final class PortSchemaUtil {

    /**
     * Returns the schema set on <code>portInstance</code>
     *
     * @param portInstance port from which schema has to be retrieved
     * @return retrieved schema from <code>portInstance</code>
     */
    static ESBRecordDefinition getPortSchema(PortInstance portInstance) {
        if (portInstance == null) {
            throw new IllegalArgumentException("cannot fetch schema from null port instance");
        }
        Schema schema = portInstance.getSchema();
        if (schema == null) {
            return null;
        }
        ESBRecordDefinition schemaDefinition = new ESBRecordDefinition();
        schemaDefinition.setStructure(schema.getContent());
        schemaDefinition.setRootElementName(ClarkName.getLocalName(schema.getRootElement()));
        schemaDefinition.setTargetNamespace(ClarkName.getNamespace(schema.getRootElement()));
        Map importedSchemas = schema.getSchemaRefs();
        if (importedSchemas != null) {
            Set properties = importedSchemas.entrySet();
            Iterator iterator;
            if (properties != null && (iterator = properties.iterator()) != null) {
                while (iterator.hasNext()) {
                    Map.Entry property = (Map.Entry) iterator.next();
                    Object name = property.getKey();
                    Object value = property.getValue();
                    if (name instanceof String && value instanceof String) {
                        schemaDefinition.addImportedStructure((String) name, (String) value);
                    }
                }
            }
        }
        schemaDefinition.setDefinitionType(convertSchemaTypeToESBRecordDefType(schema.getType()));

        return schemaDefinition;
    }

    private static int convertSchemaTypeToESBRecordDefType(int schemaType) {
        switch (schemaType) {
            case Schema.TYPE_DTD:
                return ESBRecordDefinition.DTD;
            case Schema.TYPE_XSD:
                return ESBRecordDefinition.XSD;
            default:
                return ESBRecordDefinition.NONE;
        }
    }

    /**
     * Sets the schema provided by <code>schemaDefinition</code> on port represneted by <code>portInstance</code>
     *
     * @param portInstance port on which schema has to be set
     * @param schemaDefinition schema that has to be set on the port represneted by <code>portInstance
     */
    static void setPortSchema(PortInstance portInstance, ESBRecordDefinition schemaDefinition) {
        if (portInstance == null) {
            throw new IllegalArgumentException("cannot set schema to null port instance");
        }
        if (schemaDefinition == null || schemaDefinition.getStructure() == null) {
            portInstance.setSchema(null);
            return;
        }
        Schema schema = new Schema();
        schema.setContent(schemaDefinition.getStructure());
        schema.setRootElement(ClarkName.toClarkName(schemaDefinition.getTargetNamespace(), schemaDefinition.getRootElementName()));
        Map importedSchemas = schemaDefinition.getImportedStructures();
        if (importedSchemas != null) {
            Set properties = importedSchemas.entrySet();
            Iterator iterator;
            if (properties != null && (iterator = properties.iterator()) != null) {
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    Object key = entry.getKey();
                    if (!(key instanceof String)) {
                        continue;
                    }
                    Object value = entry.getValue();
                    if (value instanceof List) {
                        schema.addSchemaRef((String) key, (String) ((List) value).get(0));
                    } else if (value instanceof String[]) {
                        schema.addSchemaRef((String) key, ((String[]) value)[0]);
                    } else if (value instanceof String) {
                        schema.addSchemaRef((String) key, (String) value);
                    }

                }
            }
        }
        schema.setType(convertESBRecordDefTypeToSchemaType(schemaDefinition.getDefinitionType()));
        portInstance.setSchema(schema);
    }

    private static int convertESBRecordDefTypeToSchemaType(int recordDefinitionType) {
        switch (recordDefinitionType) {
            case ESBRecordDefinition.DTD:
                return Schema.TYPE_DTD;
            case ESBRecordDefinition.XSD:
            default:
                return Schema.TYPE_XSD;
        }
    }

}
