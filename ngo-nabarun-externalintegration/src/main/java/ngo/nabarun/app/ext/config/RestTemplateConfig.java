package ngo.nabarun.app.ext.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import ngo.nabarun.app.common.helper.GenericPropertyHelper;

@Configuration
public class RestTemplateConfig {
	
	@Autowired 
	private GenericPropertyHelper propertyHelper;

    @Bean(name = "restTemplate")
    //@DependsOn({"firebaseApp"})
    RestTemplate restTemplate() {
    	int restTimeoutInSec =propertyHelper.getThirdPartyRestCallTimeout();
		RestTemplate restTemplate = new RestTemplateBuilder()
		        .setConnectTimeout(Duration.ofSeconds(restTimeoutInSec))
		        .setReadTimeout(Duration.ofSeconds(restTimeoutInSec))
		        .build();
        List<ClientHttpRequestInterceptor> interceptors
          = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(new RestTemplateInterceptor());

        restTemplate.setInterceptors(interceptors);
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		return restTemplate;
	}
}
