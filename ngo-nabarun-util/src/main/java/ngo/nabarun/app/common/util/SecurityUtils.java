package ngo.nabarun.app.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.Getter;

public class SecurityUtils {

	public static boolean isAuthenticated() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.isAuthenticated();
	}

	public static String getAuthUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
	
	public static AuthenticatedUser getAuthUser() {
		return new AuthenticatedUser();
	}
	
	@Getter
	public static class AuthenticatedUser{
		private String id;
		private String userId;
		private String name;
		
		AuthenticatedUser() {
			Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
			if(authentication != null) {
				Jwt principal =  (Jwt) authentication.getPrincipal();
				this.id=principal.getClaim("profile_id");
				this.userId=authentication.getName();
				this.name=principal.getClaim("profile_name");
			}	
		}
	}
	
}
