/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.fiorano.edbc.framework.service.cps.steps;

import com.fiorano.edbc.framework.service.cps.Bundle;
import com.fiorano.openide.wizard.WizardStep;
import com.fiorano.services.common.service.CollectingErrorListener;
import com.fiorano.services.common.swing.ConfigurationPanel;
import com.fiorano.services.common.swing.Notifier;
import com.fiorano.services.common.util.RBUtil;
import org.openide.WizardValidationException;

import java.awt.*;
import java.util.Collection;

/**
 * @author Fiorano Software Technologies Pvt. Ltd.
 */
public abstract class ConfigurationStep<K> extends WizardStep implements NotifiableStep {

    private final CollectingErrorListener errorListener = new CollectingErrorListener();
    private ConfigurationPanel<K> panel;
    private boolean readOnly;
    private Notifier notifier;

    public ConfigurationStep(boolean readOnly, ConfigurationPanel<K> panel) {
        this.readOnly = readOnly;
        this.panel = panel;
    }

    protected final Component createComponent() {
        return panel;
    }

    protected void model2Component() throws Exception {
        panel.loadConfiguration(fetchConfigurationToLoad());
    }

    protected void component2Model() {
        updateConfiguration(panel.getConfiguration());
    }

    public void fastValidate() throws WizardValidationException {
        if (readOnly) {
            throw new WizardValidationException(panel, RBUtil.getMessage(Bundle.class, Bundle.READ_ONLY_WIZARD), null);
        }
    }

    public void lazyValidate() throws WizardValidationException {
        try {
            validateData();
        } catch (Exception ex) {
            throw new WizardValidationException(panel, RBUtil.getMessage(Bundle.class, Bundle.CONFIG_INVALID), null);
        }
    }

    protected abstract K fetchConfigurationToLoad();

    protected abstract void updateConfiguration(K configuration);

    public void validateData() throws Exception {
        errorListener.reset();
        panel.validate(errorListener);
        if (errorListener.getCollectedExceptions().size() > 0 && notifier != null) {
            for (Exception exception : (Collection<Exception>) errorListener.getCollectedExceptions()) {
                notifier.notify(Notifier.NotificationType.EXCEPTION, null, exception);
            }

            throw errorListener.getCollectedExceptions();
        }
    }

    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }
}
