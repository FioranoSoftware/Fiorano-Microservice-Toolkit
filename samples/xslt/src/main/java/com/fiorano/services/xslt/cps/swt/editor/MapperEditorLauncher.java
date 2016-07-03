/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xslt.cps.swt.editor;

import com.fiorano.esb.server.api.service.config.editors.AbstractEditorPartLauncher;
import com.fiorano.esb.server.api.service.config.editorsupport.PropertyEnvironment;
import com.fiorano.esb.server.api.service.config.helpers.ConfigurationHelper;
import com.fiorano.esb.server.api.service.config.internal.Activator;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.common.util.ConnectedPortKey;
import com.fiorano.services.xslt.configuration.XsltPM;
import com.fiorano.tools.studio.soa.application.model.config.editorlauncher.IEditorLauncher;
import com.fiorano.tools.studio.soa.application.model.config.editorlauncher.ILauncherInput;
import com.fiorano.tools.studio.soa.application.model.config.editorlauncher.MapperLauncherInput;
import com.fiorano.tools.studio.soa.application.model.config.editorlauncher.MapperLauncherInput.Structure;
import com.fiorano.tools.studio.soa.application.model.config.editorlauncher.MapperLauncherInput.StructureType;
import com.fiorano.tools.utilities.Logger;
import fiorano.esb.record.ESBRecordDefinition;
import xslt.Messages_XSLT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** 
 *
 *@author Sai
 */
public class MapperEditorLauncher extends AbstractEditorPartLauncher<String> {

	private MapperLauncherInput launcherInput;
	
	public MapperEditorLauncher() {
		super("com.fiorano.tools.mapper.eMapper"); //$NON-NLS-1$
	}
	
	@Override
	protected void onEditorClose() {
		setValue(launcherInput.getMapperProjectResource());
	}

	@Override
	protected void onEditorSave() {
		try {
			if (launcherInput.getMetaData() != null) {
				// changing the value of "InputStructures" and "OutputStructure" set the value of strProject to null.
				// resetting the value after making the changes.
				String project = getValue();
				String inputStructuresType = null;
				if (launcherInput.isHasAppContextInInput()) {
					if (launcherInput.getInputStructures().size() > 0) {
						inputStructuresType = XsltPM.CONTEXT_BODY;
					} else {
						inputStructuresType = XsltPM.CONTEXT;
					}
				} else {
					inputStructuresType = XsltPM.BODY;
				}
				getPropertyEnvironment().setPropertyValue("InputStructures", inputStructuresType);

				String outputStructureType = null;
				if (launcherInput.isHasAppContextInOutput()) {
					outputStructureType = XsltPM.CONTEXT;
				} else {
					outputStructureType = XsltPM.BODY;
				}
				getPropertyEnvironment().setPropertyValue("OutputStructure", outputStructureType);

				setValue(project);
				getPropertyEnvironment().setPropertyValue("XSL", launcherInput.getMetaData().length > 0 ? launcherInput.getMetaData()[0] : null); //$NON-NLS-1$
				getPropertyEnvironment().setPropertyValue("JMSMessageXSL", launcherInput.getMetaData().length > 1 ? launcherInput.getMetaData()[1] : null); //$NON-NLS-1$

				Structure inportStructure =  null;
				if (launcherInput.getInputStructures().size() > 0) {
					inportStructure = launcherInput.getInputStructures().get(0);
				}
				getPropertyEnvironment().setPropertyValue("EsbDefInPort", inportStructure == null ? null : getSchema(inportStructure)); //$NON-NLS-1$
				if (launcherInput.getInputStructures().size() > 0) {
					getPropertyEnvironment().setPropertyValue("InputStructureName", launcherInput.getInputStructures().get(0).getName()); //$NON-NLS-1$
				}
				Structure outportStructure = null;
				if (launcherInput.getOutputStructures().size() > 0) {
					if (launcherInput.getOutputStructures().size() == 1) {
						if (launcherInput.getOutputStructures().get(0).getName().equals("JMS-Message"))
							outportStructure = null;
						else
							outportStructure = launcherInput.getOutputStructures().get(0);
					} else
						outportStructure = launcherInput.getOutputStructures().get(0);
				}
				getPropertyEnvironment().setPropertyValue("EsbDefOutPort", outportStructure == null ? null : getSchema(outportStructure)); //$NON-NLS-1$
			}
		} catch (Exception ex) {
			Logger.logException(Activator.PLUGIN_ID, Messages_XSLT.MapperEditorLauncher_6, ex);
		}
	}

