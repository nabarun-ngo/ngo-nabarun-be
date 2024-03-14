package ngo.nabarun.app.infra.service;

import java.util.List;
import java.util.Map;

import ngo.nabarun.app.infra.misc.EmailTemplate;
import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;

public interface IGlobalDataInfraService {
	
	@Deprecated
	EmailTemplate getEmailTemplate(String emailName) throws Exception;
	
	Map<String,  List<KeyValuePair>> getDomainRefConfigs() throws Exception;

	 // get config from doppler
	 //other data xalls like address third party static data
	//@Cacheable("userconfig")
	//@Deprecated
	//UserConfigTemplate getUserConfig() throws Exception;
	
	//@Deprecated
	//@Cacheable("donationconfig")
	//DonationConfigTemplate getDonationConfig() throws Exception;
	
	//String getBusinessErrorMessage(String errorCode) throws Exception;

	
	//List<KeyValue> getBankAccounts();

}
