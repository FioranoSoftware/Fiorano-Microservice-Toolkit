/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xslt.configuration;

import com.fiorano.edbc.framework.service.configuration.AbstractErrorHandlingConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.RetryAction;
import com.fiorano.edbc.framework.service.exception.RetryConfiguration;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.stax.FioranoStaxParser;
import fiorano.esb.record.ESBRecordDefinition;

import javax.xml.stream.XMLStreamException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Phani
 * Date: Jun 29, 2009
 * Time: 11:50:55 AM
 * To change this template use File | Settings | File Templates.
 */
class ConfigurationConvertor {
    public final int ISPEC_TYPE = 1;
    public final int MCF_TYPE = 2;
    private XsltPM xsltPM;

    public boolean isOldConfiguration(String configuration) throws XMLStreamException {
        FioranoStaxParser cursor = new FioranoStaxParser(new StringReader(configuration));
        boolean oldConfiguration = cursor.markCursor("TrGatewayConfigurations");
        cursor.close();
        return oldConfiguration;
    }

    public XsltPM convert(String oldSerializedConfiguration) throws XMLStreamException {
        FioranoStaxParser cursor = new FioranoStaxParser(new StringReader(oldSerializedConfiguration));
        return parseBCConfigurations(cursor);
    }

    private XsltPM parseBCConfigurations(FioranoStaxParser cursor) throws XMLStreamException {
        xsltPM = new XsltPM();
        if (cursor.markCursor("BCConfigurations")) {
            FioranoStaxParser newCursor = new FioranoStaxParser(new StringReader(cursor.getCData()));
            cursor.resetCursor();
            parseJCAConfigurations(newCursor);
        }
        if (cursor.markCursor("ErrorConfigurations")) {
            try {
                FioranoStaxParser newCursor = new FioranoStaxParser(new StringReader(cursor.getCData()));
                parseErrorConfigurations(newCursor);
            } catch (Exception e) {
                //ignore. CPS should launch even if some problem in error handling configuration backward compatibility.
            }
        }
        cursor.close();
        return xsltPM;
    }

    private void parseJCAConfigurations(FioranoStaxParser cursor) throws XMLStreamException {
        if (cursor.markCursor("string")) {
            String s = cursor.getElementContent("string");
            FioranoStaxParser newCursor = new FioranoStaxParser(new StringReader(s));
            cursor.close();
            parseConfigurationObjects(newCursor);
        }
    }

    private void parseConfigurationObjects(FioranoStaxParser cursor) throws XMLStreamException {
        if (cursor.markCursor("ConfigurationObjects")) {
            while (cursor.nextElement()) {
                if ("ConfigurationObject".equalsIgnoreCase(cursor.getLocalName())) {
                    if (cursor.markCursor("ConfigurationObject")) {
                        String s = cursor.getCData();
                        FioranoStaxParser newCursor = new FioranoStaxParser(new StringReader(s));
                        parseConfiguration(newCursor);
                        newCursor.close();
                        cursor.resetCursor();
                    }
                }
            }
        }
    }

    private void parseConfiguration(FioranoStaxParser cursor) throws XMLStreamException {
        if (cursor.markCursor("object")) {
            int objectType = -1;
            while (cursor.nextElement()) {
                if ("void".equalsIgnoreCase(cursor.getLocalName())) {
                    if (cursor.markCursor("void")) {
                        String propertyValue = cursor.getAttributeValue(null, "property");
                        if ("beanClassName".equalsIgnoreCase(propertyValue)) {
                            objectType = getConfigurationObjectType(cursor, objectType);
                            if (objectType == -1) {
                                cursor.close();
                                return;
                            }
                        } else if ("beanObject".equalsIgnoreCase(propertyValue)) {
                            if (objectType == ISPEC_TYPE) {
                                parseISpec(cursor);
                            } else if (objectType == MCF_TYPE) {
                                //parseMCF(cursor);
                            }
                        }
                        cursor.resetCursor();
                    }
                }
            }
            cursor.resetCursor();
        }
        cursor.close();
    }

