package ngo.nabarun.app.common.helper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.Setter;

@Configuration
@PropertySource("classpath:mock-data-${ENVIRONMENT:dev}.properties")
@ConfigurationProperties(prefix = "mockdata")
@Getter
@Setter
public class GenericMockDataHelper {
	
	private String emailRecipient;
	private String authUserId;

	
	public String[] getEmailRecipient() {
		return emailRecipient.split(",");
	}
}
