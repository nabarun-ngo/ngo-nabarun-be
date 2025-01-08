package ngo.nabarun.app.ext.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.RemoteConfig;

@Service
public interface IRemoteConfigExtService {
	
	List<RemoteConfig> getRemoteConfigs() throws ThirdPartyException;
	RemoteConfig getRemoteConfig(String configKey) throws ThirdPartyException;
	RemoteConfig addOrUpdateRemoteConfig(RemoteConfig config) throws ThirdPartyException;
	List<String> getRemoteConfigParameterGroups() throws ThirdPartyException;
}