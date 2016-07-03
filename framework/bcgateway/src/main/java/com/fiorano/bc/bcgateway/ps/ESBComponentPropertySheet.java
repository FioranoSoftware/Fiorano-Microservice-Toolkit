/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.bc.bcgateway.ps;

import com.fiorano.bc.bcgateway.Bundle;
import com.fiorano.bc.trgateway.model.Configuration;
import com.fiorano.bc.trgateway.ps.BCComponentPropertySheet;
import com.fiorano.esb.wrapper.CPSESBUtil;
import com.fiorano.util.StringUtil;
import com.fiorano.xml.ClarkName;
import fiorano.esb.adapter.jca.cci.ESBInteractionSpec;
import fiorano.esb.adapter.jca.jmx.JCAConfigMBeanManager;
import fiorano.esb.adapter.jmx.JMXUtil;
import fiorano.esb.common.ESBException;
import fiorano.esb.record.ESBRecordDefinition;
import fiorano.esb.ui.util.CPSHelperMBean;
import fiorano.esb.utils.RBUtil;
import fiorano.tifosi.common.TifosiException;
import fiorano.tifosi.dmi.application.InputPortInstance;
import fiorano.tifosi.dmi.application.OutputPortInstance;
import fiorano.tifosi.dmi.application.PortInstance;
import fiorano.tifosi.dmi.application.ServiceInstance;
import fiorano.tifosi.dmi.service.RuntimeArgument;
import fiorano.tifosi.dmi.service.Schema;
import fiorano.tifosi.tps.rtl.TifosiCustomPropertySheet;
import org.openide.WizardDescriptor;

import javax.management.MBeanServer;
import java.io.File;
import java.util.*;


/**
 * Component Property Sheet for all ESB Components
 *
 * @author FSIPL
 * @version 1.0
 * @created April 13, 2005
 */
