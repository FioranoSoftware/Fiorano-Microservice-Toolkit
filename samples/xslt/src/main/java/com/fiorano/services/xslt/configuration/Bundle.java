/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xslt.configuration;

/**
 * User: deepthi
 * Date: Nov 25, 2009
 * Time: 6:10:45 PM
 */
public interface Bundle {

    /**
     * @msg.message msg="Transformer class is not provided"
     */
    String TRANS_CLASS_NOT_PROVIDED = "trans_class_not_provided";

    /**
     * @msg.message msg="Either XSL or JMS-Message XSL has to be configured"
     */
    String XSL_NOT_PROVIDED = "xsl_not_provided";

    /**
     * @msg.message msg="Invalid XSL"
     */
    String INVALID_XSL = "invalid_xsl";

    /**
     * @msg.message msg="Invalid JMS-Message XSL"
     */
    String INVALID_JMS_XSL = "invalid_jms_xsl";

    //Attributes

    /**
     * @msg.message msg="XSL"
     */
    String XSL_NAME = "xsl_name";
    /**
     * @msg.message msg="XSL is the XML based Style Sheet used for the transformation"
     */
    String XSL_DESC = "xsl_desc";

    /**
     * @msg.message msg="JMS-Message XSL"
     */
    String JMS_MESSAGE_XSL_NAME = "jms_message_xsl_name";
    /**
     * @msg.message msg="xslt used for setting jms message properties on the output message."
     */
    String JMS_MESSAGE_XSL_DESC = "jms_message_xsl_desc";

    /**
     * @msg.message msg="Transformer factory class"
     */
    String TF_CLASS_NAME_NAME = "tf_class_name_name";
    /**
     * @msg.message msg="The fully qualified name of the class which should be used to perform transformation
     * when the transformer implementation other than that is provided by \"Saxon\" or \"Xalan\" is being used.
     * Note: The class provided should be an implenation of \"Javax.xml.transform.TransformerFactory\"."
     */
    String TF_CLASS_NAME_DESC = "tf_class_name_desc";

    /**
     * @msg.message msg="XSL"
     */
    String USER_XSL_NAME = "user_xsl_name";
    /**
     * @msg.message msg="User defined XSL for the transformation"
     */
    String USER_XSL_DESC = "user_xsl_desc";

    /**
     * @msg.message msg="JMS-Message XSL"
     */
    String USER_JMS_MESSAGE_XSL_NAME = "user_jms_message_xsl_name";
    /**
     * @msg.message msg="xslt used for setting jms message properties on the Output message."
     */
    String USER_JMS_MESSAGE_XSL_DESC = "user_jms_message_xsl_desc";

    /**
     * @msg.message msg="Mappings"
     */
    String PROJECT_NAME = "project_name";
    /**
     * @msg.message msg="Defines the Fiorano Mapper Project that can be created using Fiorano Mapper. Contains the input structure and output structure and the mappings defined.
     * The XSL required for transformation is created automatically based on the mappings defined in Fiorano Mapper."
     */
    String PROJECT_DESC = "project_desc";

    /**
     * @msg.message msg="Use Mapper to define transformation"
     */
    String MAPPER_USED_NAME = "mapper_used_name";
    /**
     * @msg.message msg="Select \"yes\" to use Mapper for defining XSL. Select \"no\" to manually provide the XSL."
     */
    String MAPPER_USED_DESC = "mapper_used_desc";

    /**
     * @msg.message msg="Transformation source data"
     */
    String INPUT_STRUCTURES_NAME = "input_structures_name";
    /**
     * @msg.message msg="Select \"Body\" if the transformation is to be applied on the Body of input message.
     * Select \"Context\" if the transformation is to be applied on the Application Context of input message.
     * Select \"Body-Context\" if the transformation is to be applied on the both Body and Application Context of input message.
     * In this case, XML instance of application context is treated as primary source.
     * Note: Elements in primary source can be referenced directly in XSL, where as elements of other structure should be referenced as document (StructureName)/ElementName."
     */
    String INPUT_STRUCTURES_DESC = "input_structures_desc";

    /**
     * @msg.message msg="Set transformation result as"
     */
    String OUTPUT_STRUCTURE_NAME = "output_structure_name";
    /**
     * @msg.message msg="Select \"Body\" if the transformation result is to be set to the Body of output message.
     * Select \"Context\" if the transformation result is to be set to the Application Context of output message."
     */
    String OUTPUT_STRUCTURE_DESC = "output_structure_desc";

    /**
     * @msg.message msg="Strip White Spaces"
     */
    String STRIP_WHITE_SPACES_NAME = "strip_white_spaces_name";
    /**
     * @msg.message msg=" None - Attribute to strip whitespace is not set at all.
     * The behaviour depends on the transformer's implementation.
     * True - Strips whitespace content from the XML before the transformation is done.
     * False - Whitespace content is retained as it is."
     */
    String STRIP_WHITE_SPACES_DESC = "strip_white_spaces_desc";

    /**
     * @msg.message msg="Fail transformation on error"
     */
    String FAIL_ON_ERRORS_NAME = "fail_on_errors_name";
    /**
     * @msg.message msg="This property determines the action to be taken when errors occur during transformation.
     * \"Yes\" - Transformer errors are reported as errors in component. Warnings are logged at log level WARNING and can be treated as errors in component if
     * \"Throw fault on warnings\" is enabled under \"Request Processing Error\" in \"Error Hanlding panel\" . \"No\" - Both errors and warnings are logged at log level WARNING and the result of the transformation is sent out.
     * Note: Transormer Fatal errors are always reported as errors in component."
     */
    String FAIL_ON_ERRORS_DESC = "fail_on_errors_desc";

    /**
     * @msg.message msg="Optimization"
     */
    String DO_OPTIMIZATION_NAME = "do_optimization_name";
    /**
     * @msg.message msg=" This property can be used when applying the transformation on large data.
     * If set to \"Yes\", some internal structures are cleared from the input message, optimizing the memory usage before the transformation begins.
     * When this is set to \"yes\" and \"Set transformation result as\" is set to \"Body\", \"Text-Content\" and \"Byte-Content\" funclets of \"JMS Message Functions\" in Fiorano Mapper cannot be used.
     * Note: This property comes into effect only when there is no \"JMS-Message XSL\" defined."
     */
    String DO_OPTIMIZATION_DESC = "do_optimization_desc";

    /**
     * @msg.message msg="Xslt Engine"
     */
    String XSLT_ENGINE_NAME = "xslt_engine_name";
    /**
     * @msg.message msg="The transformer implementation that should be used for performing the transformation.
     * Select \"Other\" to use a custom transformer implementation. XSLTC and Saxon doesn't support script extensions and jms message methods."
     */
    String XSLT_ENGINE_DESC = "xslt_engine_desc";

    /**
     * @msg.message msg="Structure name for Input"
     */
    String STRUCTURE_NAME = "structure_name";
    /**
     * @msg.message msg="This property defines name of the input structure that is referenced in XSL provided."
     */
    String STRUCTURE_NAME_DESC = "structure_name_desc";
}
