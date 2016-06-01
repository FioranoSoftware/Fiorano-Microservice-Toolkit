/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package com.fiorano.services.xslt.configuration;

import com.fiorano.edbc.framework.service.configuration.ConnectionlessServiceConfiguration;
import com.fiorano.edbc.framework.service.exception.*;
import com.fiorano.services.common.annotations.PropertyLeaf;
import com.fiorano.services.common.configuration.EncryptDecryptElements;
import com.fiorano.services.common.service.ServiceDetails;
import com.fiorano.services.common.util.RBUtil;
import com.fiorano.util.ErrorListener;
import com.fiorano.util.StringUtil;
import com.fiorano.util.annotations.resourceview.ResourceLeaf;
import com.fiorano.util.lang.ClassUtil;
import com.fiorano.xml.transform.TransformerUtil;
import fiorano.esb.record.ESBRecordDefinition;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.net.URL;
import java.util.*;


/**
 * PropertyModel class captures the configuration information from the
 * Custom Property Sheet (CPS) of the component.
 * set methods set the property to the values provided in CPS and get methods
 * can be used to get those values.
 * JMX Naming conventions should be followed for these method names.
 *
 * @fiorano.xmbean
 * @jmx.mbean
 * @jboss.xmbean
 */
public class XsltPM extends ConnectionlessServiceConfiguration {

    public final static String BODY = "Body";
    public final static String CONTEXT = "Context";
    //    public final static String BODY_CONTEXT = "Body-Context";
    public final static String CONTEXT_BODY = "Context-Body";
    public final static String XALAN = "Xalan";
    public final static String SAXON = "Saxon";
    public final static String XSLTC = "XSLTC";
    public final static String OTHER = "Other";
    private final static String XSL_INPUT_MAPPER = "Mapper";
    private final static String XSL_INPUT_USER_DEF = "User defined XSL";
    private String strXSL = "";
    private String strProject = "";
    private String tfClassName;
    private String strJMSMessageXSL = "";
    private ESBRecordDefinition esbDefInPort = null;
    private ESBRecordDefinition esbDefOutPort = null;
    private String XSLInput = XSL_INPUT_MAPPER;
    private String inputStructures = CONTEXT_BODY;
    private String outputStructure = BODY;
    private boolean bIsDefinedInputSructure = false;
    private String stripWhiteSpaces = "None";
    private boolean bIsDoOptimization = false;
    private boolean bFailOnErrors = false;
    private String xsltEngine = "Xalan";
    private ServiceDetails serviceDetails;
    private String inputStructureName;

