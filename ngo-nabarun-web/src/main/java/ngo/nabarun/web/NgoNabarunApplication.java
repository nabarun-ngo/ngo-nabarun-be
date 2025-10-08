
package ngo.nabarun.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import ngo.nabarun.outbox.config.EnableEventOutbox;

@SpringBootApplication(scanBasePackages = {
    "ngo.nabarun.web",
    "ngo.nabarun.application",
    "ngo.nabarun.domain",
    "ngo.nabarun.infra",
    "ngo.nabarun.common",
    "ngo.nabarun.infra.mapper",

})
@EnableRetry
@EnableAsync
@EnableEventOutbox
public class NgoNabarunApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(NgoNabarunApplication.class);
        application.addInitializers(new AppInitializer());
        application.run(args);
    }
    
}
