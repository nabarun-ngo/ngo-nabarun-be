package ngo.nabarun.web.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

@Configuration
@OpenAPIDefinition(info = @Info(title = "NABARUN API", version = "2.0.3"))
@SecuritySchemes({
		@SecurityScheme(name = "nabarun_auth", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER),
		@SecurityScheme(name = "nabarun_auth_apikey", scheme = "apikey", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-Api-Key") })
public class SwaggerConfig {

	@Bean
	ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

	@Bean
	OperationCustomizer customGlobalHeadersAndAddActuatorSecurity() {

		return (Operation operation, HandlerMethod handlerMethod) -> {
			
			Parameter corelationIdHeader = new Parameter().in(ParameterIn.HEADER.toString()).schema(new StringSchema())
					.name(RequestFilterConfig.CORRELATION_ID).required(false);
			operation.addParametersItem(corelationIdHeader);
			if(operation.getTags().contains("Actuator")) {
				operation.addSecurityItem(new SecurityRequirement().addList(ACTUATOR_SECURITY_SCHEME_NAME).addList("nabarun_auth"));
			}
			return operation;
		};
	}
	
	  private static final String ACTUATOR_SECURITY_SCHEME_NAME = "nabarun_auth_apikey";

}