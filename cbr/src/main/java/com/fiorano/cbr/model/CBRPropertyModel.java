/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.cbr.model;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.services.cbr.cps.swing.Bundle;
import com.fiorano.services.cbr.engine.CBRConstants;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.uif.xml.util.XMLValidator;
import com.fiorano.util.ErrorListener;
import fiorano.esb.record.ESBRecordDefinition;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.IndependentContext;
import net.sf.saxon.trans.XPathException;

import java.util.*;
import java.util.regex.Pattern;

/**
 * <p><strong> </strong> represents </p>
 *
 * @author Venkat
 * @author Rohit
 * @version 1.20, 17 April 2008
 * @created May 24, 2005
 */
public class CBRPropertyModel extends ConnectionlessServiceConfiguration {
    public static final int MILLIS = 0;
    public static final int SECS = 1;
    public static final int MINS = 2;
    public static final int HRS = 3;
    public static final int DAYS = 4;
    public static final String[] UNITS = {"milli seconds", "seconds", "minutes", "hours", "days"};
    private ArrayList<String> xpaths = new ArrayList<>();
    private ArrayList portNames = new ArrayList();
    private ArrayList routeCounts = new ArrayList();
    private boolean m_bUseXPath1_0 = false;
    //hashMap added to resolve CBR Namespace issue - Bug #559
    private HashMap<String, String> m_namespaces;
    private boolean m_bApplyOnAppContext = false;
    private ESBRecordDefinition m_schemaDefinition = null;
    private int procesorType = CBRConstants.XPATH_TYPE;
    private int unit = MILLIS;
    private boolean fioranoCBR = false;

    public CBRPropertyModel() {
        for (Object o : errorHandlingConfiguration.getErrorActionsMap().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            for (Object action : (Set) entry.getValue()) {
                ErrorHandlingAction errorHandlingAction = (ErrorHandlingAction) action;
                if (errorHandlingAction.getId() == ErrorHandlingAction.LOG || errorHandlingAction.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                    errorHandlingAction.setEnabled(true);
                }
            }
        }
    }

    /**
     * This is to only Support Backward Compatibility
     */

    public Object clone() throws CloneNotSupportedException {
        CBRPropertyModel clone = (CBRPropertyModel) super.clone();
        if (getNamespaces() != null) {
            clone.setNamespaces((HashMap<String, String>) getNamespaces().clone());
        }
        if (getOutPortNames() != null) {
            clone.setOutPortNames((ArrayList) getOutPortNames().clone());
        }
        if (getXPaths() != null) {
            clone.setXPaths((ArrayList) getXPaths().clone());
        }
        if (getRouteCounts() != null) {
            clone.setRouteCounts((ArrayList) getRouteCounts().clone());
        }
        if (getSchemaDefinition() != null) {
            clone.setSchemaDefinition(getSchemaDefinition());
        }

        return clone;
    }

    /**
     * Returns route counts for object
     *
     * @return
     */
    public ArrayList getRouteCounts() {
        return routeCounts;
    }

/** This is to only Support Backward Compatibility ends*/

    /**
     * Sets route counts for object
     *
     * @param list
     */
    public void setRouteCounts(ArrayList list) {
        routeCounts = list;
    }

    /**
     * Returns X paths for object
     *
     * @return
     */
    public ArrayList<String> getXPaths() {
        return xpaths;
    }

    /**
     * Sets X paths for object
     *
     * @param list
     */
    public void setXPaths(ArrayList<String> list) {
        xpaths = list;
    }

    /**
     * Return namespace
     *
     * @return Hashtable
     */
    public HashMap<String, String> getNamespaces() {
        return m_namespaces;
    }

    /**
     * Set Namespace
     *
     * @param ns Hashtable
     */
    public void setNamespaces(HashMap<String, String> ns) {
        m_namespaces = ns;
    }

    /**
     * Returns out port names for object
     *
     * @return
     */
    public ArrayList getOutPortNames() {
        return portNames;
    }

    /**
     * Sets out port names for object
     *
     * @param list
     */
    public void setOutPortNames(ArrayList list) {
        portNames = list;
    }

    //get/set methods added to resolve the Namespace issue- Bug #559

