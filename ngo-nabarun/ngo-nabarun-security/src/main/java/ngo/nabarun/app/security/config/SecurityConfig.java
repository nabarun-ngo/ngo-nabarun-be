package ngo.nabarun.app.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import ngo.nabarun.app.common.helper.GenericPropertyHelper;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

import org.springframework.security.oauth2.jwt.*;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

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

	@Bean
	@Order(1) // Order 1 for API requests
	SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(csrf -> csrf.disable()).cors(withDefaults()).authorizeHttpRequests(request->{
			request.antMatchers("/api/**").authenticated();
		}).oauth2ResourceServer(server -> server.jwt()).build();
	}

	@Bean
	@Order(2) // Order 2 for non-API requests
	SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
		return http.antMatcher("/**").authorizeHttpRequests().anyRequest().permitAll().and().build();
	}

	@Autowired
	private GenericPropertyHelper propertyHelper;

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

}
