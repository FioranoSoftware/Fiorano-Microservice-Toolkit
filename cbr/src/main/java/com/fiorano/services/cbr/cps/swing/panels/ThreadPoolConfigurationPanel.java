/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.fiorano.services.cbr.cps.swing.panels;

/**
 * Created by pankaj on 7/21/15.
 */

import com.fiorano.cbr.model.CBRPropertyModel;
import com.fiorano.services.cbr.cps.swing.Bundle;
import com.fiorano.services.common.util.RBUtil;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ThreadPoolConfigurationPanel extends JPanel implements EnhancedCustomPropertyEditor {
    public static final String[] UNITS = {"milli seconds", "seconds", "minutes", "hours", "days"};
    private JCheckBox enableThreadpool = new JCheckBox(RBUtil.getMessage(Bundle.class, Bundle.ENABLE_THREAD_POOL));
    private JLabel poolsize = new JLabel(RBUtil.getMessage(Bundle.class, Bundle.POOL_SIZE));
    private SpinnerNumberModel intervalModel = new SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(Integer.MAX_VALUE), Integer.valueOf(1));
    private JSpinner intervalSpinner = new JSpinner(intervalModel);
    private JLabel batchEvictionInterval = new JLabel(RBUtil.getMessage(Bundle.class, Bundle.BATCH_EVICTION_INTERVAL));
    private SpinnerNumberModel intervalModel1 = new SpinnerNumberModel(Long.valueOf(1), Long.valueOf(1), Long.valueOf(Long.MAX_VALUE), Long.valueOf(1));
    private JSpinner intervalSpinner1 = new JSpinner(intervalModel1);
    private JComboBox intervalUnitCombo = new JComboBox(ThreadPoolConfigurationPanel.UNITS);
    private CBRPropertyModel configuration;

    public ThreadPoolConfigurationPanel(CBRPropertyModel cbrPropertyModel) {
        super(new BorderLayout());
        createUI();
        loadConfiguration(cbrPropertyModel);

    }

    private void createUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(enableThreadpool, new GridBagConstraints(0, 0,
                3, 1,
                0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 5, 0),
                0, 0));
        panel.add(poolsize, new GridBagConstraints(0, 1,
                1, 1,
                0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 20, 0, 5),
                0, 0));
        panel.add(intervalSpinner, new GridBagConstraints(1, 1,
                1, 1,
                1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 5),
                0, 0));


        panel.add(batchEvictionInterval, new GridBagConstraints(0, 2,
                1, 1,
                0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 20, 0, 5),
                0, 0));
        panel.add(intervalSpinner1, new GridBagConstraints(1, 2,
                1, 1,
                1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 5),
                0, 0));

        panel.add(intervalUnitCombo, new GridBagConstraints(2, 2,
                1, 1,
                0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0),
                0, 0));


        add(panel, BorderLayout.NORTH);
        enableThreadpool.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean threadpoolenabled = enableThreadpool.isSelected();
                intervalUnitCombo.setEnabled(threadpoolenabled);
                intervalSpinner.setEnabled(threadpoolenabled);
                intervalSpinner1.setEnabled(threadpoolenabled);
            }
        });
        enableThreadpool.setSelected(false);
        enableThreadpool.setSelected(true);
    }


    public void loadConfiguration(CBRPropertyModel configuration) {
        if (configuration != null) {
            this.configuration = configuration;
            enableThreadpool.setSelected(configuration.isEnableThreadPool());
            intervalModel.setValue(configuration.getPoolSize());
            intervalModel1.setValue((configuration.getBatchEvictionInterval()));
            intervalUnitCombo.setSelectedIndex(configuration.getUnit());
        }
    }

    public CBRPropertyModel getConfiguration() {
        if (configuration != null) {
            configuration.setEnableThreadPool(enableThreadpool.isSelected());
            configuration.setPoolSize((Integer) intervalModel.getValue());
            configuration.setBatchEvictionInterval((Long) intervalModel1.getValue());
            configuration.setUnit(intervalUnitCombo.getSelectedIndex());
        }
        return configuration;
    }

    @Override
    public Object getPropertyValue() throws IllegalStateException {
        return null;
    }
}