    private void parseISpec(FioranoStaxParser cursor) throws XMLStreamException {
        while (cursor.nextElement()) {
            if (cursor.markCursor("void")) {
                String attrValue = cursor.getAttributeValue(null, "property");
                if ("XSL".equalsIgnoreCase(attrValue)) {
                    xsltPM.setXSL(getValue(cursor));
                } else if ("JMSMessageXSL".equalsIgnoreCase(attrValue)) {
                    xsltPM.setJMSMessageXSL(getValue(cursor));
                } else if ("project".equalsIgnoreCase(attrValue)) {
                    xsltPM.setProject(getValue(cursor));
                } else if ("doOptimization".equalsIgnoreCase(attrValue)) {
                    String bValue = getBooleanText(cursor);
                    if (bValue != null)
                        xsltPM.setDoOptimization(Boolean.valueOf(bValue));
                } else if ("xsltEngine".equalsIgnoreCase(attrValue)) {
                    xsltPM.setXsltEngine(getValue(cursor));
                } else if ("inputStructures".equalsIgnoreCase(attrValue)) {
                    xsltPM.setInputStructures(getValue(cursor));
                } else if ("outputStructure".equalsIgnoreCase(attrValue)) {
                    xsltPM.setOutputStructure(getValue(cursor));
                } else if ("XSLInput".equalsIgnoreCase(attrValue)) {
                    String value = getValue(cursor);
                    xsltPM.setXSLInput(value);
                } else if ("failOnErrors".equalsIgnoreCase(attrValue)) {
                    String bValue = getBooleanText(cursor);
                    if (bValue != null)
                        xsltPM.setFailOnErrors(Boolean.valueOf(bValue));
                } else if ("stripWhiteSpaces".equalsIgnoreCase(attrValue)) {
                    xsltPM.setStripWhiteSpaces(getValue(cursor));
                } else if ("tfClassName".equalsIgnoreCase(attrValue)) {
                    xsltPM.setTfClassName(getValue(cursor));
                } else if ("esbDefInPort".equalsIgnoreCase(attrValue)) {
                    ESBRecordDefinition recordDef = getESBRecord(cursor);
                    xsltPM.setEsbDefInPort(recordDef);
                } else if ("esbDefOutPort".equalsIgnoreCase(attrValue)) {
                    ESBRecordDefinition recordDef = getESBRecord(cursor);
                    xsltPM.setEsbDefOutPort(recordDef);
                }
                cursor.resetCursor();
            }
        }
    }

    private String getBooleanText(FioranoStaxParser cursor) throws XMLStreamException {
        String text = null;
        if (cursor.markCursor("boolean")) {
            text = cursor.getTextContent();
            cursor.resetCursor();
        }
        return text;
    }

    private String getInt(FioranoStaxParser cursor) throws XMLStreamException {
        String text = null;
        if (cursor.markCursor("int")) {
            text = cursor.getTextContent();
            cursor.resetCursor();
        }
        return text;
    }

    private String getLong(FioranoStaxParser cursor) throws XMLStreamException {
        String text = null;
        if (cursor.markCursor("long")) {
            text = cursor.getTextContent();
            cursor.resetCursor();
        }
        return text;
    }

    private ESBRecordDefinition getESBRecord(FioranoStaxParser cursor) throws XMLStreamException {
        ESBRecordDefinition recordDef = new ESBRecordDefinition();
        if (cursor.markCursor("void")) {
            while (cursor.nextElement()) {
                if (cursor.markCursor("object")) {
                    while (cursor.nextElement()) {
                        if (cursor.markCursor("void")) {
                            String attributeValue = cursor.getAttributeValue(null, "property");
                            if ("importedStructures".equals(attributeValue)) {
                                recordDef.setImportedStructures(getImportedStructures(cursor));
                            } else if ("definitionType".equals(attributeValue)) {
                                String intValue = getInt(cursor);
                                if (intValue != null)
                                    recordDef.setDefinitionType(Integer.valueOf(intValue));
                            } else if ("rootElementName".equalsIgnoreCase(attributeValue)) {
                                recordDef.setRootElementName(getValue(cursor));
                            } else if ("structure".equalsIgnoreCase(attributeValue)) {
                                recordDef.setStructure(getValue(cursor));
                            } else if ("targetNameSpace".equalsIgnoreCase(attributeValue)) {
                                recordDef.setTargetNamespace(getValue(cursor));
                            }
                            cursor.resetCursor();
                        }
                    }
                    cursor.resetCursor();
                }
            }
            cursor.resetCursor();
        }
        return recordDef;
    }

