package ngo.nabarun.app.infra.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * Configuration class for MongoTemplate
 */
@Configuration
public class MongoDBConfig {
	
	@Bean(name = "mongoTemplate")
	MongoTemplate customMongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
		return new MongoDBSoftDeleteTemplate(databaseFactory, converter);
	}
	
}

