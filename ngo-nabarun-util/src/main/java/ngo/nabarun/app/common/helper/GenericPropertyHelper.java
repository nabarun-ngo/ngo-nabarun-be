package ngo.nabarun.app.common.helper;

import java.util.List;

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
	
	@Value("${APP_SECRET}")
	private String appSecret; 
	
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
	

	public String getDefaultEmailSender() {
		return systemEnv.getProperty("SENDGRID_SENDER_ADDRESS");
	}
	
	@Value("${APP_NAME:'Nabarun'}")
	private String appName;
	
	@Value("${SENDGRID_DEFAULT_TEMPLATE_ID}")
	private String defaultEmailTemplateIdSendGrid;
	
	/**
	 * Enable production mode in testing env. i.e. sent email to actual email even in  test environment
	 */
	@Value("${ENABLE_PROD_MODE_FOR_TEST:false}")
	private boolean prodModeEnabledForTest;
	
	/**
	 * All Email will be sent to mocked email address if enabled.
	 * @return
	 */
	public boolean isEmailMockingEnabledForTest() {
		return isProdEnv() ? false : Boolean.valueOf(systemEnv.getProperty("ENABLE_EMAIL_MOCKING_FOR_TEST"));
	}
	
	/**
	 * When ENABLE_EMAIL_MOCKING_FOR_TEST=true 
	 * then all email will be sent to this email
	 */
	public List<String> getMockedEmailAddress() {
		return List.of(String.valueOf(systemEnv.getProperty("MOCKED_EMAIL_ADDRESS")).split(","));
	}
	
	/**
	 * Authenticated users details will be mocked in test env with the mocked user id. 
	 * No values will be taken from token
	 * @return
	 */
	public boolean isTokenMockingEnabledForTest() {
		return isProdEnv() ? false : Boolean.valueOf(systemEnv.getProperty("ENABLE_TOKEN_MOCKING_FOR_TEST"));
	}
	
	/**
	 * When ENABLE_TOKEN_MOCKING_FOR_TEST=true 
	 * then Authenticated users details will be mocked as this user id
	 */
	@Value("${MOCKED_TOKEN_USER_ID:''}")
	private String mockedTokenUserId;
	
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
	private String firebaseCredential;
	
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

	@Value("${GOOGLE_RECAPTCHA_SITE_KEY:'NO_VALUE'}")
	private String googleRecaptchaSiteKey;
	
	@Value("${GOOGLE_RECAPTCHA_SECURITY_KEY:'NO_VALUE'}")
	private String googleRecaptchaSecurityKey;
	
	public String getDopplerServiceKey() {
		return systemEnv.getProperty("DOPPLER_SERVICE_TOKEN");
	}

	public String getDopplerProject() {
		return systemEnv.getProperty("DOPPLER_PROJECT_NAME");
	}

	public String getDopplerConfigName() {
		return systemEnv.getProperty("ENVIRONMENT").toLowerCase() ;
	}
}
