package ngo.nabarun.app.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.infra.service.IApiKeyInfraService;

import static org.springframework.security.config.Customizer.withDefaults;


import org.springframework.security.oauth2.jwt.*;


@Configuration
public class SecurityConfig {
	
	@Autowired
	private IApiKeyInfraService apiKeyInfraService;

	@Bean
	@Order(1) // Order 1 for API requests
	SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
		ApiKeyAuthFilter filter = new ApiKeyAuthFilter("X-API-KEY");
		filter.setAuthenticationManager(new ApiKeyAuthManager(apiKeyInfraService));
		return http.antMatcher("/api/**").csrf(csrf -> csrf.disable()).cors(withDefaults()).sessionManagement(session -> {
			session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}).addFilter(filter).authorizeHttpRequests(request -> {
			request.antMatchers("/api/**").authenticated();
		}).oauth2ResourceServer(server -> server.jwt()).build();
	}

	@Bean
	@Order(2) // Order 2 for non-API requests
	SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
		return http.antMatcher("/**").sessionManagement(session -> {
			session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
		}).authorizeHttpRequests().anyRequest().permitAll().and().build();
	}

	@Autowired
	private PropertyHelper propertyHelper;

	@Bean
	JwtDecoder jwtDecoder() {
		/*
		 * By default, Spring Security does not validate the "aud" claim of the token,
		 * to ensure that this token is indeed intended for our app. Adding our own
		 * validator is easy to do:
		 */

		NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders
				.fromOidcIssuerLocation(propertyHelper.getAuth0IssuerURI());

		OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(
				propertyHelper.getAuth0ResourceAPIAudience());
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators
				.createDefaultWithIssuer(propertyHelper.getAuth0IssuerURI());
		OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

		jwtDecoder.setJwtValidator(withAudience);

		return jwtDecoder;
	}
	
	
	
	
//	@Bean
//	SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
//		/*
//		 * This is where we configure the security required for our endpoints and setup
//		 * our app to serve as an OAuth2 Resource Server, using JWT validation.
//		 */
//		http.csrf(csrf -> csrf.disable()).cors(withDefaults()).authorizeHttpRequests(requests -> {
//			System.out.println("1");
//			requests.antMatchers("/api/**").authenticated().antMatchers("/**").permitAll()
//			/*
//			 * .and().mvcMatchers("/api/private-scoped").hasAuthority("SCOPE_read:messages")
//			 * )
//			 */
//			;
//
//		}).oauth2ResourceServer(server -> server.jwt());
//		return http.build();
//	}

}
