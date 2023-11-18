package ngo.nabarun.app.infra.service;

import org.springframework.cache.annotation.Cacheable;

import ngo.nabarun.app.infra.misc.EmailTemplate;
import ngo.nabarun.app.infra.misc.UserConfigTemplate;

public interface IDomainRefConfigInfraService {
	
	EmailTemplate getEmailTemplate(String emailName) throws Exception;

	@Cacheable("userconfig")
	UserConfigTemplate getUserConfig() throws Exception;
	
}
