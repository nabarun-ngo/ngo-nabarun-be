package ngo.nabarun.web.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import ngo.nabarun.web.config.LoggingConfig.NoLogging;



@Component
public class RequestFilterConfig implements Filter {
    public static final String CORRELATION_ID = "Correlation-Id";

    @NoLogging
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String corrId=httpRequest.getHeader(CORRELATION_ID);
		if (corrId == null) {
        	corrId = request.getParameter(CORRELATION_ID);
        }
		if (corrId == null) {
        	corrId = UUID.randomUUID().toString();
        }
        MDC.put("CorrelationId", corrId);
        chain.doFilter(request, response); 
    }
}