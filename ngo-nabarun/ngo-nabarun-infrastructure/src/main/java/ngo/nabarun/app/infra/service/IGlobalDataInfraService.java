package ngo.nabarun.app.infra.service;

import java.util.List;
import java.util.Map;

import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;

public interface IGlobalDataInfraService {
	
	@Deprecated
	EmailTemplateDTO getEmailTemplate(String emailName) throws Exception;
	
	Map<String,  List<KeyValuePair>> getDomainRefConfigs() throws Exception;
	


	
	//List<KeyValue> getBankAccounts();

}
