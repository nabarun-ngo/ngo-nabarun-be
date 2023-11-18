package ngo.nabarun.app.common.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.AccessLevel;
import lombok.Getter;
import ngo.nabarun.app.common.util.CommonUtils;

@Configuration
@Getter
public class GenericPropertyHelper {
	
	@Getter(AccessLevel.NONE)
	@Autowired
	private Environment systemEnv;
	
	@Value("${ENVIRONMENT}")
	private String environmentName;
	
	@Value("${INCLUDE_ERROR_DETAILS:false}")
	private boolean includeErrorDetails;
	
	@Value("${RESTTIMEOUT_IN_SEC:120}")
	private int thirdPartyRestCallTimeout;

	public String[] getAllowedCorsOrigins() {
		return systemEnv.getProperty("CORS_ALLOWED_ORIGIN") == null ? null : systemEnv.getProperty("CORS_ALLOWED_ORIGIN").split(",");
	}
	
	public String[] getAllowedCorsMethods() {
		return systemEnv.getProperty("CORS_ALLOWED_METHODS") == null ? null : systemEnv.getProperty("CORS_ALLOWED_METHODS").split(",");
	}
	
	@Value("${APP_MAIl_SENDER_ADDRESS}")
	private String defaultEmailSender;
	
	@Value("${APP_NAME:'Nabarun'}")
	private String appName;
	
	@Value("${SENDGRID_DEFAULT_TEMPLATE_ID}")
	private String defaultEmailTemplateIdSendGrid;
	
	@Value("${ENABLE_PROD_MODE_FOR_TEST:false}")
	private boolean prodModeEnabledForTest;
	
	public boolean isEmailMockingEnabledForTest() {
		return isProdEnv() ? false : Boolean.valueOf(systemEnv.getProperty("ENABLE_EMAIL_MOCKING_FOR_TEST"));
	}
	
	public boolean isTokenMockingEnabledForTest() {
		return isProdEnv() ? false : Boolean.valueOf(systemEnv.getProperty("ENABLE_TOKEN_MOCKING_FOR_TEST"));
	}
	
	@Value("${FIREBASE_FILESTORAGE_BUCKET}")
	private String firebaseFileStorageBucket;
	
	@Value("${AUTH0_RESOURCE_API_AUDIENCE}")
	private String auth0ResourceAPIAudience;

	@Value("${AUTH0_ISSUER_URI}")
	private String auth0IssuerURI;
	
	public boolean isProdEnv() {
		String environmentName =systemEnv.getProperty("ENVIRONMENT");
		return CommonUtils.getProdProfileNames().stream().anyMatch(environmentName::equalsIgnoreCase);
	}
	
	@Value("${SENDGRID_API_KEY:'NO_VALUE'}")
	private String sendGridAPIKey;
	
	@Value("${FIREBASE_CREDENTIAL:'NO_VALUE'}")
	private String firebaseBase64Credential;
	
	@Value("${AUTH0_BASE_URL:'NO_VALUE'}")
	private String auth0BaseURL;

	@Value("${AUTH0_MANAGEMENT_CLIENT_ID:'NO_VALUE'}")
	private String auth0ManagementClientId;

	@Value("${AUTH0_MANAGEMENT_CLIENT_SECRET:'NO_VALUE'}")
	private String auth0ManagementClientSecret;

	@Value("${AUTH0_MANAGEMENT_API_AUDIENCE}")
	private String auth0ManagementAPIAudience;
	
	@Value("${GOOGLE_CLIENT_ID:'NO_VALUE'}")
	private String googleClientId;
	
	@Value("${GOOGLE_CLIENT_SECRET:'NO_VALUE'}")
	private String googleClientSecret;
}
