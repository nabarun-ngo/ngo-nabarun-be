package ngo.nabarun.app.infra.service;

import java.util.List;

import ngo.nabarun.app.infra.dto.LogsDTO;

public interface ILogInfraService {

	List<LogsDTO> getLogs(String corelationId);
	
	LogsDTO saveLog(LogsDTO logsDTO);
}
