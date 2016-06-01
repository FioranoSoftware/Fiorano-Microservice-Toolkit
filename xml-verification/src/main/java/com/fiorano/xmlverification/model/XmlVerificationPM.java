/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.xmlverification.model;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.ErrorHandlingAction;
import com.fiorano.edbc.framework.service.exception.ServiceConfigurationException;
import com.fiorano.edbc.framework.service.exception.ServiceErrorID;
import com.fiorano.edbc.framework.service.exception.ServiceException;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.ErrorListener;
import com.fiorano.util.lang.ClassUtil;
import com.fiorano.xml.xsd.XSDUtil;
import fiorano.esb.record.ESBRecordDefinition;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used to set and get the runtime parameters used by the service. The message is sent on the output port if validation
 * is successful. If the validation fails it is sent to the failed port with two message properties
 * ERROR_MESSAGE - this contains the reason
 * STACKTRACE - this contains the exception trace for failure.
 *
 * @author Jissy
 * @version 1.0
 * @created March 11, 2002
 * @fiorano.xmbean
 * @jmx.mbean
 */
public class XmlVerificationPM extends ConnectionlessServiceConfiguration {
    public static final String BODY = "Body";
    public static final String CONTEXT = "Context";
    public static final String CONTEXT_BODY = "Context-Body";
    public static final String NONE = "None";
    byte structureType = 0;
    // Stores the DTD against which to verify XML
    private String m_xsdStructures;
    private ESBRecordDefinition m_xsdBody;
    private ESBRecordDefinition m_xsdContext;