    private Map<String, String> getImportedStructures(FioranoStaxParser cursor) throws XMLStreamException {
        Map<String, String> importedStructures = new Hashtable<String, String>();
        if (cursor.markCursor("void")) {
            while (cursor.nextElement()) {
                if (cursor.markCursor("object")) {
                    while (cursor.nextElement()) {
                        if (cursor.markCursor("void")) {
                            String attributeValue = cursor.getAttributeValue(null, "method");
                            if ("put".equalsIgnoreCase(attributeValue)) {
                                String nameSpace = getValue(cursor);
                                String schema = getValue(cursor);
                                if (!StringUtil.isEmpty(nameSpace) && !StringUtil.isEmpty(schema)) {
                                    importedStructures.put(nameSpace, schema);
                                }
                            }
                            cursor.resetCursor();
                        }
                    }
                    cursor.resetCursor();
                }
            }
            cursor.resetCursor();
        }
        return importedStructures;
    }

    private String getValue(FioranoStaxParser cursor) throws XMLStreamException {
        String value = null;
        if (cursor.markCursor("string")) {
            value = cursor.getTextContent();
            cursor.resetCursor();
        }
        return value;
    }

    private int getConfigurationObjectType(FioranoStaxParser cursor, int objectType) throws XMLStreamException {
        String panelName;
        if (cursor.markCursor("class")) {
            panelName = cursor.getTextContent();
            if (panelName.endsWith("ManagedConnectionFactory")) {
                objectType = MCF_TYPE;
            } else if (panelName.endsWith("InteractionSpec")) {
                objectType = ISPEC_TYPE;
            } else {
                objectType = -1;
            }
            cursor.resetCursor();
        }
        return objectType;
    }

    private int getErrorID(int errorID) {

        if (errorID == 0) {         //fiorano.esb.adapter.jca.error.ErrorAction.RECONNECT
            return ErrorHandlingAction.RECONNECT;
        } else if (errorID == 1) {  //fiorano.esb.adapter.jca.error.ErrorAction.RETRY_EXECUTION
            return ErrorHandlingAction.RETRY_EXECUTION;
        } else if (errorID == 2) {  //fiorano.esb.adapter.jca.error.ErrorAction.CONTINUE_REQUEST_EXECUTION
            return ErrorHandlingAction.STOP_SERVICE;
        } else if (errorID == 3) {  //fiorano.esb.adapter.jca.error.ErrorAction.THROW_ERROR_ON_WARNING
            return ErrorHandlingAction.THROW_ERROR_ON_WARNING;
        } else if (errorID == 11) { //com.fiorano.bc.trgateway.model.dmi.error.XErrorAction.SEND_ON_ERROR_PORT
            return ErrorHandlingAction.SEND_TO_ERROR_PORT;
        } else if (errorID == 12) { //com.fiorano.bc.trgateway.model.dmi.error.XErrorAction.STOP_SERVICE
            return ErrorHandlingAction.STOP_SERVICE;
        }
        return -1;
    }

