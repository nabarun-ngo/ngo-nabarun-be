package ngo.nabarun.app.api.config;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Authentication authentication;

    public ApiKeyAuthenticationToken(Authentication authentication, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.authentication=authentication;
        setAuthenticated(authentication.isAuthenticated());
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }
}