package ngo.nabarun.web;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.common.util.CommonUtil;
import ngo.nabarun.doppler.api.ConfigsApi;
import ngo.nabarun.doppler.model.Secret;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class AppInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String PROPERTY_SOURCE_NAME = "properties";
    private static final String DOPPLER_PROJECT_NAME = "DOPPLER_PROJECT_NAME";
    private static final String DOPPLER_SERVICE_TOKEN = "DOPPLER_SERVICE_TOKEN";
    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static boolean arePropertiesLoaded = false;

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
    	initializeProperties(applicationContext);
    }

    private void initializeProperties(ConfigurableApplicationContext applicationContext) {
    	 if(!arePropertiesLoaded){
             try {
                 log.info("Initializing boot properties and secrets.");
                 Map<String, Object> propertySource = loadFromDoppler();
                 applicationContext.getEnvironment().getPropertySources()
                         .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, propertySource));
                 if(log.isDebugEnabled()) {
                     for(String key:propertySource.keySet()) {
                         log.debug("Property '{}' initialized.", key);
                     }
                 }
                 arePropertiesLoaded = true;
             } catch (Exception e) {
                 arePropertiesLoaded = false;
                 log.error("Error while initializing properties.", e);
             }
         }
		
	}

	private Map<String, Object> loadFromDoppler() throws Exception {
        String projectName = CommonUtil.getEnvProperty(DOPPLER_PROJECT_NAME);
        String token = CommonUtil.getEnvProperty(DOPPLER_SERVICE_TOKEN);
        String config = CommonUtil.getEnvProperty(ENVIRONMENT);
        ConfigsApi configApi = new ConfigsApi(projectName,token);
        List<Secret> secrets=configApi.getSecrets(config);
        return secrets.stream().collect(
                Collectors.toMap(Secret::getKey, Secret::getValue)
        );
    }

   

}
