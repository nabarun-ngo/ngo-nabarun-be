package ngo.nabarun.app.ext.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.Parameter;
import com.google.firebase.remoteconfig.ParameterGroup;
import com.google.firebase.remoteconfig.ParameterValue;
import com.google.firebase.remoteconfig.ParameterValueType;
import com.google.firebase.remoteconfig.Template;
import com.google.gson.Gson;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ThirdPartySystem;
import ngo.nabarun.app.ext.objects.RemoteConfig;
import ngo.nabarun.app.ext.service.IRemoteConfigExtService;

@Service
public class FirebaseRemoteConfigExtServiceImpl implements IRemoteConfigExtService {

	@Cacheable("remote_configs")
	@Override
	public List<RemoteConfig> getRemoteConfigs() throws ThirdPartyException {
		System.out.println("remote configggggg");
		List<RemoteConfig> firebaseConfig = new ArrayList<>();
		try {
			Gson gson = new Gson();
			Template template = FirebaseRemoteConfig.getInstance().getTemplateAsync().get();
			for (Entry<String, Parameter> parameter : template.getParameters().entrySet()) {
				RemoteConfig rc = new RemoteConfig();
				rc.setName(parameter.getKey());
				rc.setDescription(parameter.getValue().getDescription());
				rc.setType(parameter.getValue().getValueType().name());
				String jsonValue = gson.toJson(parameter.getValue().getDefaultValue());
				//String jsonValue = CommonUtils.convertToType(parameter.getValue().getDefaultValue(), new TypeReference<String>() {});

				//Map<String, String> mapValue=CommonUtils.convertToType(jsonValue, new TypeReference<Map<String,String>>() {});
				@SuppressWarnings("unchecked")
				Map<String, String> mapValue = gson.fromJson(jsonValue, Map.class);
				rc.setValue(mapValue.get("value"));
				firebaseConfig.add(rc);
			}
			for (Entry<String, ParameterGroup> paramGroup : template.getParameterGroups().entrySet()) {
				for (Entry<String, Parameter> parameter : paramGroup.getValue().getParameters().entrySet()) {
					RemoteConfig rc = new RemoteConfig();
					rc.setName(parameter.getKey());
					rc.setDescription(parameter.getValue().getDescription());
					rc.setGroup(paramGroup.getKey());
					rc.setType(parameter.getValue().getValueType().name());
					String jsonValue = gson.toJson(parameter.getValue().getDefaultValue());
					@SuppressWarnings("unchecked")
					Map<String, String> mapValue = gson.fromJson(jsonValue, Map.class);
//					String jsonValue = CommonUtils.convertToType(parameter.getValue().getDefaultValue(), new TypeReference<String>() {});
//					Map<String, String> mapValue=CommonUtils.convertToType(jsonValue, new TypeReference<Map<String,String>>() {});
					rc.setValue(mapValue.get("value"));
					firebaseConfig.add(rc);
				}
			}
			return firebaseConfig;

		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}

	}
	
	@Cacheable("remote_config-#configKey")
	@Override
	public RemoteConfig getRemoteConfig(String configKey) throws ThirdPartyException {
		return getRemoteConfigs().stream().filter(f->f.getName().equalsIgnoreCase(configKey)).findFirst().get();
	}

	@Override
	public RemoteConfig addOrUpdateRemoteConfig(RemoteConfig config) throws ThirdPartyException {
		FirebaseRemoteConfig fbInstance = FirebaseRemoteConfig.getInstance();
		try {
			Template template = fbInstance.getTemplateAsync().get();
			template.getParameterGroups().get(config.getGroup()).getParameters().put(config.getName(),
					new Parameter().setDefaultValue(ParameterValue.of(String.valueOf(config.getValue())))
							.setValueType(ParameterValueType.valueOf(config.getType()))
							.setDescription(config.getDescription()));
			template = fbInstance.validateTemplateAsync(template).get();
			template = fbInstance.publishTemplateAsync(template).get();
			return config;

		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}

	}

	@Override
	public List<String> getRemoteConfigParameterGroups() throws ThirdPartyException {
		try {
			return FirebaseRemoteConfig.getInstance().getTemplateAsync().get()
					.getParameterGroups().keySet().stream().toList();
		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
	}

}