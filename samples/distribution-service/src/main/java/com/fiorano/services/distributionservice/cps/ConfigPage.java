/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.services.distributionservice.cps;

import com.fiorano.services.common.monitor.configuration.MonitoringConfiguration;
import com.fiorano.services.cps.ui.MonitoringConfigurationUI;
import com.fiorano.services.cps.ui.TableContentProvider;
import com.fiorano.services.cps.ui.TableLabelProvider;
import com.fiorano.services.distributionservice.DistributionServiceConstants;
import com.fiorano.services.distributionservice.configuration.DistributionServicePM;
import distributionservice.Messages_Distribution;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Prakash G.R.
 * 
 */
public class ConfigPage extends WizardPage {

    private static final String WEIGHT = Messages_Distribution.ConfigDialog_0;
    private static final String PORT_NAME = Messages_Distribution.ConfigDialog_1;

    private TableViewer tableViewer;
    private List<Data> model = new ArrayList<Data>();

    private Spinner portSpinner;
    private Button propagateSchemaButton;
    private MonitoringConfigurationUI monitoringConfigurationUI;
	private DistributionServicePM servicePM;

    public ConfigPage(DistributionServicePM servicePM) {
    	super("DistributionServiceConfigPage"); //$NON-NLS-1$
        this.servicePM = servicePM;
        setTitle(Messages_Distribution.ConfigDialog_3);
    }

    @Override
    public void createControl(Composite parent) {

    	Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createTable(composite);
        createMonitorUI(composite);
        setMessage(Messages_Distribution.ConfigDialog_4);
        
        setControl(composite);
        
        loadConfiguration();
    }
    
    private void loadConfiguration() {
		
    	portSpinner.setSelection(servicePM.getPortCount());
        propagateSchemaButton.setSelection(servicePM.isPropagateSchema());
        int[] portWeights = servicePM.getPortWeights();
        int j = servicePM.getPortWeights().length;
        for (int i = 0; i < servicePM.getPortCount(); i++) {
        	if (i < j) {
        		model.add(new Data("OUT_PORT_" + i, portWeights[i])); //$NON-NLS-1$
            } else {
            	model.add(new Data("OUT_PORT_" + i, 1)); //$NON-NLS-1$
            }
        }
        
        tableViewer.refresh();

        monitoringConfigurationUI.setValue(servicePM.getMonitoringConfiguration());
	}

	private void createMonitorUI(Composite parent) {
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(1,false));
        monitoringConfigurationUI = new MonitoringConfigurationUI(composite,null);
      }

    private void createTable(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        composite.setLayout(layout);

        Label propagateSchemaLabel = new Label(composite, SWT.NONE);
        propagateSchemaLabel.setText(Messages_Distribution.ConfigDialog_8);

        propagateSchemaButton = new Button(composite, SWT.CHECK);
		GridData propagateSchemaButtonData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		propagateSchemaButton.setLayoutData(propagateSchemaButtonData);

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages_Distribution.ConfigDialog_6);

        portSpinner = new Spinner(composite, SWT.BORDER);

        Table table = new Table(composite, SWT.FULL_SELECTION | SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4);
        tableData.heightHint = 150;
        table.setLayoutData(tableData);

        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnWeightData(1, 40));
        tableLayout.addColumnData(new ColumnWeightData(1, 60));
        table.setLayout(tableLayout);

        TableColumn Column1 = new TableColumn(table, SWT.NONE);
        Column1.setText(PORT_NAME);

        TableColumn Column2 = new TableColumn(table, SWT.LEFT);
        Column2.setText(WEIGHT);

        portSpinner.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {

                portSpinner.setMinimum(1);
                if (portSpinner.getSelection() > model.size()) {

                    int numPorts = portSpinner.getSelection() - model.size();

                    for (int j = 0; j < numPorts; j++) {

                        model.add(new Data(DistributionServiceConstants.OUT_PORT_PREFIX
                                + (model.size()), 1));
                    }
                } else if (portSpinner.getSelection() < model.size()) {

                    int numPorts = model.size() - portSpinner.getSelection();
                    for (int j = 0; j < numPorts; j++) {
                        model.remove(model.size() - 1);
                    }

                }
                tableViewer.refresh();
            }
        });

        createViewer(table);
    }

    private void createViewer(Table table) {

        tableViewer = new TableViewer(table);

        tableViewer.setContentProvider(new TableContentProvider() {
        	
        	public Object[] getElements(Object inputElement) {
        		return ((List<?>) inputElement).toArray();
        	}
        });
        tableViewer.setLabelProvider(new TableLabelProvider() {
        	
        	public String getColumnText(Object element, int columnIndex) {
        		Data data = (Data) element;
        		return columnIndex == 0 ? data.getPort() : Integer.toString(data.getWeight());
        	}
        });
        tableViewer.setCellModifier(getCellModifier());
        tableViewer.setCellEditors(getCellEditor());
        tableViewer.setColumnProperties(new String[] { PORT_NAME, WEIGHT });

        tableViewer.setInput(model);
        portSpinner.setValues(model != null ? model.size() : 0, 0, 100, 0, 1, 5);
    }

    private CellEditor[] getCellEditor() {
        return new CellEditor[] { new TextCellEditor(tableViewer.getTable()),
                new TextCellEditor(tableViewer.getTable()) };
    }

    private ICellModifier getCellModifier() {

        return new ICellModifier() {

            public boolean canModify(Object element, String property) {
                if (property.equals(PORT_NAME))
                    return false;
                else
                    return true;
            }

            public Object getValue(Object element, String property) {
                Data data = (Data) element;
                if (property.equals(PORT_NAME))
                    return data.getPort();
                else {
                    String weight = Integer.toString(data.getWeight());
                    return weight;
                }
            }

            public void modify(Object element, String property, Object value) {

                TableItem item = (TableItem) element;

                Data data = (Data) item.getData();

                if (property.equals(PORT_NAME))
                    data.setPort((String) value);
                else {
                    try {
                        data.setWeight(Integer.parseInt(value.toString()));
                    } catch (NumberFormatException nfe) {
                        // ignore to undo the change
                    }
                }
                tableViewer.refresh();
            }

        };
    }
    
    public void updateConfiguration() {

    	servicePM.setPortCount(portSpinner.getSelection());
        servicePM.setPropagateSchema(propagateSchemaButton.getSelection());
    	servicePM.setMonitoringConfiguration((MonitoringConfiguration) monitoringConfigurationUI.getPropertyValue());

    	int portCount = servicePM.getPortCount();
    	int[] aIWeightOnPorts = new int[portCount];

    	for (int i = 0; i < portCount; ++i) {
    		Data data = model.get(i);
    		aIWeightOnPorts[i] = data.getWeight();
    	}
    	servicePM.setPortWeights(aIWeightOnPorts);
    }
    
    private class Data {

    	private String port;
    	private int weight;

    	public Data(String port, int weight) {
    		this.port = port;
    		this.weight = weight;
    	}

    	public String getPort() {
    		return port;
    	}

    	public void setPort(String port) {
    		this.port = port;
    	}

    	public int getWeight() {
    		return weight;
    	}

    	public void setWeight(int weight) {
    		this.weight = weight;
    	}
    }
}
