package ngo.nabarun.app.infra.service;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface ICountsInfraService {
	int getEntiryLastSequence(String seqName); 
	Date getEntiryLastResetDate(String seqName); 
	int incrementEntirySequence(String seqName); 
	int decrementEntitySequence(String seqName); 
	int resetEntirySequence(String seqName); 
	
	Map<String,String> getDashboardCounts(String userId);
	Map<String, String> addOrUpdateDashboardCounts(String userId, Map<String, String> map);

}
