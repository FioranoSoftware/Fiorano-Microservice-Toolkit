/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.xslt.cps;

import com.fiorano.esb.server.api.service.config.wizard.ConfigWizardWithEditorLauncher;
import com.fiorano.services.common.service.ISchema;
import com.fiorano.services.xslt.configuration.XsltConfigurationSerializer;
import com.fiorano.services.xslt.configuration.XsltPM;
import com.fiorano.tools.studio.soa.application.model.PortInstanceType;
import com.fiorano.tools.utilities.FileUtil;
import com.fiorano.tools.utilities.Logger;
import com.fiorano.util.StringUtil;
import fiorano.esb.record.ESBRecordDefinition;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import xslt.Activator;
import xslt.Messages_XSLT;

import java.io.*;

/** 
 *
 *@author Sai
 */
public class ConfigLauncher extends ConfigWizardWithEditorLauncher {

	private String prevProjectValue;
	
	public ConfigLauncher() {
		super(XsltPM.class);
	}
	
	@Override
	public boolean performFinish() {
		XsltPM configuration = (XsltPM) getConfiguration();
		String currentProjectValue = configuration.getProject();
		IFolder transformationFolder = configurationHelper.getComponentTransformationsFolder();

		try {
			if (!StringUtil.isEmpty(prevProjectValue) && isNonXML(prevProjectValue)) {
				IFolder projectFolder = transformationFolder.getFolder(prevProjectValue);
				projectFolder.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				if (projectFolder.exists() && projectFolder.getFile(prevProjectValue + ".fmp").exists()) { //$NON-NLS-1$
					saveContentToHistory(projectFolder.getFile(prevProjectValue + ".fmp")); //$NON-NLS-1$
					IFolder schemaFolder = projectFolder.getFolder(new Path("/resources/schemas/")); //$NON-NLS-1$
					if (schemaFolder.exists()) {
						for(IResource resource : schemaFolder.members()) {
							if (resource.getType() == IResource.FILE) {
								IFile schemaFile = (IFile) resource;
								saveContentToHistory(schemaFile);
							}
						}
					}
				}
			}

			if (!StringUtil.isEmpty(currentProjectValue) && isNonXML(currentProjectValue)) {
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IProject transformationProject = workspaceRoot.getProject(currentProjectValue);
				if (transformationProject.exists()) {
					IFile fmpFile = null;
					for (IResource child : transformationProject.members()){
						if (child.getType() == IResource.FILE && child.getName().endsWith(".fmp")) { //$NON-NLS-1$
							fmpFile = (IFile) child;
							break;
						}
					}

					if (fmpFile != null) {
						currentProjectValue = fmpFile.getName().substring(0, fmpFile.getName().indexOf(".fmp")); //$NON-NLS-1$
						IFolder projectFolder = transformationFolder.getFolder(currentProjectValue);
						copyFilesInFolder(projectFolder, transformationProject);
						copyFilesInFolder(projectFolder.getFolder(new Path("/resources/schemas/")), transformationProject.getFolder(new Path("/resources/schemas/")));
						transformationFolder.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
						transformationProject.delete(true, null);
						configuration.setProject(currentProjectValue);
					}
				}
			}
		} catch (Exception e) {
			Logger.logException(Activator.PLUGIN_ID, Messages_XSLT.ConfigLauncher_5, e); //$NON-NLS-1$
			return false;
		}
		
		try {
		PortInstanceType inputPort = configurationHelper.getServiceInstanceType().getPort("IN_PORT"); //$NON-NLS-1$
		configurationHelper.getPortSchemaHelper().setSchema(inputPort, configuration.getEsbDefInPort());
		PortInstanceType outputPort = configurationHelper.getServiceInstanceType().getPort("OUT_PORT"); //$NON-NLS-1$
		ISchema outPortSchema = XsltPM.CONTEXT.equals(configuration.getOutputStructure()) ? configuration.getEsbDefInPort() : configuration.getEsbDefOutPort();
		configurationHelper.getPortSchemaHelper().setSchema(outputPort, outPortSchema);
		} catch (Exception e) {
			Logger.logException(Activator.PLUGIN_ID, Messages_XSLT.ConfigLauncher_8, e); //$NON-NLS-1$
			return false;
		}
		return super.performFinish();
	}

