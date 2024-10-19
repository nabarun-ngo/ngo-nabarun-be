package ngo.nabarun.app.api.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import ngo.nabarun.app.common.annotation.NoLogging;


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
        MDC.put("CorrelationId", corrId);
        chain.doFilter(request, response); 
    }
}