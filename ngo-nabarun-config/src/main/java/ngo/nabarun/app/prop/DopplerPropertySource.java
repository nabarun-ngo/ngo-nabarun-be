package ngo.nabarun.app.prop;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class DopplerPropertySource extends PropertySource{
	
	private String projectName;
	private String configName;
	private String serviceToken;

	public DopplerPropertySource(String projectName,String configName,String serviceToken) {
		this.projectName=projectName;
		this.configName=configName.toLowerCase();
		this.serviceToken=serviceToken;
	}

	@Override
	public Map<String, Object> loadProperties() throws Exception {
		Map<String, Object> propertySource = new HashMap<>();

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            HttpGet httpget = new HttpGet("https://api.doppler.com/v3/configs/config/secrets?project="+projectName+"&config="+configName+"&include_dynamic_secrets=false&include_managed_secrets=false");
            
            httpget.setHeader(HttpHeaders.ACCEPT, "application/json");
            httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer "+serviceToken);
            
            ResponseHandler< String > responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    HttpEntity entity = response.getEntity();
                    throw new ClientProtocolException("Unexpected response status: " + status+" -> "+ EntityUtils.toString(entity));
                }
            };
            
            String responseBody = httpclient.execute(httpget, responseHandler);
            JSONObject respObj=new JSONObject(responseBody);
            JSONObject secrets=respObj.getJSONObject("secrets");
            for(String key:secrets.toMap().keySet()) {
            	propertySource.put(key,secrets.getJSONObject(key).get("raw") );
            }
        }
		return propertySource;
	}

}
