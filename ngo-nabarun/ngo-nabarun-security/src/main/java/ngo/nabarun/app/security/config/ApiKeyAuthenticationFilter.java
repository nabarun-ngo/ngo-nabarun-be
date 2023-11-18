package ngo.nabarun.app.security.config;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class ApiKeyAuthenticationFilter implements Filter {

    static final private String AUTH_METHOD = "api-key";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
    	System.out.println(request.toString());

        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            String apiKey = getApiKey((HttpServletRequest) request);
            if(apiKey != null) {
                if(apiKey.equals("my-valid-api-key")) {
                    ApiKeyAuthenticationToken apiToken = new ApiKeyAuthenticationToken(apiKey, AuthorityUtils.NO_AUTHORITIES);
                    SecurityContextHolder.getContext().setAuthentication(apiToken);
                } else {
                	System.out.println("Hello s");
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    httpResponse.setStatus(401);
                    httpResponse.getWriter().write("Invalid API Key");
                    return;
                }
            }
        }
        
        chain.doFilter(request, response);
        
    }

    private String getApiKey(HttpServletRequest httpRequest) {
        String apiKey = null;
        
        String authHeader = httpRequest.getHeader("X-API-KEY");
        if(authHeader != null) {
        	 apiKey =authHeader;
        }
        
        return apiKey;
    }
}