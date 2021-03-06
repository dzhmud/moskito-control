package org.moskito.control.core;

import org.configureme.sources.ConfigurationSource;
import org.configureme.sources.ConfigurationSourceKey;
import org.configureme.sources.ConfigurationSourceListener;
import org.configureme.sources.ConfigurationSourceRegistry;
import org.moskito.control.config.*;
import org.moskito.control.core.chart.Chart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages applications.
 *
 * @author lrosenberg
 * @since 01.04.13 23:08
 */
public final class ApplicationRepository {

	/**
	 * Map with currently configured applications.
	 */
	private ConcurrentMap<String, Application> applications;

	/**
	 * Manages components events
	 */
	private EventsDispatcher eventsDispatcher = new EventsDispatcher();

	/**
	 * Logger.
	 */
	private static Logger log = LoggerFactory.getLogger(ApplicationRepository.class);
	/**
	 * Returns the singleton instance of the ApplicationRepository.
	 * @return instance of the ApplicationRepository
	 */
	public static final ApplicationRepository getInstance(){
		return ApplicationRepositoryInstanceHolder.instance;
	}

	/**
	 * Creates a new repository.
	 */
	private ApplicationRepository(){
		applications = new ConcurrentHashMap<String, Application>();

		readConfig();

        //Listen for config updates
        final ConfigurationSourceKey sourceKey = new ConfigurationSourceKey(ConfigurationSourceKey.Type.FILE, ConfigurationSourceKey.Format.JSON, "moskitocontrol");
        ConfigurationSourceRegistry.INSTANCE.addListener(sourceKey, new ConfigurationSourceListener() {
            public void configurationSourceUpdated(ConfigurationSource target) {
                log.info("Configuration file updated, reading config...");
                readConfig();
            }
        });
	}

    /**
     * Read applications configuration.
     */
	private void readConfig(){
        applications.clear();
		ApplicationConfig[] configuredApplications = MoskitoControlConfiguration.getConfiguration().getApplications();
		for (ApplicationConfig ac : configuredApplications){
			Application app = new Application(ac.getName());
			for (ComponentConfig cc : ac.getComponents()){
				Component comp = new Component(app);
				comp.setCategory(cc.getCategory());
				comp.setName(cc.getName());
				app.addComponent(comp);
			}

			if (ac.getCharts()!=null && ac.getCharts().length>0){
				for (ChartConfig cc : ac.getCharts()){
					Chart chart = new Chart(app, cc.getName(), cc.getLimit());

					ChartLineConfig[] lines = cc.getLines();
					for (ChartLineConfig line : lines){
						chart.addLine(line.getComponent(), line.getAccumulator(), line.getCaption());
					}

					app.addChart(chart);
				}
			}
			addApplication(app);
		}
	}

	private void addApplication(Application app){
		applications.put(app.getName(), app);
	}

	public List<Application> getApplications(){
		ArrayList<Application> ret = new ArrayList<Application>();
		ret.addAll(applications.values());
		return ret;
	}

	public Application getApplication(String applicationName) {
		return applications.get(applicationName);
	}

	public EventsDispatcher getEventsDispatcher() {
		return eventsDispatcher;
	}

	/**
	 * Singleton instance holder class.
	 */
	private static class ApplicationRepositoryInstanceHolder{
		/**
		 * Singleton instance.
		 */
		private static final ApplicationRepository instance = new ApplicationRepository();
	}

}
