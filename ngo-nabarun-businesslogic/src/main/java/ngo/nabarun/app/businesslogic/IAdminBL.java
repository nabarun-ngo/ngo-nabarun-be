package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.ApiKeyDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail.JobDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;


@Service
public interface IAdminBL {
	
	void clearSystemCache(List<String> names);

	void adminServices(ServiceDetail trigger) throws Exception;

	ApiKeyDetail generateApiKey(ApiKeyDetail apiKeyDetail);
	ApiKeyDetail updateApiKey(String id, ApiKeyDetail keyDetail,boolean revoke);
	List<ApiKeyDetail> getApiKeys();

	List<KeyValue> getApiKeyScopes() throws Exception;

	Paginate<JobDetail> getJobList(Integer pageIndex, Integer pageSize, JobDetailFilter filter);

}
