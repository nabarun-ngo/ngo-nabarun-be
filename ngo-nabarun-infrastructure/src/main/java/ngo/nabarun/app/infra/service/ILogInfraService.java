package ngo.nabarun.app.infra.service;

import java.util.List;

import ngo.nabarun.app.common.annotation.NoLogging;
import ngo.nabarun.app.infra.dto.LogsDTO;

public interface ILogInfraService {

	@NoLogging
	List<LogsDTO> getLogs(String corelationId);
	
	@NoLogging
	LogsDTO saveLog(LogsDTO logsDTO);
}
