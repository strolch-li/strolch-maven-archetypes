package ${package}.plc.web.web;

import static li.strolch.utils.helper.ExceptionHelper.hasCause;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.File;
import java.io.InputStream;

import li.strolch.agent.api.LoggingLoader;
import li.strolch.agent.api.StrolchAgent;
import li.strolch.agent.api.StrolchBootstrapper;
import li.strolch.exception.StrolchException;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.utils.helper.FileHelper;
import li.strolch.utils.helper.StringHelper;
import li.strolch.xmlpers.api.XmlPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

@WebListener
public class StartupListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);
	public static final String APP_NAME = "MyApp";

	private StrolchAgent agent;

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		logger.info("Starting " + APP_NAME + "...");
		long start = System.currentTimeMillis();
		try {
			startStrolch(sce);
		} catch (Throwable e) {
			logger.error("Failed to start " + APP_NAME + " due to: " + e.getMessage(), e);
			throw e;
		}

		long took = System.currentTimeMillis() - start;
		logger.info("Started " + APP_NAME + " in " + (StringHelper.formatMillisecondsDuration(took)));
	}

	private void startStrolch(ServletContextEvent sce) {

		String bootstrapFileName = "/" + StrolchBootstrapper.FILE_BOOTSTRAP;
		InputStream bootstrapFile = getClass().getResourceAsStream(bootstrapFileName);
		StrolchBootstrapper bootstrapper = new StrolchBootstrapper(StartupListener.class);
		this.agent = bootstrapper.setupByBootstrapFile(StartupListener.class, bootstrapFile);

		try {
			this.agent.initialize();
			this.agent.start();
			RestfulStrolchComponent.getInstance().setWebPath(sce.getServletContext().getRealPath("/"));
		} catch (StrolchException e) {

			if (!hasCause(e, XmlPersistenceException.class))
				throw e;

			logger.error(
					"Failed to start Strolch due to a XmlPersistenceException. Deleting local dbStore and restarting the agent...");

			this.agent.stop();
			this.agent.destroy();

			File dataPath = this.agent.getStrolchConfiguration().getRuntimeConfiguration().getDataPath();
			File dbStorePath = new File(dataPath, "dbStore");
			if (dbStorePath.exists()) {
				if (!FileHelper.deleteFile(dbStorePath, true))
					throw new IllegalStateException(
							"Strolch failed to load due to a persistence exception, could not delete dbStore, so failing hard!");

				// now try again to start
				bootstrapper = new StrolchBootstrapper(StartupListener.class);
				bootstrapFile = getClass().getResourceAsStream(bootstrapFileName);
				this.agent = bootstrapper.setupByBootstrapFile(StartupListener.class, bootstrapFile);
				this.agent.initialize();
				this.agent.start();
			}
		}
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
