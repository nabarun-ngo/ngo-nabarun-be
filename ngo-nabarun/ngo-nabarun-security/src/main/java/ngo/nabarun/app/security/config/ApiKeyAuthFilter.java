//package ngo.nabarun.app.security.config;
//
//import java.io.IOException;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//@Component
//public class ApiKeyAuthFilter extends OncePerRequestFilter {
//	@Value("${api.key:1234}")
//	private String apiKey;
//	@Value("${api.secret:1234}")
//	private String apiSecret;
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//		// Get the API key and secret from request headers
//		String requestApiKey = request.getHeader("X-API-KEY");
//		String requestApiSecret = request.getHeader("X-API-SECRET");
//		if(requestApiSecret != null )
//		// Validate the key and secret
//		if (apiKey.equals(requestApiKey) && apiSecret.equals(requestApiSecret)) {
//			// Continue processing the request
//			filterChain.doFilter(request, response);
//		} else {
//			// Reject the request and send an unauthorized error
//			response.setStatus(HttpStatus.UNAUTHORIZED.value());
//			response.getWriter().write("Unauthorized1");
//		}
//	}
//}