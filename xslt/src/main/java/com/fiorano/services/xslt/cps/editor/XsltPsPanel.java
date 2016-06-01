/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.xslt.cps.editor;

import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.esb.wrapper.ICPSESBUtil;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.xslt.configuration.XsltPM;
import com.fiorano.xml.ClarkName;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.util.CPSUtil;
import fiorano.tifosi.mapper.MapperMain;
import fiorano.tifosi.mapper.launcher.DefaultMapperLauncher;
import fiorano.tifosi.mapper.model.Structure;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.PropertyEnv;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.swing.*;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Phani
 * Date: Jun 25, 2009
 * Time: 11:41:44 AM
 * To change this template use File | Settings | File Templates.
 */

public class XsltPsPanel {

    private final static String EMPTY_CONTEXT_STR = "<!ELEMENT Context (#PCDATA)>";

    //input port name
    private final static String IN_PORT = "IN_PORT";

    //output portname
    private final static String OUT_PORT = "OUT_PORT";

    private XsltModelEditor xsltModelEditor;
    private String strProject;
    private String strXSL;
    private String strJMSMessageXSL;
    private ESBRecordDefinition esbDefInPort;
    private ESBRecordDefinition esbDefOutPort;

    private MBeanServerConnection mbeanServerCon;
    private ObjectName modelClass;
    private ICPSESBUtil cpsEsbUtil;
    private String strOutputStructure;
    private String strInputStructures;
    private Logger logger = CPSUtil.getAnonymousLogger();


    /**
     * @param editor
     * @param propertyEnv
     */
    public XsltPsPanel(XsltModelEditor editor, PropertyEnv propertyEnv) {
        xsltModelEditor = editor;

        if (editor != null) {
            //fetch the mapper project
            strProject = (String) editor.getValue();
        }

        modelClass = (ObjectName) propertyEnv.getFeatureDescriptor().getValue(ObjectName.class.getName());

        try {

            mbeanServerCon = (MBeanServerConnection) propertyEnv.getFeatureDescriptor().getValue(
                    MBeanServerConnection.class.getName());
            cpsEsbUtil = CPSESBUtil.getCPSESBUtil(mbeanServerCon, false);

            //port definitions for XSLT
            esbDefInPort = (ESBRecordDefinition) mbeanServerCon.getAttribute(modelClass, "EsbDefInPort");
            esbDefOutPort = (ESBRecordDefinition) mbeanServerCon.getAttribute(modelClass, "EsbDefOutPort");

        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }

        launchPs();
    }

    /**
     * Returns mapper for object
     */
    public MapperMain getMapper() {
        return MapperMain.getMapperInstance();
    }

    private int getPosition(boolean bIsInput) {

        if (bIsInput) {
            int index = strInputStructures.indexOf(XsltPM.BODY);
            return index == -1 ? -1 : index > 0 ? 1 : 0;
        } else {
            return strOutputStructure.trim().equals(XsltPM.BODY) ? 0 : -1;
        }
    }

