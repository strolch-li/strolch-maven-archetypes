package ${package}.server.web.web;

import static ${package}.server.web.web.StartupListener.APP_NAME;

import jakarta.ws.rs.ApplicationPath;
import java.util.logging.Level;

import ${package}.server.web.rest.ShopFloorResource;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.StrolchRestfulClasses;
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

		// register app rest services
		register(ShopFloorResource.class);

		// strolch services
		for (Class<?> clazz : StrolchRestfulClasses.getRestfulClasses()) {
			register(clazz);
		}

		// filters
		for (Class<?> clazz : StrolchRestfulClasses.getProviderClasses()) {
			register(clazz);
		}

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