    private RetryAction getRetryAction(FioranoStaxParser cursor) throws XMLStreamException {

        RetryConfiguration retryConfiguration = new RetryConfiguration();
        RetryAction retryAction = new RetryAction();

        if (cursor.markCursor("object")) {
            while (cursor.nextElement()) {
                if ("void".equals(cursor.getLocalName())) {
                    String propertyName = cursor.getAttributeValue(null, "property");
                    if ("active".equals(propertyName)) {
                        retryAction.setEnabled(Boolean.valueOf(getBooleanText(cursor)));
                    } else if ("errorActionID".equals(propertyName)) {
                        retryAction.setId(getErrorID(Integer.valueOf(getInt(cursor))));
                    } else if ("noOfRetries".equals(propertyName)) {
                        retryConfiguration.setRetryCount(Integer.valueOf(getInt(cursor)));
                    } else if ("repeatInterval".equals(propertyName)) {
                        retryConfiguration.setRetryInterval(Long.valueOf(getLong(cursor)));
                    } else if ("sendErrorAfterTries".equals(propertyName)) {
                        ErrorHandlingAction action = new ErrorHandlingAction(ErrorHandlingAction.SEND_TO_ERROR_PORT);
                        action.setEnabled(true);
                        retryConfiguration.addOtherAction(action, Integer.valueOf(getInt(cursor)));
                    }
                }
            }
            cursor.resetCursor();
        }

        retryAction.setConfiguration(retryConfiguration);

        return retryAction;
    }

    private ErrorHandlingAction getErrorAction(FioranoStaxParser cursor) throws XMLStreamException {

        ErrorHandlingAction errorHandlingAction = new ErrorHandlingAction();

        if (cursor.markCursor("object")) {
            while (cursor.nextElement()) {
                if ("void".equals(cursor.getLocalName())) {
                    String propertyName = cursor.getAttributeValue(null, "property");
                    if ("active".equals(propertyName)) {
                        errorHandlingAction.setEnabled(Boolean.valueOf(getBooleanText(cursor)));
                    } else if ("errorActionID".equals(propertyName)) {
                        int id = Integer.valueOf(getInt(cursor));
                        errorHandlingAction.setId(getErrorID(id));
                        //in case of fiorano.esb.adapter.jca.error.ErrorAction.CONTINUE_REQUEST_EXECUTION
                        // i.e 'Donot stop service' - negate setEnabled
                        if (id == 2) {
                            errorHandlingAction.setEnabled(!errorHandlingAction.isEnabled());
                        }
                    }
                }
            }
            cursor.resetCursor();
        }

        return errorHandlingAction;
    }

    private List<ErrorHandlingAction> getErrorActions(FioranoStaxParser cursor) throws XMLStreamException {

        List<ErrorHandlingAction> errorActions = new ArrayList<ErrorHandlingAction>();

        if (cursor.markCursor("object")) {
            while (cursor.nextElement()) {
                if (cursor.markCursor("void")) {
                    while (cursor.nextElement()) {
                        if ("object".equals(cursor.getLocalName())) {
                            String className = cursor.getAttributeValue(null, "class");
                            if ("fiorano.esb.adapter.jca.error.RetryErrorAction".equals(className)) {
                                errorActions.add(getRetryAction(cursor));
                            } else {
                                errorActions.add(getErrorAction(cursor));
                            }
                        }
                    }
                    cursor.resetCursor();
                }
            }
            cursor.resetCursor();
        }

        return errorActions;
    }

    private Map<Integer, List<ErrorHandlingAction>> getErrorActionsMap(FioranoStaxParser cursor) throws XMLStreamException {

        Map<Integer, List<ErrorHandlingAction>> errorActionsMap = new HashMap<Integer, List<ErrorHandlingAction>>();

        if (cursor.markCursor("void")) {  //error actions Map
            Integer errorID = null;
            while (cursor.nextElement()) {
                if (cursor.markCursor("void")) {  //put
                    String methodName = cursor.getAttributeValue(null, "method");
                    if ("put".equals(methodName)) {
                        while (cursor.nextElement()) {
                            if ("object".equals(cursor.getLocalName())) {
                                String className = cursor.getAttributeValue(null, "class");
                                if ("fiorano.esb.adapter.jca.error.Error".equals(className)) {
                                    if (cursor.markCursor("void")) {
                                        String propertyName = cursor.getAttributeValue(null, "property");
                                        if ("errorID".equals(propertyName)) {
                                            errorID = Integer.valueOf(getInt(cursor));
                                        }
                                        cursor.resetCursor();
                                    }
                                } else if ("java.util.ArrayList".equals(className)) {
                                    errorActionsMap.put(errorID, getErrorActions(cursor));
                                }
                            }
                        }
                    }
                    cursor.resetCursor();
                }
            }
            cursor.resetCursor();
        }

        return errorActionsMap;
    }

