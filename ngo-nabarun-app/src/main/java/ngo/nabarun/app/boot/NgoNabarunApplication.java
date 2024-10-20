package ngo.nabarun.app.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@ComponentScan(basePackages = {
		"ngo.nabarun.app",
		"ngo.nabarun.app.api",
		"ngo.nabarun.app.web",
		"ngo.nabarun.app.security",
		"ngo.nabarun.app.businesslogic",
		"ngo.nabarun.app.common",
		"ngo.nabarun.app.infra",
		"ngo.nabarun.app.ext"
		})
@EntityScan(basePackages = {"ngo.nabarun.app"})
@EnableMongoRepositories(basePackages = {"ngo.nabarun.app"})
@EnableWebSecurity
//@EnableScheduling/custom form//notification
@EnableCaching
@EnableAsync
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class NgoNabarunApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(NgoNabarunApplication.class, args);
	}

}