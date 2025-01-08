package ngo.nabarun.app.ext.service;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.Secret;
import ngo.nabarun.app.ext.objects.SecretList;
import ngo.nabarun.app.ext.objects.UpdateSecret;

@Service
public interface ISecretExtService {
	
	SecretList getSecretList() throws ThirdPartyException;
	Secret getSecret(String name) throws ThirdPartyException;
	SecretList addOrUpdateSecret(UpdateSecret updateSecret) throws ThirdPartyException;
}