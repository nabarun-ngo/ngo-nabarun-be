package ngo.nabarun.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import ngo.nabarun.common.props.PropertyHelper;
import ngo.nabarun.common.props.PropertySource;
import ngo.nabarun.common.props.source.EnvironmentSource;
import ngo.nabarun.common.props.source.SystemSource;

@Configuration
public class PropertyConfig {

	@Autowired
	private Environment environment;

	@Bean
	PropertyHelper propertyHelper() {
		return new PropertyHelper(new SpringEnvironmentSource(environment), new EnvironmentSource(),
				new SystemSource());
	}

}

/**
 * A PropertySource that delegates to Spring's Environment. Supports all active
 * property sources (application.yml, system, env vars, etc.).
 */
class SpringEnvironmentSource implements PropertySource {

	private final Environment environment;

	public SpringEnvironmentSource(Environment environment) {
		this.environment = environment;
	}

	@Override
	public String getProperty(String key) {
		String value = environment.getProperty(key);
		return StringUtils.hasText(value) ? value : null;
	}
}
