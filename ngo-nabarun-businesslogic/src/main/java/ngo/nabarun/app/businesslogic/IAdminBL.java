package ngo.nabarun.app.businesslogic;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.CronServiceDetail;


@Service
public interface IAdminBL {
	Map<String, String> generateApiKey(List<String> scopes);

	void cronTrigger(List<CronServiceDetail> triggerDetail);
	
	void clearSystemCache(List<String> names);

	void adminServices(CronServiceDetail trigger) throws Exception;

	Map<String, String> updateApiKey(String id, List<String> scopes, boolean revoke);

}
