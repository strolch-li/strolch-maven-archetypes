package ${package}.server.web.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.InputStream;

import li.strolch.agent.api.LoggingLoader;
import li.strolch.agent.api.StrolchAgent;
import li.strolch.agent.api.StrolchBootstrapper;
import li.strolch.utils.helper.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class StartupListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);
	public static final String APP_NAME = "${appName}";

	private StrolchAgent agent;

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		logger.info("Starting " + APP_NAME + "...");
		long start = System.currentTimeMillis();
		try {
			String bootstrapFileName = "/" + StrolchBootstrapper.FILE_BOOTSTRAP;
			InputStream bootstrapFile = getClass().getResourceAsStream(bootstrapFileName);
			StrolchBootstrapper bootstrapper = new StrolchBootstrapper(StartupListener.class);
			this.agent = bootstrapper.setupByBootstrapFile(StartupListener.class, bootstrapFile);
			this.agent.initialize();
			this.agent.start();
		} catch (Throwable e) {
			logger.error("Failed to start " + APP_NAME + " due to: " + e.getMessage(), e);
			throw e;
		}

		long took = System.currentTimeMillis() - start;
		logger.info("Started " + APP_NAME + " in " + (StringHelper.formatMillisecondsDuration(took)));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		LoggingLoader.reloadLoggingConfiguration();

		if (this.agent != null) {
			logger.info("Destroying " + APP_NAME + "...");
			try {
				this.agent.stop();
				this.agent.destroy();
			} catch (Throwable e) {
				logger.error("Failed to stop " + APP_NAME + " due to: " + e.getMessage(), e);
				throw e;
			}
		}
		logger.info("Destroyed " + APP_NAME);
	}
}
