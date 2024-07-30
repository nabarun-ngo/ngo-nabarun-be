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


@Component
public class FilterConfig implements Filter {
    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        MDC.put(CORRELATION_ID, httpRequest.getHeader(CORRELATION_ID));
        System.err.println(MDC.get(CORRELATION_ID));
        //log.info("Intercept coming request and set MDC context information");
        // pass the request
        chain.doFilter(request, response);
    }
}