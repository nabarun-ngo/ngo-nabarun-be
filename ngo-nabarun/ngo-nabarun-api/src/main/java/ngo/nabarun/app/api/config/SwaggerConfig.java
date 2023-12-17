package ngo.nabarun.app.api.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

@Configuration
@OpenAPIDefinition(info = @Info(title = "NABARUN API", version = "2.0"))
@SecuritySchemes({
	@SecurityScheme(name = "nabarun_auth", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER),
	@SecurityScheme(name = "nabarun_auth2", scheme = "apikey", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER,paramName = "X-API-KEY"),
	})
public class SwaggerConfig {
	
}


