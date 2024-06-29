package ngo.nabarun.app.ext.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ThirdPartySystem;
import ngo.nabarun.app.ext.objects.RazorPayBankList;
import ngo.nabarun.app.ext.objects.Secret;
import ngo.nabarun.app.ext.objects.SecretList;
import ngo.nabarun.app.ext.objects.UpdateSecret;
import ngo.nabarun.app.ext.service.IMiscExtService;
import ngo.nabarun.app.ext.service.ISecretExtService;

@Service
public class DopplerSecretAndMiscExtService implements ISecretExtService, IMiscExtService {

	@Autowired
	private GenericPropertyHelper propertyHelper;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public SecretList getSecretList() throws ThirdPartyException {
		String url = "https://api.doppler.com/v3/configs/config/secrets";

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("project", propertyHelper.getDopplerProject())
				.queryParam("config", propertyHelper.getDopplerConfigName())
				.queryParam("include_dynamic_secrets", false).queryParam("include_managed_secrets", false);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + propertyHelper.getDopplerServiceKey());
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<SecretList> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
					requestEntity, SecretList.class);
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.DOPPLER);
		}
	}

	@Override
	public Secret getSecret(String name) throws ThirdPartyException {
		String url = "https://api.doppler.com/v3/configs/config/secret";

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("project", propertyHelper.getDopplerProject())
				.queryParam("config", propertyHelper.getDopplerConfigName()).queryParam("name", name);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + propertyHelper.getDopplerServiceKey());
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<Secret> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
					requestEntity, Secret.class);
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.DOPPLER);
		}
	}

	@Override
	public SecretList addOrUpdateSecret(UpdateSecret updateSecret) throws ThirdPartyException {
		String url = "https://api.doppler.com/v3/configs/config/secrets";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + propertyHelper.getDopplerServiceKey());
		HttpEntity<UpdateSecret> requestEntity = new HttpEntity<>(updateSecret, headers);
		try {
			ResponseEntity<SecretList> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					SecretList.class);
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.DOPPLER);
		}
	}

	@Override
	public RazorPayBankList getRazorPayBanks(int offset, int limit, String state, String city, String bankcode)
			throws ThirdPartyException {
		String url = "https://ifsc.razorpay.com/search";

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("limit", limit)
				.queryParam("offset", offset);

		if (state != null) {
			builder = builder.queryParam("state", state);
		}

		if (city != null) {
			builder = builder.queryParam("city", city);
		}

		if (bankcode != null) {
			builder = builder.queryParam("bankcode", bankcode);
		}

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<RazorPayBankList> responseEntity = restTemplate.exchange(builder.toUriString(),
					HttpMethod.GET, requestEntity, RazorPayBankList.class);
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.RAZORPAY);
		}
	}

}
