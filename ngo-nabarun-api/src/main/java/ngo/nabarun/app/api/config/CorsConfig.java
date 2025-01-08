package ngo.nabarun.app.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ngo.nabarun.app.common.helper.PropertyHelper;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Autowired
	private PropertyHelper propertyHelper;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**").allowedOrigins(propertyHelper.getAllowedCorsOrigins()).allowedHeaders("*")
				.allowedMethods(propertyHelper.getAllowedCorsMethods()).allowCredentials(true);
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/public/**").addResourceLocations("file:public/");
	}

}
