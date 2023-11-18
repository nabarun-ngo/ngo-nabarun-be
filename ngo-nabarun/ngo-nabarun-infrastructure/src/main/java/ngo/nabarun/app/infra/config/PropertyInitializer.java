package ngo.nabarun.app.infra.config;

import java.util.HashMap;
import java.util.Map;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.common.enums.DBConfigParamType;
import ngo.nabarun.app.common.enums.DBType;
import ngo.nabarun.app.infra.core.entity.DBConfig;
import ngo.nabarun.app.infra.core.entity.UserProfileEntity;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * This class will bootstrap and add properties from DB to Env
 * 
 */
@Configuration
@Slf4j
public class PropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final String PROPERTY_SOURCE_NAME = "databaseProperties";
	private static final String CONFIG_COLLECTION_NAME = "db_config";

	private static boolean arePropertiesLoadedFromDatabase = false;

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		if (!arePropertiesLoadedFromDatabase) {
			String connURI = System.getProperty("MONGODB_URL");
			try {
				Map<String, Object> propertySource = getPropertyMapFromDatabase(connURI, DBType.MONGO);
				applicationContext.getEnvironment().getPropertySources()
						.addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, propertySource));
			} catch (Exception e) {
				arePropertiesLoadedFromDatabase = false;
				e.printStackTrace();
			}

		}
		arePropertiesLoadedFromDatabase = true;
	}

	private Map<String, Object> getPropertyMapFromDatabase(String connUri, DBType type) {
		Map<String, Object> propertySource = new HashMap<>();
		switch (type) {
		case MONGO: {
			ConnectionString connectionString = new ConnectionString(connUri);
			CodecRegistry pojoCodecRegistry = fromProviders(
					PojoCodecProvider.builder().register(DBConfig.class,UserProfileEntity.class).automatic(true).build());
			CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
					pojoCodecRegistry);
			MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
					.codecRegistry(codecRegistry).build();
			MongoClient client = MongoClients.create(clientSettings);
			FindIterable<DBConfig> configs = client.getDatabase(connectionString.getDatabase())
					.getCollection(CONFIG_COLLECTION_NAME, DBConfig.class).find();
			for (DBConfig dbConfig : configs) {
				if (dbConfig.isActive()) {
					try {
						if (DBConfigParamType.valueOf(dbConfig.getProperty_value_type()) == DBConfigParamType.BOOLEAN) {
							propertySource.put(dbConfig.getProperty_key(),
									Boolean.valueOf(dbConfig.getProperty_value()));
						} else if (DBConfigParamType
								.valueOf(dbConfig.getProperty_value_type()) == DBConfigParamType.DOUBLE) {
							propertySource.put(dbConfig.getProperty_key(),
									Double.valueOf(dbConfig.getProperty_value()));
						} else if (DBConfigParamType
								.valueOf(dbConfig.getProperty_value_type()) == DBConfigParamType.INTEGER) {
							propertySource.put(dbConfig.getProperty_key(),
									Integer.valueOf(dbConfig.getProperty_value()));
						} else {
							propertySource.put(dbConfig.getProperty_key(), dbConfig.getProperty_value());
						}
						//System.out.println(dbConfig.getProperty_key() + " -> " + dbConfig.getProperty_value());
						log.debug("Property '"+dbConfig.getProperty_key()+"' has been initialized from DB.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			client.close();
			break;
		}
		default:
			break;
		}
		return propertySource;
	}

}