	private boolean hasAppContextStructure(List<Structure> structureList) {
		for (Structure structure : structureList) {
			if ("Application-Context".equals(structure.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected ILauncherInput getLauncherInput() {
		launcherInput = new MapperLauncherInput();
		PropertyEnvironment propertyEnv = getPropertyEnvironment();
		try {

			String inputStructuresStr = (String) propertyEnv.getPropertyValue("InputStructures"); //$NON-NLS-1$
			String outputStructureStr = (String) propertyEnv.getPropertyValue("OutputStructure"); //$NON-NLS-1$
			launcherInput.setAppContextInInput(inputStructuresStr.indexOf(XsltPM.CONTEXT) != -1);
			launcherInput.setAppContextInOutput(outputStructureStr.indexOf(XsltPM.CONTEXT) != -1);
			ISchema appContextSchema = getPropertyEnvironment().getConfigurationHelper().fetchApplicationContext();
			Structure appContextStructure = null;
			if (appContextSchema != null) {
				appContextStructure = getStructure(appContextSchema);
				appContextStructure.setName("Application-Context"); //$NON-NLS-1$
			}
			launcherInput.setAppContextSchema(appContextStructure);
			launcherInput.setMaxInputStructures(2);
			launcherInput.setMaxOutputStructures(2);
			List<Structure> inputStructures = new ArrayList<Structure>(1);
			fillStructures(inputStructures, inputStructuresStr, true);
			launcherInput.setInputStructures(inputStructures);
			List<Structure> outputStructures = new ArrayList<Structure>(1);
			fillStructures(outputStructures, outputStructureStr, false);
			launcherInput.setOutputStructures(outputStructures);
			List<Structure> allowedInputStructures = new ArrayList<Structure>(2);
			if (appContextStructure != null) {
				allowedInputStructures.add(appContextStructure);
			} else {
				Structure dummyStructure = launcherInput.new Structure();
				dummyStructure.setName("Application-Context");
				allowedInputStructures.add(dummyStructure);
			}
			Structure inportStructure = getConnectedPortStructrue(true);
			if (inportStructure != null) {
				inportStructure.setName("IN_PORT");
				allowedInputStructures.add(inportStructure);
			}
			launcherInput.setAllowedInputStructures(allowedInputStructures);
			List<Structure> allowedOutputStructures = new ArrayList<Structure>(2);
			if (appContextStructure != null) {
				allowedOutputStructures.add(appContextStructure);
			} else {
				Structure dummyStructure = launcherInput.new Structure();
				dummyStructure.setName("Application-Context");
				allowedOutputStructures.add(dummyStructure);
			}
			Structure outportStructure = getConnectedPortStructrue(false);
			if (outportStructure != null) {
				outportStructure.setName("OUT_PORT");
				allowedOutputStructures.add(outportStructure);
			}
			launcherInput.setAllowedOutputStructures(allowedOutputStructures);
			launcherInput.setMapperProjectResource(value);
			return launcherInput;
		} catch (Exception e) {
			Logger.logException(getClass().getPackage().getName(), e);
			return null;
		}
	}

	private void fillStructures(List<Structure> structures, String structureStr, boolean isInput) throws Exception {
		if (structureStr.indexOf(XsltPM.BODY) != -1) {
			Structure portStructure = getConnectedPortStructrue(isInput);
			if (portStructure != null) {
				portStructure.setName(isInput ? "IN_PORT" : "OUT_PORT"); //$NON-NLS-1$ //$NON-NLS-2$
				structures.add(portStructure);
			}
		}
	}

	private Structure getConnectedPortStructrue(boolean isInput) throws Exception {
		Structure portStructure = null;
		ConfigurationHelper configurationHelper = getPropertyEnvironment().getConfigurationHelper();
		Map<ConnectedPortKey, ISchema> connectedPortSchemas = isInput ? configurationHelper.fetchSchemasConnectedToInputPorts() : configurationHelper.fetchSchemasConnectedToOutputPorts();
		if (connectedPortSchemas != null) {
			for (ISchema schema : connectedPortSchemas.values()) {
				portStructure = getStructure(schema);
			}
		}
		return portStructure;
	}
 
	@SuppressWarnings("unchecked")
	private Structure getStructure(ISchema schema) {
		Structure structure = launcherInput.new Structure();
		structure.setStructureType(schema.getDefinitionType() == ISchema.DTD ? StructureType.DTD : StructureType.XSD);
		structure.setRootElementName(schema.getRootElementName());
		structure.setTargetNamespace(schema.getTargetNamespace());
		structure.setStructure(schema.getStructure());
		Map importedStructures = schema.getImportedStructures();
		if (importedStructures != null) {
			for (Object namespace : importedStructures.keySet()) {
				String extContent = null;
				Object content = importedStructures.get(namespace);
				if (content instanceof String) {
					extContent = (String) content;
				} else if (content instanceof List && !((List) content).isEmpty()) {
					extContent = (String) ((List) content).get(0);
				} else if (content instanceof String[] && !(((String[]) content).length != 0)) {
					extContent = ((String[]) content)[0];
				}
				if (extContent != null)
					structure.addImportedStructure((String)namespace, extContent);
			}
		}
		return structure;
	}

	private ISchema getSchema(Structure structure) {
		ISchema schema = new ESBRecordDefinition();
		schema.setDefinitionType(structure.getStructureType() == StructureType.DTD ? ISchema.DTD : ISchema.XSD);
		schema.setRootElementName(structure.getRootElementName());
		schema.setTargetNamespace(structure.getTargetNamespace());
		schema.setStructure(structure.getStructure());
		Map<String, String> importedStructures = structure.getImportedStructures();
		if (importedStructures != null) {
			for (Entry<String, String> entry : importedStructures.entrySet()) {
				schema.addImportedStructure(entry.getKey(), entry.getValue());
			}
		}
		return schema;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initializeLauncher(IEditorLauncher editorLauncher) {
		
	}
}