    private void parseErrorConfigurations(FioranoStaxParser cursor) throws XMLStreamException {

        Map<Integer, List<ErrorHandlingAction>> errorActionsMap = null;

        if (cursor.markCursor("void")) {
            while (cursor.nextElement()) {
                if ("void".equals(cursor.getLocalName())) {
                    errorActionsMap = getErrorActionsMap(cursor);
                }
            }
            cursor.resetCursor();
        }

        AbstractErrorHandlingConfiguration errorConfig = xsltPM.getErrorHandlingConfiguration();
        Collection<ErrorHandlingAction> invalidRequestErrors = errorConfig.getActions(ServiceErrorID.INVALID_REQUEST_ERROR);
        Collection<ErrorHandlingAction> requestProcessingErrors = errorConfig.getActions(ServiceErrorID.REQUEST_EXECUTION_ERROR);

        if (errorActionsMap != null) {
            List<ErrorHandlingAction> invalidRequestErrorActions = errorActionsMap.get(0); //fiorano.esb.adapter.jca.error.Error.INVALID_REQUEST_ERROR
            List<ErrorHandlingAction> requestExecutionErrorActions = errorActionsMap.get(2); //fiorano.esb.adapter.jca.error.Error.REQUEST_EXECUTION_ERROR

            Iterator<ErrorHandlingAction> iterator = invalidRequestErrorActions.iterator();
            while (iterator.hasNext()) {
                ErrorHandlingAction action = iterator.next();
                setErrorAction(invalidRequestErrors, action);
            }

            Iterator<ErrorHandlingAction> iterator1 = requestExecutionErrorActions.iterator();
            while (iterator1.hasNext()) {
                ErrorHandlingAction action = iterator1.next();
                setErrorAction(requestProcessingErrors, action);
            }
        }

        xsltPM.setErrorHandlingConfiguration(errorConfig);
    }

    private void setErrorAction(Collection<ErrorHandlingAction> errors, ErrorHandlingAction action) {

        Iterator<ErrorHandlingAction> iterator = errors.iterator();
        while (iterator.hasNext()) {
            ErrorHandlingAction action1 = iterator.next();
            if (action.getId() == action1.getId()) {
                action1.setEnabled(action.isEnabled());
                if (action instanceof RetryAction) {
                    RetryConfiguration retryConfig = ((RetryAction) action).getConfiguration();
                    RetryConfiguration retryConfig1 = ((RetryAction) action1).getConfiguration();
                    if (retryConfig1 == null) {
                        ((RetryAction) action1).setConfiguration(retryConfig);
                    } else {    // to avoid overwriting other actions in retryConfig1
                        retryConfig1.setRetryCount(retryConfig.getRetryCount());
                        retryConfig1.setRetryInterval(retryConfig.getRetryInterval());

                        copyOtherActions(retryConfig, retryConfig1);
                    }
                }
                return;
            }
        }
    }

    private void copyOtherActions(RetryConfiguration retryConfig, RetryConfiguration retryConfig1) {
        Map otherActions = retryConfig.getOtherActions();
        for (Object obj : otherActions.entrySet()) {
            Map.Entry<ErrorHandlingAction, Integer> entry = (Map.Entry<ErrorHandlingAction, Integer>) obj;
            ErrorHandlingAction action = entry.getKey();
            if (action.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                Integer retriesBeforeError = entry.getValue();
                Map otherActions1 = retryConfig1.getOtherActions();
                Iterator iter = otherActions1.keySet().iterator();
                while (iter.hasNext()) {
                    ErrorHandlingAction action1 = (ErrorHandlingAction) iter.next();
                    if (action1.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                        iter.remove();
                        otherActions1.put(action, retriesBeforeError);
                        return;
                    }
                }
            }
        }
    }

}
