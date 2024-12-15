package ngo.nabarun.app.infra.service;

import org.springframework.stereotype.Service;

@Service
public interface ISystemInfraService {

	
	int configureAuthEmailProvider(String sender, String apikey_sg) throws Exception;
	
	
}
