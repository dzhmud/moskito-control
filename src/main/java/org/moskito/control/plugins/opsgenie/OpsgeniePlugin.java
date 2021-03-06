package org.moskito.control.plugins.opsgenie;

import org.configureme.ConfigurationManager;
import org.moskito.control.core.ApplicationRepository;
import org.moskito.control.plugins.AbstractMoskitoControlPlugin;

/**
 * Plugin for OpsGenie notifications.
 * Sends alert to OpsGenie account, specified in configuration file
 * on any component status change, if status change notifications is not muted.
 */
public class OpsgeniePlugin extends AbstractMoskitoControlPlugin {

    /**
     * Path to OpsGenie configuration
     */
    private String configurationName;

    /**
     * Status change listener for OpsGenie plugin
     * Initialize on initialize() method call.
     * Link needs to be stored here for detaching it in deInitialize() method
     */
    private StatusChangeOpsgenieNotifier notifier;

    @Override
    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    @Override
    public void initialize() {

        OpsgenieConfig config = new OpsgenieConfig();
        ConfigurationManager.INSTANCE.configureAs(config, configurationName);

        notifier = new StatusChangeOpsgenieNotifier(config);

        // Attaching listener to event dispatcher for sending alerts to OpsGenie on status change
        ApplicationRepository.getInstance()
                .getEventsDispatcher().addStatusChangeListener(notifier);

    }

    @Override
    public void deInitialize() {
        // Removing listener, alerts to Opsgenie will not been send from now
        ApplicationRepository.getInstance()
                .getEventsDispatcher().removeStatusChangeListener(notifier);
    }

}