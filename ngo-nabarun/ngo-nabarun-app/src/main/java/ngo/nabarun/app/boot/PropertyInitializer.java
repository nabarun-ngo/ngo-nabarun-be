package ngo.nabarun.app.boot;

import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.prop.PropertySource;
import ngo.nabarun.app.prop.DBPropertySource;

/**
 * This class will bootstrap and add properties from DB to Env
 * 
 */
@Configuration
@Slf4j
public class PropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final String PROPERTY_SOURCE_NAME = "databaseProperties";

	private static boolean arePropertiesLoadedFromDatabase = false;
	
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		if (!arePropertiesLoadedFromDatabase) {
			try {
				log.info("Initilizing boot properties and secrets.");
				String appSecret=System.getProperty("APP_SECRET");
				String connURL=System.getProperty("APP_CONFIGDB_URL");
				DBPropertySource conn=PropertySource.connectMongo(connURL);
				Map<String, Object> propertySource =conn.loadProperties(appSecret);
				applicationContext.getEnvironment().getPropertySources()
						.addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, propertySource));
				if(log.isDebugEnabled()) {
					for(String key:propertySource.keySet()) {
						log.debug("Property '"+key+"' initialized.");
					}
				}
			} catch (Exception e) {
				arePropertiesLoadedFromDatabase = false;
				log.error("Error while initilizing properties.",e);
			}
		}
		arePropertiesLoadedFromDatabase = true;
	}
}
