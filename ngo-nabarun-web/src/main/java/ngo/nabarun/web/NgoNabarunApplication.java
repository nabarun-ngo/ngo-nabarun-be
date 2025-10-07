
package ngo.nabarun.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "ngo.nabarun.web",
    "ngo.nabarun.application",
    "ngo.nabarun.domain",
    "ngo.nabarun.infra",
    "ngo.nabarun.common",
    "ngo.nabarun.infra.mapper",

})
public class NgoNabarunApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(NgoNabarunApplication.class);
        application.addInitializers(new AppInitializer());
        application.run(args);
    }
    
}
