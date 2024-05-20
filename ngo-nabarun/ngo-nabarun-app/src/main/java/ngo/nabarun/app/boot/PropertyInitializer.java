package ngo.nabarun.app.boot;

import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.prop.PropertyFactory;
import ngo.nabarun.app.prop.PropertySource;

/**
 * This class will bootstrap and add properties from DB to Env
 * 
 */
@Configuration
@Slf4j
public class PropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final String PROPERTY_SOURCE_NAME = "databaseProperties";
	private static final String COLLECTION_DB_CONFIG = "db_config_";
	private static final String DOPPLER_PROJECT_NAME = "DOPPLER_PROJECT_NAME";
	private static final String DOPPLER_SERVICE_TOKEN = "DOPPLER_SERVICE_TOKEN";
	private static final String ENVIRONMENT = "ENVIRONMENT";


	private static boolean arePropertiesLoaded = false;
	
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		if (!arePropertiesLoaded) {
			try {
				log.info("Initilizing boot properties and secrets.");
				
				//Map<String, Object> propertySource = loadFromDB();
				Map<String, Object> propertySource = loadFromExternalService();
				applicationContext.getEnvironment().getPropertySources()
						.addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, propertySource));
				if(log.isDebugEnabled()) {
					for(String key:propertySource.keySet()) {
						log.debug("Property '"+key+"' initialized.");
						// System.out.println(propertySource.get(key));
					}
				}
				arePropertiesLoaded = true;
			} catch (Exception e) {
				arePropertiesLoaded = false;
				log.error("Error while initilizing properties.",e);
			}
		}
	}
	
	
	private Map<String, Object> loadFromExternalService() throws Exception {
		String projectName=System.getenv(DOPPLER_PROJECT_NAME) == null ? System.getProperty(DOPPLER_PROJECT_NAME) : System.getenv(DOPPLER_PROJECT_NAME);
		String token=System.getenv(DOPPLER_SERVICE_TOKEN) == null ? System.getProperty(DOPPLER_SERVICE_TOKEN) : System.getenv(DOPPLER_SERVICE_TOKEN);
		String env = System.getenv(ENVIRONMENT) == null ? System.getProperty(ENVIRONMENT) : System.getenv(ENVIRONMENT);
		System.out.println(projectName);
		Assert.notNull(projectName, "DOPPLER_PROJECT_NAME must be set as argument");
		Assert.notNull(token, "DOPPLER_SERVICE_TOKEN must be set as argument");
		Assert.notNull(env, "ENVIRONMENT must be set as argument");

		PropertySource conn=PropertyFactory.initDoppler(projectName, env, token);
		return conn.loadProperties();
	}


	@SuppressWarnings("unused")
	private Map<String,Object> loadFromDB() throws Exception{
		String appSecret=System.getProperty("APP_SECRET");
		String connURL=System.getProperty("APP_CONFIGDB_URL");
		String env = System.getProperty("ENVIRONMENT");
		
		Assert.notNull(appSecret, "APP_SECRET must be set as argument");
		Assert.notNull(connURL, "APP_CONFIGDB_URL must be set as argument");
		Assert.notNull(env, "ENVIRONMENT must be set as argument");

		PropertySource conn=PropertyFactory.connectMongo(connURL,COLLECTION_DB_CONFIG+env);
		return conn.loadProperties(appSecret);
	}
}
