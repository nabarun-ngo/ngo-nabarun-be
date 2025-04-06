package ngo.nabarun.app.common.helper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.Getter;
import ngo.nabarun.app.common.util.CommonUtils;

@Configuration
public class PropertyHelper {
	
	//@Getter(AccessLevel.PACKAGE)
	@Autowired
	private Environment systemEnv;
	
	@Getter
	@Value("${ENVIRONMENT}")
	private String environmentName;
	
	@Getter
	@Value("${APP_SECRET}")
	private String appSecret; 
	
	@Getter
	@Value("${INCLUDE_ERROR_DETAILS:false}")
	private boolean includeErrorDetails;
	
	@Getter
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
	
	@Getter
	@Value("${APP_NAME:'Nabarun'}")
	private String appName;
	
	@Getter
	@Value("${SENDGRID_DEFAULT_TEMPLATE_ID}")
	private String defaultEmailTemplateIdSendGrid;
	
	/**
	 * Enable production mode in testing env. i.e. sent email to actual email even in  test environment
	 */
	@Getter
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
	@Getter
	@Value("${MOCKED_TOKEN_USER_ID:''}")
	private String mockedTokenUserId;
	
	@Getter
	@Value("${FIREBASE_FILESTORAGE_BUCKET}")
	private String firebaseFileStorageBucket;
	
	@Getter
	@Value("${AUTH0_RESOURCE_API_AUDIENCE}")
	private String auth0ResourceAPIAudience;

	@Getter
	@Value("${AUTH0_ISSUER_URI}")
	private String auth0IssuerURI;
	
	public boolean isProdEnv() {
		String environmentName =systemEnv.getProperty("ENVIRONMENT");
		return CommonUtils.getProdProfileNames().stream().anyMatch(environmentName::equalsIgnoreCase);
	}
	
	@Getter
	@Value("${SENDGRID_API_KEY:'NO_VALUE'}")
	private String sendGridAPIKey;
	
	@Getter
	@Value("${FIREBASE_CREDENTIAL:'NO_VALUE'}")
	private String firebaseCredential;
	
	@Getter
	@Value("${FIREBASE_DATABASE_URL}")
	private String firebaseDBUrl;
	
	@Getter
	@Value("${AUTH0_BASE_URL:'NO_VALUE'}")
	private String auth0BaseURL;

	@Getter
	@Value("${AUTH0_MANAGEMENT_CLIENT_ID:'NO_VALUE'}")
	private String auth0ManagementClientId;

	@Getter
	@Value("${AUTH0_MANAGEMENT_CLIENT_SECRET:'NO_VALUE'}")
	private String auth0ManagementClientSecret;

	@Getter
	@Value("${AUTH0_MANAGEMENT_API_AUDIENCE}")
	private String auth0ManagementAPIAudience;
	
	@Getter
	@Value("${GOOGLE_CLIENT_ID:'NO_VALUE'}")
	private String googleClientId;
	
	@Getter
	@Value("${GOOGLE_CLIENT_SECRET:'NO_VALUE'}")
	private String googleClientSecret;

	@Getter
	@Value("${GOOGLE_RECAPTCHA_SITE_KEY:'NO_VALUE'}")
	private String googleRecaptchaSiteKey;
	
	@Getter
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
	
	@Getter
	@Value("${APP_LOGIN_URL}")
	private String appLoginURL;
	
	@Getter
	@Value("${GITHUB_TOKEN}")
	private String githubToken;
	
	@Getter
	@Value("${GITHUB_ORG}")
	private String githubOrg;
	
	@Getter
	@Value("${GITHUB_REPO}")
	private String githubRepo;
	
	@Getter
	@Value("${GITHUB_DICUSSION_ID}")
	private String githubDiscussionId;
	
}