    /**
     * Returns apply on X path for object
     *
     * @return
     */
    public boolean getApplyOnXPath() {
        return m_bApplyOnAppContext;
    }

    /**
     * Sets apply on X path for object
     *
     * @param isXPath
     */
    public void setApplyOnXPath(boolean isXPath) {
        m_bApplyOnAppContext = isXPath;
    }

    /**
     * Returns use X path1_0 for object
     *
     * @return
     */
    public boolean getUseXPath1_0() {
        return m_bUseXPath1_0;
    }

    /**
     * @param useXpath1_0
     */
    public void setUseXPath1_0(boolean useXpath1_0) {
        m_bUseXPath1_0 = useXpath1_0;
    }

    public int getProcessorType() {
        return procesorType;
    }

    public void setProcessorType(int type) {
        procesorType = type;
    }

    public ESBRecordDefinition getSchemaDefinition() {
        return m_schemaDefinition;
    }

    public void setSchemaDefinition(ESBRecordDefinition schemaDefinition) {
        m_schemaDefinition = schemaDefinition;
    }

    public boolean isFioranoCBR() {
        return fioranoCBR;
    }

    public void setFioranoCBR(boolean fioranoCBR) {
        this.fioranoCBR = fioranoCBR;
    }

    @Override
    public void test() throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAsFormattedString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void validate(ErrorListener listener) throws ServiceConfigurationException {

    }