    /**
     * This function fetches the port structure from the mapper and sets them to portDefinitions
     *
     * @param bIsInput True if processing input port else false
     */
    private void setPortStructure(DefaultMapperLauncher launcher, boolean bIsInput) {

        //if input get input vector from mapper get output vector
        Vector vectStructs = bIsInput ? launcher.getInput() : launcher.getOutput();

        //portDefinition to be set based input or output
        ESBRecordDefinition portDefinition = bIsInput ? esbDefInPort : esbDefOutPort;

        Structure structure;

        //if port definition is not present already create a new definition
        if (portDefinition == null) {
            portDefinition = new ESBRecordDefinition();
        }

        //determines the probable index of required structure in vector
        int iCnt = getPosition(bIsInput);

        //if index value is greate than the size of vector the structure cannot be present do not set anything, return
        if (iCnt == -1 || iCnt >= vectStructs.size()) {
            return;
        }

        //fetch the structure
        structure = (Structure) vectStructs.get(iCnt);

        //if structure is not present return. not required ideally, but if there is some problem with mapper it will save
        //running into exception
        if (structure == null) {
            return;
        }

        //set the port definition
        portDefinition.setStructure(structure.source);
        portDefinition.setRootElementName(structure.rootName);
        portDefinition.setTargetNamespace(structure.nsURI);
        portDefinition.setImportedStructures(structure.externalStructures);
        // Rohit -> Fix for 9273 begins.
        if (structure.mime != null && structure.mime.equalsIgnoreCase("XSD")) {
            portDefinition.setDefinitionType(ESBRecordDefinition.XSD);
        }
        if (structure.mime != null && structure.mime.equalsIgnoreCase("DTD")) {
            portDefinition.setDefinitionType(ESBRecordDefinition.DTD);
        }
        // Rohit -> Fix for 9273 ends.
        try {
            //set the port definition to appropriate port by invoking setter on appropriate object
            mbeanServerCon.setAttribute(modelClass, new Attribute(
                    bIsInput ? "EsbDefInPort" : "EsbDefOutPort", portDefinition));
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }

    private void fillVector(Vector<Structure> vector, boolean bIsInput) {
        try {
            Map portSchemas = (Map) (bIsInput ? cpsEsbUtil.fetchConnectedOutputPortSchemas().get(IN_PORT)
                    : cpsEsbUtil.fetchConnectedInputPortSchemas().get(OUT_PORT));

            if (portSchemas != null) {
                Map.Entry entry = null;
                for (Map.Entry loopEntry : (Set<Map.Entry>) portSchemas.entrySet()) {
                    entry = loopEntry;
                }
                if (entry != null && (entry.getValue() != null)) {
                    ESBRecordDefinition esbRecord = (ESBRecordDefinition) entry.getValue();
                    //name that should be given to the structure based on input or output
                    String strStructName = bIsInput ? IN_PORT : OUT_PORT;
                    Structure struct = createStructure(esbRecord, strStructName);
                    vector.add(struct);

                    if (bIsInput) {
                        if (esbDefInPort == null) {
                            esbDefInPort = new ESBRecordDefinition();
                            esbDefInPort.setStructure(esbRecord.getStructure());
                            esbDefInPort.setImportedStructures(esbRecord.getImportedStructures());
                            esbDefInPort.setRootElementName(esbRecord.getRootElementName());
                            esbDefInPort.setTargetNamespace(esbRecord.getTargetNamespace());
                        }
                    } else {
                        if (esbDefOutPort == null) {
                            esbDefOutPort = new ESBRecordDefinition();
                            esbDefOutPort.setStructure(esbRecord.getStructure());
                            esbDefOutPort.setImportedStructures(esbRecord.getImportedStructures());
                            esbDefOutPort.setRootElementName(esbRecord.getRootElementName());
                            esbDefOutPort.setTargetNamespace(esbRecord.getTargetNamespace());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }

    private Structure createStructure(ESBRecordDefinition esbRecord, String strStructName) {
        Structure struct = new Structure(strStructName, esbRecord.getStructure(), null,
                esbRecord.getRootElementName(), null);

        Map importedStructures = esbRecord.getImportedStructures();
        Map<String, String> externalXsds = new Hashtable<String, String>();
        if (importedStructures != null) {
            for (Object obj : importedStructures.entrySet()) {
                Map.Entry entry2 = (Map.Entry) obj;
                String key = (String) entry2.getKey();
                java.util.List extXsdList = (java.util.List) entry2.getValue();
                if (extXsdList != null)
                    externalXsds.put(key, (String) extXsdList.get(0)); //No multiple xsds with same namespace support in mapper
            }
        }
        struct.externalStructures = (Hashtable) externalXsds;

        if (struct.mime.equals("xsd")) {
            struct.parser = "Xerces";
        }
        return struct;
    }


    /**
     * Shows the Custom Property Sheet
     */
    private void launchPs() {

        Dialog dlg = DialogDisplayer.getDefault().createDialog(new DialogDescriptor(new JPanel(), ""));
        Window owner = dlg.getOwner();
        dlg.dispose();

        String project = strProject;

        //vector s which hold input and output structures
        Vector<Structure> input = new Vector<Structure>();
        Vector<Structure> output = new Vector<Structure>();

        String context = null;
        QName contextRootElement = null;

        try {

            //structures to be loaded into the mapper
            strOutputStructure = (String) mbeanServerCon.getAttribute(modelClass, "OutputStructure");
            strInputStructures = (String) mbeanServerCon.getAttribute(modelClass, "InputStructures");

            ISchema appCtxSchema = cpsEsbUtil.fetchApplicationContextSchema();
            if (appCtxSchema != null) {
                context = appCtxSchema.getStructure();
                contextRootElement = ClarkName.toQName((appCtxSchema.getRootElementName()));
            }
        }
        //  handled for configuration through configureBC.bat
        catch (InstanceNotFoundException e) {
            logger.log(Level.INFO, e.getLocalizedMessage());
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }

        if (context == null) {
            context = EMPTY_CONTEXT_STR;
        }
        String contextRootElementName = contextRootElement != null ? contextRootElement.getLocalPart() : null;
        Structure contextStructure = new Structure("Application-Context", context, null, contextRootElementName, null, false, true);
        contextStructure.nsURI = contextRootElement != null ? contextRootElement.getNamespaceURI() : null;

        //if the input structures to be passed contain Context i.e the value of strOutputStructures is
        // Context or Body-Context or Context-Body
        boolean bIsInputContext = (strInputStructures.indexOf(XsltPM.CONTEXT) != -1);

        //if context is to be passed add it as first structure in the vector
        if (bIsInputContext) {
            input.add(contextStructure);
        }
        //if body is to be passed add the xsd as structures to vector
        if (strInputStructures.indexOf(XsltPM.BODY) != -1) {
            fillVector(input, true);
        }

        //if the output structures to be passed contain Context i.e the value of strOutputStructures is Context
        boolean bIsOutputContext = (strOutputStructure.indexOf(XsltPM.CONTEXT) != -1);

        //if context is to be passed add it as first structure in the vector
        if (bIsOutputContext) {
            output.add(contextStructure);
        }
        //if body is to be passed add the xsd as structures to vector
        if (strOutputStructure.indexOf(XsltPM.BODY) != -1) {
            fillVector(output, false);
        }

        //@todo currently no api available
        boolean isRunning = false;

        try {
            DefaultMapperLauncher launcher =
                    new DefaultMapperLauncher(
                            owner, project,
                            input, new String[]{"XML", "DTD", "XSD"},
                            output, new String[]{"XML", "DTD", "XSD"},
                            true, true, isRunning, bIsInputContext, bIsOutputContext, false, strInputStructures.indexOf("-") != -1 ? 2 : 1, 2) {
                        public void windowClosed(WindowEvent event) {
                            super.windowClosed(event);

                            //save the project, xsl and port definitions when the mapper is closed
                            if (isChanged()) {
                                try {
                                    strProject = getProject();
                                    if (strProject == null) {
                                        strProject = "";
                                    }
                                } catch (Exception e) {
                                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                                }

                                try {
                                    Vector input = getInput();
                                    if (input.size() == 2) {
                                        mbeanServerCon.setAttribute(modelClass, new Attribute("InputStructureName", ((Structure) input.get(1)).name));
                                    }
                                } catch (Exception e) {
                                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                                }

                                try {

                                    //saving XSL
                                    String xsl[] = getMetaData();

                                    if (xsl != null && xsl.length > 0 && xsl[0] != null) {
                                        strXSL = xsl[0];
                                    } else {
                                        strXSL = "";
                                    }
                                    mbeanServerCon.setAttribute(modelClass, new Attribute("XSL", strXSL));

                                    if (xsl != null && xsl.length > 1 && xsl[1] != null) {
                                        strJMSMessageXSL = xsl[1];
                                    } else {
                                        strJMSMessageXSL = "";
                                    }
                                    mbeanServerCon.setAttribute(modelClass, new Attribute("JMSMessageXSL", strJMSMessageXSL));

                                    if (strXSL == null) {
                                        strXSL = "";
                                    }
                                    if (strJMSMessageXSL == null) {
                                        strJMSMessageXSL = "";
                                    }
                                } catch (Exception e) {
                                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                                }
                                //saving project
                                xsltModelEditor.setValue(strProject);

                                //saving port definitions
                                setPortStructure(this, true);
                                setPortStructure(this, false);
                            }
                        }
                    };
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }

    }
}

