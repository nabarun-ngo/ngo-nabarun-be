package ngo.nabarun.app.infra.service;

import java.util.List;
import java.util.Map;

import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;

public interface IGlobalDataInfraService {
	
	
	Map<String,  List<KeyValuePair>> getDomainRefConfigs() throws Exception;
	
	Map<String,  List<KeyValuePair>> getDomainLocationData() throws Exception;

	//List<KeyValue> getBankAccounts();//razorpay
	
	String getRulesAndRegulationContent();

}
