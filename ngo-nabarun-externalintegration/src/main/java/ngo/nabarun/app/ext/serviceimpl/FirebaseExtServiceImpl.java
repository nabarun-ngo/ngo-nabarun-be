package ngo.nabarun.app.ext.serviceimpl;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.Parameter;
import com.google.firebase.remoteconfig.ParameterGroup;
import com.google.firebase.remoteconfig.ParameterValue;
import com.google.firebase.remoteconfig.ParameterValueType;
import com.google.firebase.remoteconfig.Template;
import com.google.gson.Gson;

import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ObjectFilter;
import ngo.nabarun.app.ext.helpers.ObjectFilter.Operator;
import ngo.nabarun.app.ext.helpers.ThirdPartySystem;
import ngo.nabarun.app.ext.objects.RemoteConfig;
import ngo.nabarun.app.ext.service.ICollectionExtService;
import ngo.nabarun.app.ext.service.IFileStorageExtService;
import ngo.nabarun.app.ext.service.IMessageExtService;
import ngo.nabarun.app.ext.service.IRemoteConfigExtService;

@Service
public class FirebaseExtServiceImpl
		implements IRemoteConfigExtService, IFileStorageExtService, IMessageExtService, ICollectionExtService {

	private static final String FIREBASE_DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s";

	@Autowired
	private PropertyHelper propertyHelper;

	@Override
	public String uploadFile(String fileName, MultipartFile multipartFile) throws ThirdPartyException {
		try {
			return uploadFile(fileName, multipartFile.getContentType(), multipartFile.getBytes());
		} catch (ThirdPartyException | IOException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
	}

	@Override
	public String uploadFile(String fileName, String contentType, byte[] content) throws ThirdPartyException {
		String token = UUID.randomUUID().toString();
		String fileStorageBucket = propertyHelper.getFirebaseFileStorageBucket();
		BlobId blobId = BlobId.of(fileStorageBucket, fileName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType)
				.setMetadata(Map.of("firebaseStorageDownloadTokens", token)).build();
		Blob blob;
		try {
			blob = StorageClient.getInstance().bucket(fileStorageBucket).getStorage().create(blobInfo, content);
			return String.format(FIREBASE_DOWNLOAD_URL, URLEncoder.encode(fileStorageBucket, StandardCharsets.UTF_8),
					URLEncoder.encode(blob.getName(), StandardCharsets.UTF_8),
					URLEncoder.encode(token, StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}

	}

	@Override
	public boolean removeFileByFilename(String fileName) {
		if (fileName == null) {
			return false;
		}
		String fileStorageBucket = propertyHelper.getFirebaseFileStorageBucket();
		Blob file = StorageClient.getInstance().bucket(fileStorageBucket).get(fileName);
		return file != null ? file.delete() : false;
	}

	@Override
	public URL getTemporaryDownloadUrl(String fileName, long duration, TimeUnit unit) throws ThirdPartyException {
		String fileStorageBucket = propertyHelper.getFirebaseFileStorageBucket();
		Blob blob = StorageClient.getInstance().bucket(fileStorageBucket).get(fileName);
		return blob.signUrl(duration, unit);
	}

	@Cacheable(value = "domain_global_config",key="'all_value'")
	@Override
	public List<RemoteConfig> getRemoteConfigs() throws ThirdPartyException {
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
					rc.setValue(mapValue.get("value"));
					firebaseConfig.add(rc);
				}
			}
			return firebaseConfig;

		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}

	}
	@Cacheable(value = "DOMAIN_GLOBAL_CONFIG", key = "#configKey")
	@Override
	public RemoteConfig getRemoteConfig(String configKey) throws ThirdPartyException {
		return getRemoteConfigs().stream().filter(f -> f.getName().equalsIgnoreCase(configKey)).findFirst().get();
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

	@Cacheable("RemoteConfigParameterGroups")
	@Override
	public List<String> getRemoteConfigParameterGroups() throws ThirdPartyException {
		try {
			return FirebaseRemoteConfig.getInstance().getTemplateAsync().get().getParameterGroups().keySet().stream()
					.toList();
		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
	}

	@Override
	public <T> List<T> getCollectionData(String collectionName, Integer page, Integer size,
			List<ObjectFilter> filters,Class <T> valueType) throws ThirdPartyException {
		Firestore db = FirestoreClient.getFirestore();
		List<T> collections = new ArrayList<>();
		try {
			Query documentQuery = db.collection(collectionName);
			if (filters != null) {
				for (ObjectFilter filter : filters) {
					if (filter.getOperator() == Operator.EQUAL) {
						documentQuery = documentQuery.whereEqualTo(filter.getKey(), filter.getValue());
					} else if (filter.getOperator() == Operator.CONTAIN) {
						documentQuery = documentQuery.whereArrayContains(filter.getKey(), filter.getValue());
					} else if (filter.getOperator() == Operator.IN) {
						// System.out.println(filter.getKey()+" "+ filter.getValue());
						documentQuery = documentQuery.whereIn(filter.getKey(), List.of(filter.getValue()));
					} else if (filter.getOperator() == Operator.ARRAY_CONTAIN) {
						documentQuery = documentQuery.whereArrayContainsAny(filter.getKey(),
								List.of(filter.getValue()));
					}
				}
			}

			if (page != null && size != null) {
				documentQuery = documentQuery.limit(size);
			}
			List<QueryDocumentSnapshot> documents = documentQuery.get().get().getDocuments();
			for (QueryDocumentSnapshot document : documents) {
				if (document.exists()) {
					//document.getData()
					collections.add(document.toObject(valueType));
				}
			}

		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
		return collections;
	}

	@Override
	public <T> T storeCollectionData(String collectionName,String id, T item)
			throws ThirdPartyException {

		try {
			Firestore db = FirestoreClient.getFirestore();
			db.collection(collectionName).document(id).set(item).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
		return item;
	}


	@Override
	public Map<String, Object> updateCollectionData(String collectionName, String id, Map<String, Object> items)
			throws ThirdPartyException {
		try {
			Firestore db = FirestoreClient.getFirestore();
			List<String> keysToUpdate = new ArrayList<>();
			for (String key : items.keySet()) {
				keysToUpdate.add(key);
			}
			db.collection(collectionName).document(id).set(items, SetOptions.mergeFields(keysToUpdate)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
		return items;
	}

	@Override
	public void removeCollectionData(String collectionName, String id) throws ThirdPartyException {
		try {
			Firestore db = FirestoreClient.getFirestore();
			db.collection(collectionName).document(id).delete().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
	}

	@Override
	public List<String> sendMessage(String title, String body, String imageUrl, List<String> tokens,
			Map<String, String> data) throws ThirdPartyException {
		List<String> msgIds = new ArrayList<>();
		FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
		Notification notification = Notification.builder().setTitle(title).setBody(body).setImage(imageUrl).build();
		for (String token : tokens) {
			try {
				Message message = Message.builder().setToken(token).setNotification(notification).putAllData(data)
						.build();
				msgIds.add(firebaseMessaging.send(message));
			} catch (FirebaseMessagingException e) {
				throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
			}
		}
		return msgIds;
	}

}