    /**
     * @jmx:managed-constructor description="default constructor"
     */
    public XsltPM() {
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
                if (errorHandlingAction instanceof RetryAction) {
                    Map otherActions = ((RetryAction) errorHandlingAction).getConfiguration().getOtherActions();
                    if (otherActions != null) {
                        for (Map.Entry otherActionentry : (Set<Map.Entry>) otherActions.entrySet()) {
                            ErrorHandlingAction action = (ErrorHandlingAction) otherActionentry.getKey();
                            if (action.getId() == ErrorHandlingAction.LOG || action.getId() == ErrorHandlingAction.SEND_TO_ERROR_PORT) {
                                action.setEnabled(true);
                            }
                        }
                    }
                }
                if (isJMSError && errorHandlingAction.getId() == ErrorHandlingAction.STOP_SERVICE) {
                    errorHandlingAction.setEnabled(true);
                }
            }
        }
    }

    // The variable strXSL has two getters one is used in case of mapper when XSL becomes a expert property
    // another used in case of userDefined XSL when it is a normal property. Depending on the mode only one of
    //them is shown at any time - Venkat

    /**
     * This is the XSL which is used for a transfomation
     * from the source to the destination document structure.
     *
     * @return the XSL that is to be transformed
     * @jmx.managed-attribute access="read-only" description="xsl_desc"
     * @jmx.descriptor name="displayName" value="xsl_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="index" value="4"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getXSL() {
        return (getProject().trim().equals("") && getXSLInput().equals(XSL_INPUT_MAPPER)) ? "" : strXSL;
    }

    // The variable strJMSMessageXSL has two getters one is used in case of mapper when XSL becomes a expert property
    // another used in case of userDefined XSL when it is a normal property. Depending on the mode only one of
    //them is shown at any time - Venkat

    /**
     * @jmx.managed-attribute
     */
    public void setXSL(String strXSL) {
        this.strXSL = strXSL;
    }

    /**
     * The variable strJMSMessageXSL defines xslt used for setting jms message properties
     *
     * @return xslt used for setting jms message properties
     * @jmx.managed-attribute access="read-only" description="jms_message_xsl_desc"
     * @jmx.descriptor name="displayName" value="jms_message_xsl_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="index" value="5"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getJMSMessageXSL() {
        return (getProject().trim().equals("") && getXSLInput().equals(XSL_INPUT_MAPPER)) ? "" : strJMSMessageXSL;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setJMSMessageXSL(String strJMSMessageXSL) {
        this.strJMSMessageXSL = strJMSMessageXSL;
    }

    /**
     * @return Returns the value  of transformer factory classname
     * @jmx.managed-attribute access="read-write" description="tf_class_name_desc"
     * @jmx.descriptor name="displayName" value="tf_class_name_name"
     * @jmx.descriptor name="index" value="7"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    @ResourceLeaf(name = "Transformation Class Name")
    @PropertyLeaf(name = "Transformation Class Name", type = "java.lang.String", isEncrypted = "no")
    public String getTfClassName() {
        return tfClassName;
    }

    /**
     * Sets the value of transformer class name
     *
     * @jmx.managed-attribute
     */
    @PropertyLeaf(name = "Transformation Class Name", type = "java.lang.String", isEncrypted = "no")
    public void setTfClassName(String tfClassName) {
        this.tfClassName = tfClassName;
    }

    /**
     * This is the XSL which is used for a transfomation
     * from the source to the destination document structure.
     *
     * @return the XSL that is to be transformed
     * @jmx.managed-attribute access="read-write" description="user_xsl_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="displayName" value="user_xsl_name"
     * @jmx.descriptor name="index" value="4"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getUserXSL() {
        return strXSL;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setUserXSL(String strXSL) {
        this.strXSL = strXSL;
    }

    /**
     * The variable strJMSMessageXSL defines xslt used for setting jms message properties
     *
     * @return xslt used for setting jms message properties
     * @jmx.managed-attribute access="read-write" description="user_jms_message_xsl_desc"
     * @jmx.descriptor name="displayName" value="user_jms_message_xsl_name"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="index" value="5"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getUserJMSMessageXSL() {
        return strJMSMessageXSL;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setUserJMSMessageXSL(String strJMSMessageXSL) {
        this.strJMSMessageXSL = strJMSMessageXSL;
    }

    /**
     * Refers to the Fiorano Mapper project which is created
     * at runtime to specify the XSl for this transfomration.
     *
     * @return the Metadata which mentions the transformation specification
     * @jmx.managed-attribute access="read-write" description="project_desc"
     * @jmx.descriptor name="PropertyEditor" value="com.fiorano.services.xslt.cps.editor.XsltModelEditor"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.xslt.cps.editors.MapperEditorLauncher"
     * @jmx.descriptor name="displayName" value="project_name"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="index" value="3"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getProject() {
        return strProject;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setProject(String strProject) {
        this.strProject = strProject;
    }

    /**
     * Specifies whether the XSL is to be input using the
     * Fiorano Mapper or it is to be supplied manually.
     *
     * @return The way XSL provided is as input.
     * @jmx.managed-attribute access="read-write" description="mapper_used_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="displayName" value="mapper_used_name"
     * @jmx.descriptor name="primitive" value="true"
     * @jmx.descriptor name="hidesProperties" value="true"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="index" value="0"
     * @jmx.descriptor name="warningMessage" value="This action will remove any mappings previously defined using the mapper"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public boolean isMapperUsed() {
        return !XSL_INPUT_USER_DEF.equals(getXSLInput());
    }

    /**
     * Sets the XSL input mode
     *
     * @jmx.managed-attribute
     */
    public void setMapperUsed(boolean useMapper) {
        setXSLInput(useMapper ? XSL_INPUT_MAPPER : XSL_INPUT_USER_DEF);
    }

    public String getXSLInput() {
        return XSLInput;
    }

    public void setXSLInput(String strXSLInput) {

        if (!strXSLInput.equals(XSLInput) && strXSLInput.equals(XSL_INPUT_USER_DEF)) {
            strProject = "";
        }

        XSLInput = strXSLInput.trim();

        if (bIsDefinedInputSructure == false) {
            inputStructures = (strXSLInput.equals(XSL_INPUT_MAPPER)) ? CONTEXT_BODY : BODY;
        }

    }

    /**
     * Specifies whether the specified transformation is to be applied to the
     * application context or body of the input message or to both.
     *
     * @return String
     * @jmx.managed-attribute access="read-write" description="input_structures_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="legalValues" value="Body, Context, Context-Body"
     * @jmx.descriptor name="displayName" value="input_structures_name"
     * @jmx.descriptor name="index" value="1"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="warningMessage" value="This action will remove any mappings previously defined using the mapper"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getInputStructures() {

//        * Select \"Context-Body\" if the transformation is to be applied on the both Application Context and Body of input message.
//        *         In this case Application Context is considered as primary Structure.

        return inputStructures;
    }

    /**
     * Set the input structures on which the transformation should be applied
     *
     * @jmx.managed-attribute
     */
    public void setInputStructures(String strInputStructures) {
        if (!strInputStructures.equals(inputStructures)) {
            strProject = "";
        }
        inputStructures = strInputStructures.trim();
        bIsDefinedInputSructure = true;
    }

    /**
     * Specifies whether the transformation result is to be set as the application context or the body of the
     * output message.
     *
     * @return String
     * @jmx.managed-attribute access="read-write" description="output_structure_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="legalValues" value="Body, Context "
     * @jmx.descriptor name="displayName" value="output_structure_name"
     * @jmx.descriptor name="index" value="2"
     * @jmx.descriptor name="warningMessage" value="This action will remove any mappings previously defined using the mapper"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getOutputStructure() {
        return outputStructure;
    }

    /**
     * Set the output structure to which the transformation result should be set to.
     *
     * @jmx.managed-attribute
     */
    public void setOutputStructure(String strOutputStructure) {
        if (!strOutputStructure.equals(outputStructure)) {
            strProject = "";
        }

        outputStructure = strOutputStructure.trim();
    }

    /**
     * Specifies whether white space characters are to be removed from the input document structure.
     *
     * @return String
     * @jmx.managed-attribute access="read-write" description="strip_white_spaces_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="displayName" value="strip_white_spaces_name"
     * @jmx.descriptor name="legalValues" value="None, True, False"
     * @jmx.descriptor name="index" value="8"
     * @jmx.descriptor name="refresh" value="true"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getStripWhiteSpaces() {
        return stripWhiteSpaces;
    }

    /**
     * Sets the value for StripWhiteSpaces which determines if the whitespaces have to be stripped while transforming
     *
     * @jmx.managed-attribute
     */
    public void setStripWhiteSpaces(String strStripWhiteSpaces) {
        stripWhiteSpaces = strStripWhiteSpaces;
    }

    /**
     * Specifies whether the transformation should fail on errors or not.
     *
     * @return boolean
     * @jmx.managed-attribute access="read-write" description="fail_on_errors_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="displayName" value="fail_on_errors_name"
     * @jmx.descriptor name="index" value="9"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public boolean getFailOnErrors() {
        return bFailOnErrors;
    }

    /**
     * Set the input structures on which the transformation should be applied
     *
     * @jmx.managed-attribute
     */
    public void setFailOnErrors(boolean m_bFailOnErrors) {
        this.bFailOnErrors = m_bFailOnErrors;
    }

    /**
     * Specifies whether memory space optimization is to be used during the transformation.
     *
     * @return String
     * @jmx.managed-attribute access="read-write" description="do_optimization_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="displayName" value="do_optimization_name"
     * @jmx.descriptor name="index" value="10"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public boolean getDoOptimization() {
        return bIsDoOptimization;
    }

    /**
     * Set the input structures on which the transformation should be applied
     *
     * @jmx.managed-attribute
     */
    public void setDoOptimization(boolean m_bIsDoOptimization) {
        this.bIsDoOptimization = m_bIsDoOptimization;
    }

    /**
     * This is the XSLT Transformation Engine that will be used to facilitate the specified transformation.
     *
     * @return String
     * @jmx.managed-attribute access="read-write" description="xslt_engine_desc"
     * persistPolicy="Never"
     * @jmx.descriptor name="displayName" value="xslt_engine_name"
     * @jmx.descriptor name="index" value="6"
     * @jmx.descriptor name="legalValues" value="Xalan, Saxon, XSLTC, Other"
     * @jmx.descriptor name="hidesProperties" value="true"
     * @jmx.descriptor name="expert" value="true"
     * @jmx.descriptor name="defaultValue" value="Xalan"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    @ResourceLeaf(name = "XSLT Engine")
    @PropertyLeaf(name = "XSLT Engine", type = "java.lang.String", isEncrypted = "no")
    public String getXsltEngine() {
        return xsltEngine;
    }

    /**
     * Sets the XSLT transformer that should be used
     *
     * @jmx.managed-attribute
     */
    @PropertyLeaf(name = "XSLT Engine", type = "java.lang.String", isEncrypted = "no")
    public void setXsltEngine(String xsltEngine) {
        this.xsltEngine = xsltEngine;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="Input Port Description"
     * @jmx.descriptor name="hidden" value="true"
     */
    public ESBRecordDefinition getEsbDefInPort() {
        return esbDefInPort;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setEsbDefInPort(ESBRecordDefinition esbDefInPort) {
        this.esbDefInPort = esbDefInPort;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="Output Port Description"
     * @jmx.descriptor name="hidden" value="true"
     */
    public ESBRecordDefinition getEsbDefOutPort() {
        return esbDefOutPort;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setEsbDefOutPort(ESBRecordDefinition esbDefOutPort) {
        this.esbDefOutPort = esbDefOutPort;
    }

    /**
     * @jmx.managed-attribute access="read-write" description="structure_name_desc"
     * @jmx.descriptor name="displayName" value="structure_name"
     * @jmx.descriptor name="index" value="7"
     * @jmx.descriptor name="bundleName" value="com.fiorano.services.xslt.configuration.Bundle"
     */
    public String getInputStructureName() {
        return inputStructureName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setInputStructureName(String inputStructureName) {
        this.inputStructureName = inputStructureName;
    }

    /**
     * Specifies whether white space characters are to
     * be removed from the input document structure.
     */
    public Boolean getStripWhiteSpacesBool() {
        return (!stripWhiteSpaces.trim().equals("None")) ? Boolean.valueOf(stripWhiteSpaces.trim()) : null;
    }

    /**
     * @jmx.managed-operation descriptor="get Hidden Properties"
     */
    public List<String> fetchHiddenProperties() {
        List<String> listHiddenProps = super.fetchHiddenProperties();
        if (listHiddenProps == null) listHiddenProps = new ArrayList<String>();

        if (getXSLInput().equals(XSL_INPUT_USER_DEF)) {
            listHiddenProps.add("Project");
            listHiddenProps.add("XSL");
            listHiddenProps.add("JMSMessageXSL");
        } else {
            listHiddenProps.add("UserXSL");
            listHiddenProps.add("UserJMSMessageXSL");
            listHiddenProps.add("InputStructureName");
        }
        if (!(OTHER.equals(getXsltEngine())))
            listHiddenProps.add("TfClassName");

        if (isMapperUsed()) {
            listHiddenProps.add("InputStructures");
            listHiddenProps.add("OutputStructure");
        }
        return listHiddenProps;
    }

    public String getTfClassToUse() {
        if (XALAN.equals(getXsltEngine())) {
            return TransformerUtil.XALAN_TRANSFORMER_FACTORY;
        } else if (SAXON.equals(getXsltEngine())) {
            return TransformerUtil.SAXON_TRANSFORMER_FACTORY;
        } else if (XSLTC.equals(getXsltEngine())) {
            return TransformerUtil.XSLTC_TRANSFORMER_FACTORY;
        } else {
            return tfClassName;
        }
    }

    /**
     * Validates the configuration parameters in the panel.
     *
     * @param Listener error listener used to notify errors
     * @throws ServiceConfigurationException if there is any exception in validation
     */
    public void validate(ErrorListener Listener) throws ServiceConfigurationException {
        if (OTHER.equals(getXsltEngine()) && StringUtil.isEmpty(getTfClassName())) {
            throw new ServiceConfigurationException(RBUtil.getMessage(com.fiorano.services.xslt.configuration.Bundle.class, com.fiorano.services.xslt.configuration.Bundle.TRANS_CLASS_NOT_PROVIDED),
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }

        String strValidate = getXSL();

        if ((strValidate == null || strValidate.trim().equalsIgnoreCase("")) && (strJMSMessageXSL == null || strJMSMessageXSL.trim().equalsIgnoreCase(""))) {
            throw new ServiceConfigurationException(RBUtil.getMessage(com.fiorano.services.xslt.configuration.Bundle.class, com.fiorano.services.xslt.configuration.Bundle.XSL_NOT_PROVIDED),
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }

        try {
            if (!StringUtil.isEmpty(strValidate)) {
                DefaultHandler errHandler = new validationErrorHandler();

                SAXParserFactory.newInstance().
                        newSAXParser().
                        parse(new InputSource(new StringReader(strValidate)), errHandler);

            }
        } catch (Exception ex) {
            throw new ServiceConfigurationException(RBUtil.getMessage(com.fiorano.services.xslt.configuration.Bundle.class, com.fiorano.services.xslt.configuration.Bundle.INVALID_XSL), ex,
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }

        try {
            if (!StringUtil.isEmpty(strJMSMessageXSL)) {
                DefaultHandler errHandler = new validationErrorHandler();

                SAXParserFactory.newInstance().
                        newSAXParser().
                        parse(new InputSource(new StringReader(strJMSMessageXSL)), errHandler);

            }
        } catch (Exception ex) {
            throw new ServiceConfigurationException(RBUtil.getMessage(com.fiorano.services.xslt.configuration.Bundle.class, com.fiorano.services.xslt.configuration.Bundle.INVALID_JMS_XSL), ex,
                    ServiceErrorID.INVALID_CONFIGURATION_ERROR);
        }
    }

    /**
     * Overridden method which tests the component's logic.
     */
    public void test() throws ServiceException {
    }

    /**
     * Overridden method generally used for logging.
     */
    public String getAsFormattedString() {
        return "";
    }

    /**
     * @return help URL
     * @jmx.managed-operation description="Help set URL"
     */
    public URL fetchHelpSetURL() {
        return getClass().getResource(getHelpSetName());
    }

    /**
     * @return name of help set.
     */
    public String getHelpSetName() {
        return ClassUtil.getShortClassName(this.getClass()) + ".hs";
    }

    public ServiceDetails fetchServiceDetails() {
        return serviceDetails;
    }

    public void updateServiceDetails(ServiceDetails serviceDetails) {
        this.serviceDetails = serviceDetails;
    }

    /**
     * @jmx.managed-attribute
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.xslt.cps.swt.editor.XSDStructureEditor"
     */
    public EncryptDecryptElements getElementsToDecrypt() {
        return super.getElementsToDecrypt();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setElementsToDecrypt(EncryptDecryptElements elementsToDecrypt) {
        super.setElementsToDecrypt(elementsToDecrypt);
    }

    /**
     * @jmx.managed-attribute
     * @jmx.descriptor name="hidden" value="false"
     * @jmx.descriptor name="Editor" value="com.fiorano.services.xslt.cps.swt.editor.XSDStructureEditor"
     */
    public EncryptDecryptElements getElementsToEncrypt() {
        return super.getElementsToEncrypt();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setElementsToEncrypt(EncryptDecryptElements elementsToEncrypt) {
        super.setElementsToEncrypt(elementsToEncrypt);
    }

    /**
     * @jmx.managed-attribute
     * @jmx.descriptor name="hidden" value="false"
     */
    public boolean isEnableThreadPool() {
        return super.isEnableThreadPool();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setEnableThreadPool(boolean enableThreadPool) {
        super.setEnableThreadPool(enableThreadPool);
    }

    /**
     * @jmx.managed-attribute
     * @jmx.descriptor name="index" value="23"
     * @jmx.descriptor name="hidden" value="false"
     */
    @Override
    public int getPoolSize() {
        return super.getPoolSize();
    }

    /**
     * @jmx.managed-attribute
     */
    @Override
    public void setPoolSize(int poolSize) {
        super.setPoolSize(poolSize);
    }

    /**
     * @jmx.managed-attribute
     * @jmx.descriptor name="index" value="24"
     * @jmx.descriptor name="hidden" value="false"
     */
    @Override
    public long getBatchEvictionInterval() {
        return super.getBatchEvictionInterval();
    }

    /**
     * @jmx.managed-attribute
     */
    @Override
    public void setBatchEvictionInterval(long batchEvictionInterval) {
        super.setBatchEvictionInterval(batchEvictionInterval);
    }

    /**
     * <p><strong> </strong> represents </p>
     *
     * @author FSIPL
     * @version 1.0
     * @created August 16, 2005
     */
    private class validationErrorHandler extends DefaultHandler {

        /**
         * @param exception
         * @throws org.xml.sax.SAXException
         */
        public void error(SAXParseException exception)
                throws SAXException {
            throw exception;
        }

        /**
         * @param exception
         * @throws SAXException
         */
        public void fatalError(SAXParseException exception)
                throws SAXException {
            throw exception;
        }

        /**
         * @param exception
         * @throws SAXException
         */
        public void warning(SAXParseException exception)
                throws SAXException {
            throw exception;
        }
    }
}
