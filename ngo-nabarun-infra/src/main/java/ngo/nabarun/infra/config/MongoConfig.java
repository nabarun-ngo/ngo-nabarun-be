package ngo.nabarun.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "ngo.nabarun.infra.mongo.repo")
@Configuration
public class MongoConfig {
}