    public XmlVerificationPM() {
        setDefaults();
        Iterator errorActionsIterator = errorHandlingConfiguration.getErrorActionsMap().entrySet().iterator();
        while (errorActionsIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) errorActionsIterator.next();
            Set actions = (Set) entry.getValue();
            boolean isJMSError = ServiceErrorID.TRANSPORT_ERROR.equals(entry.getKey());
            for (Iterator iterator = actions.iterator(); iterator.hasNext(); ) {
                ErrorHandlingAction errorHandlingAction = (ErrorHandlingAction) iterator.next();
                if (errorHandlingAction.getId() == ErrorHandlingAction.LOG || errorHandlingAction.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                    errorHandlingAction.setEnabled(true);
                }
                if (isJMSError && errorHandlingAction.getId() == ErrorHandlingAction.STOP_SERVICE) {
                    errorHandlingAction.setEnabled(true);
                }
            }
        }
    }

    /**
     * Gets the DTD against which to validate XML.
     *
     * @return The DTD as a string
     */

    public String getXSD() {
        return getBody() != null ? getBody().getStructure() : null;
    }

    /**
     * Sets the DTD against which to validate XML.
     *
     * @param strXSD the DTD
     */
    public void setXSD(String strXSD) {
        if (strXSD == null) {
            structureType = (byte) (structureType & 2);
        } else {
            structureType = (byte) (structureType | 1);
        }
        setStructureType(structureType);
        if (getBody() == null) {
            setBody(new ESBRecordDefinition());
        }
        getBody().setStructure(strXSD);
    }

    /**
     * Gets the DTD against which to validate XML.
     *
     * @return The DTD as a string
     */
    public String getContextXSD() {
        return getContext() != null ? getContext().getStructure() : null;
    }

    /**
     * Sets the DTD against which to validate XML.
     *
     * @param strContextXSD the DTD
     */
    public void setContextXSD(String strContextXSD) {
        if (strContextXSD == null) {
            structureType = (byte) (structureType & 1);
        } else {
            structureType = (byte) (structureType | 2);
        }
        setStructureType(structureType);
        if (getContext() == null) {
            setContext(new ESBRecordDefinition());
        }
        getContext().setStructure(strContextXSD);
    }

    /**
     * @jmx.managed-attribute access="read-write" description="body_desc"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.adapter.jca.editors.schema.SchemaPropertyEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.editors.SchemaPropertyEditor"
     * @jmx.descriptor name="displayName" value="body_name"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="index" value="1"
     * @jmx.descriptor name="bundleName" value="com.fiorano.xmlverification.model.Bundle"
     */
    public ESBRecordDefinition getBody() {
        return m_xsdBody;
    }

    /**
     * Sets the Body against which to validate XML.
     *
     * @param strBody the DTD
     * @jmx.managed-attribute
     */
    public void setBody(ESBRecordDefinition strBody) {
        m_xsdBody = strBody;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="context_desc"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.adapter.jca.editors.schema.SchemaPropertyEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.cps.editors.SchemaPropertyEditor"
     * @jmx.descriptor name="displayName" value="context_name"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="index" value="2"
     * @jmx.descriptor name="bundleName" value="com.fiorano.xmlverification.model.Bundle"
     */
    public ESBRecordDefinition getContext() {
        return m_xsdContext;
    }

    /**
     * Sets the Context against which to validate XML.
     *
     * @param strContext the DTD
     * @jmx.managed-attribute
     */
    public void setContext(ESBRecordDefinition strContext) {
        m_xsdContext = strContext;
//        if(strContext!=null)
//        setcontextRootName(strContext.getRootElementName());
    }

    /**
     * Specifies whether the specified verification is to be applied to the
     * application context or body of the input message or to both.
     *
     * @return String
     * @jmx.managed-attribute access="read-write" description="xsd_structures_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="legalValues" value="Body, Context, Context-Body"
     * @jmx.descriptor name="displayName" value="xsd_structures_name"
     * @jmx.descriptor name="defaultValue" value="Body"
     * @jmx.descriptor name="hidesProperties" value="true"
     * @jmx.descriptor name="index" value="0"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="bundleName" value="com.fiorano.xmlverification.model.Bundle"
     */
    public String getXSDStructures() {
        return m_xsdStructures;
    }

    /**
     * Set the xsd structures on which the verification should be applied
     *
     * @jmx.managed-attribute
     */
    public void setXSDStructures(String strxsdStructures) {
        m_xsdStructures = strxsdStructures;
        if (BODY.equals(m_xsdStructures)) {
            setContext(null);
        }
        if (CONTEXT.equals(m_xsdStructures)) {
            setBody(null);
        }
    }

    public byte getStructureType() {
        return structureType;
    }

    private void setStructureType(byte structureType) {
        this.structureType = structureType;
        switch (structureType) {
            case 0:
                setXSDStructures(NONE);
            case 1:
                setXSDStructures(BODY);
                break;
            case 2:
                setXSDStructures(CONTEXT);
                break;
            case 3:
                setXSDStructures(CONTEXT_BODY);
                break;
            default:
                setXSDStructures(BODY);
        }
    }

    /**
     * Returns whether the validation of input is enabled or not.
     * Since there is no schema set on component's input port, here the validation is disabled.
     *
     * @jmx.managed-attribute access="read-write" description="The service tries to validate the input received if set to true. If this is set to false,
     * service will not validate the input and hence the performance increases. CAUTION: Setting this to false may cause undesired results if the input xml is
     * not valid"
     * @jmx.descriptor name="displayName" value="Validate input"
     * @jmx.descriptor name="defaultValue" value="true"
     * @jmx.descriptor name="index" value="-1"
     * @jmx.descriptor name="hidden" value="true"
     */
    public boolean isInputValidationEnabled() {
        return false;
    }

    /**
     * sets whether the validation of input is enabled or not
     *
     * @jmx.managed-attribute
     */
    public void setInputValidationEnabled(boolean inputValidationEnabled) {
        this.inputValidationEnabled = inputValidationEnabled;
    }

    public void validate(ErrorListener Listener) throws ServiceConfigurationException {
        if (CONTEXT_BODY.equals(getXSDStructures())) {
            //if Context-Body Validate both
            //pass body
            validateSchema(getBody(), getXSDStructures(), BODY);
            //pass context
            validateSchema(getContext(), getXSDStructures(), CONTEXT);
        } else {
            if (BODY.equals(getXSDStructures())) // verify body
            {
                validateSchema(getBody(), getXSDStructures(), getXSDStructures());
            } else if (CONTEXT.equals(getXSDStructures())) //verify context
            {
                validateSchema(getContext(), getXSDStructures(), getXSDStructures());
            } else  //this should not happen if it happens its invalid.
            {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.INVALID_STRUCTURES, new String[]{getXSDStructures()}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
        }
    }

    /**
     * Validates the schema provided
     *
     * @param recordDefinition  - Schema to validate
     * @param requiredStructure - The structure which is expected. Values from {Context, Body or Context-Body}
     * @param currentStructure  - The structure which is passed. Values from {Context, Body}
     */
    private void validateSchema(ESBRecordDefinition recordDefinition, String requiredStructure, String currentStructure)
            throws ServiceConfigurationException {

        // This method should be called only if the schema is required. Hence if schema is not specified throw exception
        if (recordDefinition == null || recordDefinition.getStructure() == null) {
            throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.SCHEMA_NOT_PROVIDED, new String[]{requiredStructure, currentStructure}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }

        //check if it is DTD
        boolean dtd = recordDefinition.getDefinitionType() == ESBRecordDefinition.DTD
                || (recordDefinition.getDefinitionType() == ESBRecordDefinition.NONE
                && recordDefinition.getStructure().indexOf("<!ELEMENT") != -1);

        //do this only for XSD as schema editor does not validate XSD with imported schemas
        if (!dtd) {
            //create XSModel if it is done without errors XSD is valid.
            XSLoader xsLoader = null;
            try {
                xsLoader = XSDUtil.createXSLoader(recordDefinition, null);
            } catch (Exception e) {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.SCHEMA_NOT_VALID, new String[]{currentStructure}), e, ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
            XSModel xsModel = xsLoader.load(
                    new DOMInputImpl(null, null, null, recordDefinition.getStructure(), null));
            if (xsModel == null) {
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.SCHEMA_NOT_VALID, new String[]{currentStructure}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
            if (xsModel.getComponents(XSConstants.ELEMENT_DECLARATION).getLength() == 0) {
                String noElemsMsg = RBUtil.getMessage(Bundle.class, Bundle.NO_ELEMENTS);
                throw new ServiceConfigurationException(RBUtil.getMessage(Bundle.class, Bundle.SCHEMA_NOT_VALID_REASON, new String[]{currentStructure, noElemsMsg}), ServiceErrorID.INVALID_CONFIGURATION_ERROR);
            }
        }

    }

    /**
     * @return help url
     * @jmx.managed-operation description="Help set URL"
     */
    public URL fetchHelpSetURL() {
        return getClass().getResource(getHelpSetName());
    }

    public String getHelpSetName() {
        return ClassUtil.getShortClassName(this.getClass()) + ".hs";
    }

    /**
     * @jmx.managed-operation descriptor="get Hidden Properties"
     */
    public List<String> fetchHiddenProperties() {
        List<String> hiddenProperties = super.fetchHiddenProperties();
        if (BODY.equals(getXSDStructures())) {
            hiddenProperties.add(CONTEXT);
        }
        if (CONTEXT.equals(getXSDStructures())) {
            hiddenProperties.add(BODY);
        }
        return hiddenProperties;
    }

    @Override
    public void test() throws ServiceException {
    }

    private void setDefaults() {
        setXSDStructures(BODY);
    }

    public String getAsFormattedString() {
        return null;
    }
}