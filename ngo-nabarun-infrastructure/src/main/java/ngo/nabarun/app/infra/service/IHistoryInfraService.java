package ngo.nabarun.app.infra.service;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.enums.HistoryRefType;
import ngo.nabarun.app.common.util.SecurityUtils.AuthenticatedUser;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.infra.dto.HistoryDTO;

@Service
public interface IHistoryInfraService {
	@Async
	void logCreation(HistoryRefType type, String refId,AuthenticatedUser aUser, Map<String,Object> object) throws ThirdPartyException;
	@Async
	void logUpdate(HistoryRefType type, String refId,AuthenticatedUser aUser, Map<String,Object> object1,Map<String,Object> object2) throws ThirdPartyException;
	
	List<HistoryDTO> getHistory(HistoryRefType type, String refId) throws ThirdPartyException;

}