	private void saveContentToHistory(IFile file) throws IOException,
			FileNotFoundException, CoreException {
		File schemaFile = new File(file.getLocationURI());
		String fileContents = FileUtil.readFile(schemaFile);
		InputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(fileContents.getBytes());
			file.setContents(inputStream, true, true, null);
		} finally {
			inputStream.close();
		}
	}
	
	private void copyFilesInFolder(IFolder destinationFolder, IContainer sourceFolder) throws CoreException {
		if (destinationFolder.exists()) {
			IResource[] members = destinationFolder.members();
			for (IResource child : members) {
				if (child instanceof IFile && !child.getName().startsWith(".")) { //$NON-NLS-1$
					IFile currentFile = (IFile) child;
					IFile newFile = sourceFolder.getFile(new Path(child.getName()));
					if (newFile.exists()) {
						InputStream newContents = null;
						try {
							newContents = newFile.getContents();
							currentFile.setContents(newContents, IResource.NONE, null);
						} catch (CoreException e) {
							Logger.logException(Activator.PLUGIN_ID, e);
						} finally {
							try {
								if (newContents != null)
									newContents.close();
							} catch (IOException e) {
								newContents = null;
							}
						}
					} else {
						if (currentFile.exists())
							currentFile.delete(true, null);
					}
				}
			}
		} else if(!destinationFolder.exists()){
			createFolder(destinationFolder);
		}

		if(sourceFolder.exists()) {
			for (IResource sourceFile : sourceFolder.members()) {
				if (sourceFile instanceof IFile && !sourceFile.getName().startsWith(".")) { //$NON-NLS-1$
					IFile file = destinationFolder.getFile(sourceFile.getName());
					if (!file.exists()) {
						InputStream source = null;
						try {
							source = ((IFile) sourceFile).getContents();
							file.create(source, true, null);
						} finally {
							try {
								if (source != null)
									source.close();
							} catch (IOException e) {
								source = null;
							}
						}
					}
				}
			}
		}
		destinationFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
    private void createFolder(IFolder folder) throws CoreException {
        IContainer container = folder.getParent();
        if (!container.exists()) {
            createFolder((IFolder) container);
        }
        folder.create(true, true, null);
    }
    
    private boolean isNonXML(String string) {
		// performs a quick check to see if the string is enclosed in angle brackets.
		// Though this does not guarantee that the string is not an xml, 
		// it is sufficient here as the project string is either a valid xml or a project name.
		return !StringUtil.isEmpty(string) && !(string.startsWith("<") && string.endsWith(">")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void readConfig() throws Exception {
		XsltConfigurationSerializer serializer = new XsltConfigurationSerializer();
		configuration = serializer.deserializeFromString(readConfigString());
		try {
			PortInstanceType inputPort = configurationHelper.getServiceInstanceType().getPort("IN_PORT"); //$NON-NLS-1$
			((XsltPM)configuration).setEsbDefInPort((ESBRecordDefinition)(configurationHelper.getPortSchemaHelper().getSchema(inputPort)));
			PortInstanceType outputPort = configurationHelper.getServiceInstanceType().getPort("OUT_PORT"); //$NON-NLS-1$
			((XsltPM)configuration).setEsbDefOutPort((ESBRecordDefinition)(configurationHelper.getPortSchemaHelper().getSchema(outputPort)));
		} catch (Exception e) {
			Logger.logException(Activator.PLUGIN_ID, "Failed to read Port schemas", e); //$NON-NLS-1$
		}
	}
	
	@Override
	public boolean performCancel() {
		XsltPM configuration = (XsltPM) getConfiguration();
		String projectStr = configuration.getProject();
		if (!StringUtil.isEmpty(projectStr) && !(projectStr.startsWith("<") && projectStr.endsWith(">"))) { //$NON-NLS-1$ //$NON-NLS-2$
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject transformationProject = workspaceRoot.getProject(projectStr);
			if (transformationProject.exists()) {
				try {
					transformationProject.delete(true, null);
				} catch (CoreException e) {
					Logger.logException(getClass().getPackage().getName(), e);
				}
			}
		}
		return super.performCancel();
	}
	
	@Override
	protected void initConfig() throws Exception {
		super.initConfig();
		prevProjectValue = ((XsltPM)getConfiguration()).getProject();
	}
}
