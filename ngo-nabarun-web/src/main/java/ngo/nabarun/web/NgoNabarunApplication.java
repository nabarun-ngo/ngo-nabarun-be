
package ngo.nabarun.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {
    "ngo.nabarun.web",
    "ngo.nabarun.application",
    "ngo.nabarun.domain",
    "ngo.nabarun.infra",
    "ngo.nabarun.common"
})
@EnableMongoRepositories(basePackages = "ngo.nabarun.infra.mongo.repo")
public class NgoNabarunApplication {
    public static void main(String[] args) {
        SpringApplication.run(NgoNabarunApplication.class, args);
    }
}