    public void validateSchema(ESBRecordDefinition recordDef) throws ServiceConfigurationException {

        if (recordDef == null) {
            return;
        }

        if (recordDef.getDefinitionType() == ESBRecordDefinition.DTD) {
            throw new ServiceConfigurationException(RBUtil.getMessage(
                    Bundle.class, Bundle.DTD_NOT_SUPPORTED),
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }

        try {
            String schema = recordDef.getStructure();
            Map imports = recordDef.getImportedStructures();
            if (imports != null) {
                //use this object to set to the resource
                Hashtable importedXSDs = new Hashtable();
                for (Object entryObj : imports.entrySet()) {
                    Map.Entry entry = (Map.Entry) entryObj;
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof java.util.List) {
                        importedXSDs.put(key, ((java.util.List) value).get(0));
                    } else if (value instanceof String[]) {
                        importedXSDs.put(key, ((String[]) value)[0]);
                    } else if (value instanceof String) {
                        importedXSDs.put(key, value);
                    }
                }
                XMLValidator.validateSchema(schema, importedXSDs);
            } else {
                XMLValidator.validateSchema(schema);
            }
        } catch (Exception e) {
            throw new ServiceConfigurationException(RBUtil.getMessage(
                    Bundle.class, Bundle.SCHEMA_VALIDATION_FAILED, new Object[]{e.getMessage()}),
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }

    public void validateXPaths(List<String> xpaths) throws ServiceConfigurationException {

        if (xpaths == null || xpaths.size() == 0) {
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.ATLEAST_ONE_XPATH),
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }

    public void validateReservedPortNames(List<String> portNames) throws ServiceConfigurationException {

        String[] reservedPortNames = {"ON_EXCEPTION", "ON_TRANSACTION_FAIL", "OUT_FALSE"};

        //check for reserved port names
        for (String portName : portNames) {
            for (int k = 0; k < reservedPortNames.length; k++) {
                if (portName.equalsIgnoreCase(reservedPortNames[k])) {
                    throw new ServiceConfigurationException(RBUtil.getMessage(
                            Bundle.class, Bundle.RESERVED_PORT, new Object[]{(k + 1), portName}),
                            ServiceErrorID.INVALID_CONFIGURATION_ERROR);
                }
            }
        }
    }

    public void validateDuplicatePortNames(List<String> portNames) throws ServiceConfigurationException {

        Properties portNameMap = new Properties();
        for (int index = 0; index < portNames.size(); index++) {
            String portName = (portNames.get(index)).toLowerCase();
            String portIndex = (String) portNameMap.setProperty(portName, index + "");
            if (portIndex != null) {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.SAME_NAME,
                        new Object[]{(index + 1), portName, (Integer.parseInt(portIndex) + 1)}),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
        }
    }

    public void validatePortNamesForXPaths(List<String> portNames, ArrayList<String> xpaths)
            throws ServiceConfigurationException {

        String xpath;

        for (int i = 0; i < xpaths.size(); i++) {
            String portName = portNames.get(i);

            if (portName == null || portName.trim().equals("")) {
                throw new ServiceConfigurationException(RBUtil.getMessage(
                        Bundle.class, Bundle.NO_PORT_NAME, new Object[]{(i + 1)}),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }

            if (hasInvalidCharacters(portName) || portName.startsWith("_") || portName.endsWith("_") || portName.contains("__")) {
                throw new ServiceConfigurationException(RBUtil.getMessage(
                        Bundle.class, Bundle.INVALID_PORT_NAME, new Object[]{(i + 1)}),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
            xpath = xpaths.get(i);
            if (xpath == null || xpath.trim().equals("")) {
                throw new ServiceConfigurationException(RBUtil.getMessage(
                        Bundle.class, Bundle.NO_XPATH, new Object[]{portName}),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
        }
    }

    public boolean hasInvalidCharacters(String text) {
        String[] invalidCharArray = new String[]{"\\\\", "/", "\\+", "\\(", "\\)", "~", "`", "=", "\\{", "\\}", "\\[", "\\]", "\\^", "\\*", "\\?",
                "\\$", "\\|", "!", "@", "#", "%", "\"", "'", ";", ":", "<", ">", "&", ".", "\\s", ","};
        StringBuilder sb = new StringBuilder("[");

        for (String anInvalidCharArray : invalidCharArray) {
            sb.append(anInvalidCharArray);
        }
        sb.append("]+");

        Pattern INVALID_INPUT_CHARS_REGEX = Pattern.compile(sb.toString());
        return INVALID_INPUT_CHARS_REGEX.matcher(text).find();
    }

    public void validateXPATH(List<String> list, String processorType) throws ServiceConfigurationException {

        if (!("XPATH".equals(processorType))) {
            return;
        }

        XPathExpression xpaths[] = new XPathExpression[list.size()];
        XPathEvaluator evaluator = new XPathEvaluator();

        if (getUseXPath1_0()) {
            //In SAXON, for XPath 1.0 support context.isInBackwardsCompatibleMode
            // should return true.
            evaluator.setStaticContext(
                    new IndependentContext() {
                        public boolean isInBackwardsCompatibleMode() {
                            return true;
                        }
                    });
        }

        //added to resolve Bug #559 - CBR Namespace issue -Priya
        IndependentContext stx = evaluator.getStaticContext();
        HashMap namespaces = getNamespaces();

        if (namespaces != null) {
            Iterator it = namespaces.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();

                stx.declareNamespace((String) entry.getKey(),
                        (String) entry.getValue());
            }
        }
        //end of code - Bug #559
        for (int i = xpaths.length - 1; i >= 0; i--) {
            String str = list.get(i);

            if (str != null && str.length() > 0) {
                try {
                    xpaths[i] = evaluator.createExpression(str);
                } catch (XPathException e) {
                    throw new ServiceConfigurationException(RBUtil.getMessage(
                            Bundle.class, Bundle.INVALID_XPATH_ROW, new Object[]{(i + 1)}),
                            ServiceErrorID.INVALID_CONFIGURATION_ERROR);
                }
            }
        }
    }

    public void validatePrefixes(List<String> prefixes) throws ServiceConfigurationException {

        HashSet<String> prefixSet = new HashSet<>();
        for (int index = 0; index < prefixes.size(); index++) {
            if (!prefixSet.add((prefixes.get(index)).toLowerCase()))
                throw new ServiceConfigurationException(RBUtil.getMessage(
                        Bundle.class, Bundle.DUPLICATE_PREFIX_DESC, new Object[]{prefixes.get(index)}),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }

    public void validateURIs(List<String> uris) throws ServiceConfigurationException {

        HashSet<String> prefixSet = new HashSet<>();
        for (int index = 0; index < uris.size(); index++) {
            if (!prefixSet.add((uris.get(index)).toLowerCase()))
                throw new ServiceConfigurationException(RBUtil.getMessage(
                        Bundle.class, Bundle.DUPLICATE_NAMESPACE_DESC, new Object[]{uris.get(index)}),
                        ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit < MILLIS && unit > DAYS ? MILLIS : unit;
    }
}