public class ESBComponentPropertySheet
        extends TifosiCustomPropertySheet {

    public final static String BPEL_PROCESS_FILE = "BpelProcessFile";
    private final static String EXCEPTION_PORTNAME = "ON_EXCEPTION";
    private static String IN_PORT = "IN_PORT";
    private static String OUT_PORT = "OUT_PORT";

    // Component Property Sheet
    private BCComponentPropertySheet m_componentPropertySheet;
    private boolean storeImportedSchemas = false;
    private boolean importedSchemasRequiredAtRuntime = false;

    /**
     */
    public ESBComponentPropertySheet() {
        m_componentPropertySheet = new BCComponentPropertySheet();
        System.setProperty("COMPONENT_CPS", "false");
    }

    protected ESBComponentPropertySheet(BCComponentPropertySheet componentPropertySheet) {
        this.m_componentPropertySheet = componentPropertySheet;
    }

    /**
     * Show Tifosi Custom Property Sheet
     *
     * @param isRunning
     * @param isReadOnly
     * @throws TifosiException
     */
    public void show(boolean isRunning, boolean isReadOnly)
            throws TifosiException {
        Configuration model = null;

        Object result = WizardDescriptor.CANCEL_OPTION;

        // This boolean is used by BC Component property sheet to hide
        // composite component CPS
        //
        boolean isBPELProcess = isBPELProcess(getServiceInstance());

        m_componentPropertySheet.setBPELProcess(isBPELProcess);
        if (getRepositoryName() != null)
            m_componentPropertySheet.setServiceRepository(getRepositoryName());
        m_componentPropertySheet.setDependencies(getDependencies());
        m_componentPropertySheet.setInFEPOJVM(isInFEPOJVM());

        try {
            // Load Configuration
            _loadConfiguration();
            // START - 7623
            m_componentPropertySheet.setLaunchMode(BCComponentPropertySheet.FEPO);
            // END - 7623

            RuntimeArgument arg = getServiceInstance().getRuntimeArgument(JMXUtil.TYPE_INTERACTION_SPEC);
            if (arg == null || StringUtil.isEmpty(arg.getValueAsString()))
                arg = getServiceInstance().getRuntimeArgument(JMXUtil.TYPE_ACTIVATION_SPEC);

            if (arg != null) {
                m_componentPropertySheet.setSpecType(arg.getName());
                m_componentPropertySheet.setSpecClassName(arg.getValueAsString());
            }
            m_componentPropertySheet.setComponentGUID(getServiceInstance().getGUID());
            m_componentPropertySheet.setComponentInstanceName(getServiceInstanceName());
            m_componentPropertySheet.setAppName(getAppName());
            m_componentPropertySheet.setIconFileName(getIconFileName());
            m_componentPropertySheet.setComponentVersion("" + getServiceInstance().getVersion());

            //set the path of the fesb-ra.xml from which component properties are to be loaded.
            //In FEPO in the cache directory we are download the reasources (including fesb-ra.xml)
            //in <ComponentGUID>/<ComponentVersion> directory
            //so assuming the the fepo's cache/<ComponentGUID>/<ComponentVersion> dir is the user.dir we are setting
            // System.getProperty(user.dir)/fesb-ra.xml as the FesbRaFile.
            m_componentPropertySheet.setFesbRaFile(new File(System.getProperty("user.dir"), "fesb-ra.xml"));


            String title = getServiceInstanceName() + " [" + getServiceInstance().getGUID() + ":" + getServiceInstance().getVersion() + "] - Configuration";

            title = title + (isReadOnly ? " (Read Only)"
                    : (isRunning ? " (Running)" : ""));
            m_componentPropertySheet.setTitle(title);

            m_componentPropertySheet.init();
            applyManageableProperties(m_componentPropertySheet.getModel());

            registerCPSHelper();
            registerCPSESBUtil(isRunning, isReadOnly);

            m_componentPropertySheet.getModel().applyPasswordEncLogger();
            if (getPsValue() != null) {
                m_componentPropertySheet.getModel().afterDeSerialization();
            }

            result = m_componentPropertySheet.show(isReadOnly);

            //If Finish button is not pressed then cancel the configuration
            boolean isFinished = (result == WizardDescriptor.FINISH_OPTION || (result.getClass().getName().indexOf("FinishAction") != -1));

            if (!isFinished) {
                //FEPOJVM - inside if
                if (isInFEPOJVM()) {
                    m_componentPropertySheet.disposePS();
                    unregisterCPS();
                    //FEPOJVM if ends
                }
                setPsValue(null);
                return;
            }
        } catch (TifosiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TifosiException(ex.getMessage(), ex);
        }


        // Serialize configuration
        String psValue = null;

        model = m_componentPropertySheet.getModel();

        System.setProperty("COMPONENT_CPS", "true");

        ServiceInstance serviceInstance = getServiceInstance();
        if (serviceInstance == null || m_componentPropertySheet.isBPELProcess()) {
            //FEPOJVM inside if
            if (isInFEPOJVM()) {
                m_componentPropertySheet.disposePS();
                unregisterCPS();
                //FEPOJVM if ends
            }
            return;
        }

        try {
            ESBInteractionSpec iSpec = m_componentPropertySheet.getModel().getBCDKConfigurationInfo()
                    .getJcaAdapterConfigObject().getInteractionSpec();
            storeImportedSchemas = iSpec.isStoreImportedSchemas();

            importedSchemasRequiredAtRuntime = m_componentPropertySheet.getModel().getBCDKConfigurationInfo()
                    .getJcaAdapterConfigObject().getInteractionSpec().getValidateInputRecord();
        } catch (Exception e) {
        }

        if (!m_componentPropertySheet.getModel().getBCDKConfigurationInfo().getJcaAdapterConfigObject().isInbound()) {
            //generate In Port XSD
            try {
                ESBRecordDefinition inPortRecordDef = m_componentPropertySheet.getInPortRecordDefinition();

                addInPortRecordDefinition(inPortRecordDef);
            } catch (ESBException ex) {
                ex.printCompleteStackTrace();
            }
        } else {
            //remove the existing input port.
            removeInputPort(IN_PORT);
        }

        //generate Out Port XSD
        try {
            ESBRecordDefinition outPortXSD = m_componentPropertySheet.getOutPortRecordDefinition();

            addOutPortRecordDefinition(outPortXSD);
        } catch (ESBException ex) {
            ex.printCompleteStackTrace();
        }
        try {
            ESBRecordDefinition errPortXSD = m_componentPropertySheet.getErrPortRecordDefinition();

            addErrPortRecordDefinition(errPortXSD);
        } catch (ESBException ex) {
            ex.printCompleteStackTrace();
        }

        if (model != null) {
            try {
                model.beforeSerialization();
                populateResources(model);
                populateManageableProperties(model);
                psValue = model.toXML();
            } catch (Exception ex) {
                throw new TifosiException(RBUtil.getMessage(Bundle.class, Bundle.ERROR_SERIALIZING), ex);
            }
        }
        setPsValue(psValue);
        System.getProperties().remove("COMPONENT_CPS");
        //FEPOJVM inside if
        if (isInFEPOJVM()) {
            m_componentPropertySheet.disposePS();
            unregisterCPS();
            //FEPOJVM if ends
        }

    }

    private String getAppName() {
        if (getApplicationPropertySheet() == null)
            return null;
        return getApplicationPropertySheet().getGUID();
    }

    private boolean isBPELProcess(ServiceInstance serviceInst) {
        if (serviceInst == null)
            return false;

        RuntimeArgument arg = serviceInst.getRuntimeArgument(BPEL_PROCESS_FILE);
        return arg != null && !StringUtil.isEmpty(arg.getValueAsString());
    }

    private void unregisterCPS()
            throws TifosiException {
        try {
            unregisterCPSHelper();
            unregisterCPSESBUtil();
        } catch (Exception ex) {
            throw new TifosiException(ex.getMessage(), ex);
        }

    }

    private void registerCPSHelper()
            throws Exception {
        JCAConfigMBeanManager configMbeanManager =
                m_componentPropertySheet.getJCAConfigMBeanManager();

        CPSHelperMBean.registerCPSHelperMBean(configMbeanManager.getMBeanServer(), this);
    }

    private void registerCPSESBUtil(boolean isRunning, boolean isReadOnly) throws Exception {
        CPSESBUtil cpsesbUtil = getCPSESBUtil(isRunning, isReadOnly);
        JCAConfigMBeanManager configMbeanManager = m_componentPropertySheet.getJCAConfigMBeanManager();
        MBeanServer mbeanServer = configMbeanManager.getMBeanServer();
        CPSESBUtil.registerCPSESBUtil(mbeanServer, cpsesbUtil);
    }

    private CPSESBUtil getCPSESBUtil(boolean isRunning, boolean isReadOnly) {

        CPSESBUtil cpsesbUtil = new CPSESBUtil();

        cpsesbUtil.setApplication(getApplicationPropertySheet());
        cpsesbUtil.setServiceInstance(getServiceInstance());
        cpsesbUtil.setSerializedConfiguration(getPsValue());
        cpsesbUtil.setReadOnly(isReadOnly);
        if (cpsesbUtil.getServiceInstanceAdapter() != null) {
            cpsesbUtil.getServiceInstanceAdapter().setRunning(isRunning);
        }
        String iconFileName = getIconFileName();
        cpsesbUtil.setIconLocation(iconFileName != null ? new File(iconFileName) : null);
        cpsesbUtil.setCpsLaunchedInMemory(isInFEPOJVM());

        return cpsesbUtil;
    }


    private void unregisterCPSHelper()
            throws Exception {
        m_componentPropertySheet.unregisterMBean();

        JCAConfigMBeanManager configMbeanManager =
                m_componentPropertySheet.getJCAConfigMBeanManager();

        CPSHelperMBean.unregisterCPSHelperMBean(configMbeanManager.getMBeanServer());
    }

    private void unregisterCPSESBUtil() throws Exception {
        JCAConfigMBeanManager configMbeanManager = m_componentPropertySheet.getJCAConfigMBeanManager();
        MBeanServer mbeanServer = configMbeanManager.getMBeanServer();
        CPSESBUtil.unregisterCPSESBUtil(mbeanServer);
    }


    /**
     * Get Configuration Object
     *
     * @throws Exception
     */
    private void _loadConfiguration()
            throws Exception {
        Configuration configuration = new Configuration();

        String psValue = getPsValue();

        if (psValue != null) {
            configuration.fromXML(psValue);
            m_componentPropertySheet.setLoadExistingConfiguration(true);
        }
        m_componentPropertySheet.setModel(configuration);
    }

    private void addInPortRecordDefinition(ESBRecordDefinition inPortXSD) {
        ServiceInstance serviceInstance = getServiceInstance();
        boolean added = false;

        Enumeration inPorts = Collections.enumeration(serviceInstance.getInputPortInstances());

        while (inPorts.hasMoreElements()) {
            InputPortInstance inPort = (InputPortInstance)
                    inPorts.nextElement();

            if (!inPort.getName().equalsIgnoreCase(IN_PORT))
                continue;

            if (m_componentPropertySheet.isSchedulingEnabled()) {
                inPort.setEnabled(false);
                inPort.setDescription("This Port is Disabled as Scheduling is Switched on");
            } else
                inPort.setEnabled(true);
            setPortSchema(inPort, inPortXSD);
            added = true;
        }

        if (added)
            return;
        InputPortInstance inPort = new InputPortInstance();
        inPort.setName(IN_PORT);
        setPortSchema(inPort, inPortXSD);
        serviceInstance.addInputPortInstance(inPort);
    }

    private void addOutPortRecordDefinition(ESBRecordDefinition outPortXSD) {
        ServiceInstance serviceInstance = getServiceInstance();

        boolean added = false;

        Enumeration outPorts = Collections.enumeration(serviceInstance.getOutputPortInstances());

        while (outPorts.hasMoreElements()) {
            OutputPortInstance outPort = (OutputPortInstance)
                    outPorts.nextElement();

            if (!outPort.getName().equalsIgnoreCase(OUT_PORT))
                continue;

            setPortSchema(outPort, outPortXSD);
            added = true;
        }

        if (added)
            return;
        OutputPortInstance outPort = new OutputPortInstance();

        outPort.setName(OUT_PORT);
        setPortSchema(outPort, outPortXSD);
        serviceInstance.addOutputPortInstance(outPort);
    }

    private void addErrPortRecordDefinition(ESBRecordDefinition errPortXSD) {
        ServiceInstance serviceInstance = getServiceInstance();

        boolean added = false;
        OutputPortInstance errPort = serviceInstance.getOutputPortInstance(EXCEPTION_PORTNAME);

        if (errPort != null) {
            setPortSchema(errPort, errPortXSD);
            added = true;
        }

        if (added)
            return;
        errPort = new OutputPortInstance();
        errPort.setName(EXCEPTION_PORTNAME);
        setPortSchema(errPort, errPortXSD);
        serviceInstance.addOutputPortInstance(errPort);
    }

    private void setPortSchema(PortInstance port, ESBRecordDefinition schemaDefinion) {
        if (port == null)
            return;
        //clear the port structure
        port.setSchema(null);

        if (schemaDefinion != null && !StringUtil.isEmpty(schemaDefinion.getStructure())) {
            Schema schema = new Schema();
            schema.setContent(schemaDefinion.getStructure());
            schema.setType(getStructureType(schemaDefinion));
            if (!StringUtil.isEmpty(schemaDefinion.getRootElementName()))
                schema.setRootElement(ClarkName.toClarkName(schemaDefinion.getTargetNamespace(), schemaDefinion.getRootElementName()));

            if (storeImportedSchemas && schemaDefinion.hasImportedStructures()) {
                m_componentPropertySheet.getModel().addImportedSchemasToRepo(schemaDefinion,
                        getServiceInstance().getName(), importedSchemasRequiredAtRuntime);
                schemaDefinion.getImportedStructures().clear();
                schema.setContent(schemaDefinion.getStructure());
                port.setSchema(schema);
                return;
            }

            Map imports = schemaDefinion.getImportedStructures();
            //use this object to set to the resource
            if (imports != null) {
                Set properties = imports.entrySet();
                Iterator iter;
                if (properties != null && (iter = properties.iterator()) != null) {
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        Object key = entry.getKey();
                        if (!(key instanceof String))
                            continue;
                        Object value = entry.getValue();
                        if (value instanceof List) {
                            for (int index = 0; index < ((List) value).size(); index++) {
                                if (index == 1)
                                    break;
                                Object externalStructure = ((List) value).get(index);
                                if (externalStructure instanceof String)
                                    schema.addSchemaRef((String) key, (String) externalStructure);
                            }
                        } else if (value instanceof String[]) {
                            for (int index = 0; index < ((String[]) value).length; index++) {
                                if (index == 1)
                                    break;
                                Object externalStructure = ((String[]) value)[index];
                                if (externalStructure instanceof String)
                                    schema.addSchemaRef((String) key, (String) externalStructure);
                            }
                        } else if (value instanceof String)
                            schema.addSchemaRef((String) key, (String) value);
                    }
                }
            }
            port.setSchema(schema);
        }
    }

    private void removeInputPort(String portName) {
        ServiceInstance serviceInstance = getServiceInstance();
        InputPortInstance port = serviceInstance.getInputPortInstance(portName);
        if (port != null)
            serviceInstance.removeInputPortInstance(port);
    }

    private int getStructureType(ESBRecordDefinition def) {
        if (def == null || StringUtil.isEmpty(def.getStructure()))
            return -1;
        if (def.getStructure().contains("<!ELEMENT"))
            return Schema.TYPE_DTD;
        else
            return Schema.TYPE_XSD;
    }


}

