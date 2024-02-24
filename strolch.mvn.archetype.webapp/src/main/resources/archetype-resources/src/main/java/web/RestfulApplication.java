package ${package}.web;

import static ${package}.web.StartupListener.APP_NAME;

import javax.ws.rs.ApplicationPath;
import java.util.logging.Level;

import ${package}.rest.BooksResource;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.StrolchRestfulClasses;
import li.strolch.rest.StrolchRestfulExceptionMapper;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("rest")
public class RestfulApplication extends ResourceConfig {

	private static final Logger logger = LoggerFactory.getLogger(RestfulApplication.class);

	public RestfulApplication() {
		setApplicationName(APP_NAME);

		// add project resources by package name
		packages(BooksResource.class.getPackage().getName());

		// strolch services
		for (Class<?> clazz : StrolchRestfulClasses.getRestfulClasses()) {
			register(clazz);
		}

		// filters
		for (Class<?> clazz : StrolchRestfulClasses.getProviderClasses()) {
			register(clazz);
		}

		// log exceptions and return them as plain text to the caller
		register(StrolchRestfulExceptionMapper.class);

		RestfulStrolchComponent restfulComponent = RestfulStrolchComponent.getInstance();
		if (restfulComponent.isRestLogging()) {
			register(new LoggingFeature(java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
					Level.SEVERE, LoggingFeature.Verbosity.PAYLOAD_ANY, Integer.MAX_VALUE));

			property(ServerProperties.TRACING, "ALL");
			property(ServerProperties.TRACING_THRESHOLD, "TRACE");
		}

		logger.info(
				"Initialized REST application " + getApplicationName() + " with " + getClasses().size() + " classes, "
						+ getInstances().size() + " instances, " + getResources().size() + " resources and "
						+ getProperties().size() + " properties");
	}
}
