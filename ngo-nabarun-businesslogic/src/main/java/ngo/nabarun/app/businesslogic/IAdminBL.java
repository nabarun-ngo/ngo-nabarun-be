package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.ApiKeyDetail;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;


@Service
public interface IAdminBL {
	
	void clearSystemCache(List<String> names);

	void adminServices(ServiceDetail trigger) throws Exception;

	ApiKeyDetail generateApiKey(ApiKeyDetail apiKeyDetail);
	ApiKeyDetail updateApiKey(String id, ApiKeyDetail keyDetail,boolean revoke);
	List<ApiKeyDetail> getApiKeys();

	List<KeyValue> getApiKeyScopes() throws Exception;

}
