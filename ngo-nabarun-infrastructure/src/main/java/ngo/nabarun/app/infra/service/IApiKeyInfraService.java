package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;

@Service
public interface IApiKeyInfraService {
	ApiKeyDTO createOrUpdateApiKey(ApiKeyDTO apiKeyDTO);
	ApiKeyDTO getApiKeyDetail(String apiKey);
	List<ApiKeyDTO> getApiKeys(ApiKeyStatus status);

}
