package ngo.nabarun.app.ext.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ComponentScan
@Slf4j
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		if(!request.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
			request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		}
		request.getHeaders().set("X-Correlation-ID", UUID.randomUUID().toString());
		logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(request,response);
		return response;
	}
	
	
	private void logRequest(HttpRequest request, byte[] body) throws IOException 
    {
        if (log.isDebugEnabled()) 
        {
        	List<String> authorization = null;
        	if(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
        		authorization=request.getHeaders().getValuesAsList(HttpHeaders.AUTHORIZATION);
        		request.getHeaders().replace(HttpHeaders.AUTHORIZATION, List.of("<token_type> <access_token_value>"));
        	}
            log.debug("===========================request begin================================================");
           // log.debug("URI         : {}", request.getURI());
            log.debug("Method      : {}", request.getMethod());
            log.debug("Headers     : {}", request.getHeaders());
//            if(request.getHeaders().containsKey(ApplicationProperties.TOKEN_ENDPOINT)) {
//                log.debug("Request body: {}", "<Request body> *** This Request contains credentials. Skipped due to security constraints ***");
//            }else {
                log.debug("Request body: {}", new String(body, "UTF-8"));
//            }
            log.debug("==========================request end===================================================");
            if(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
        		request.getHeaders().replace(HttpHeaders.AUTHORIZATION,authorization);
        	}
        }
    }
 
	
    private void logResponse(HttpRequest request, ClientHttpResponse response) throws IOException 
    {
        if (log.isDebugEnabled()) 
        {
        	/*
        	ClientHttpResponse newCopiedResponse = new BufferingClientHttpResponseWrapper(response);
    		StringBuilder inputStringBuilder = new StringBuilder();
    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(newCopiedResponse.getBody(), "UTF-8"));
    		String line = bufferedReader.readLine();
    		while (line != null) {
    			inputStringBuilder.append(line);
    			line = bufferedReader.readLine();
    		}
    		*/
            log.debug("============================response begin==========================================");
            log.debug("Status code  : {}", response.getStatusCode());
            log.debug("Status text  : {}", response.getStatusText());
            log.debug("Headers      : {}", response.getHeaders());
            /*
            if(request.getHeaders().containsKey(ApplicationProperties.TOKEN_ENDPOINT)) {
                log.debug("Response body: {}", "<response body> *** This response contains access token. Skipped due to security constraints ***");
            }else {
                log.debug("Response body: {}", inputStringBuilder.toString());
            }
            */
            log.debug("=======================response end=================================================");
        }
    }
   

   
}

