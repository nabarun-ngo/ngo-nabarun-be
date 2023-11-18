package ngo.nabarun.app.ext.serviceimpl.firebase;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.firebase.cloud.StorageClient;

import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ThirdPartySystem;
import ngo.nabarun.app.ext.service.IFileStorageExtService;

@Service
public class FirebaseFileStorageExtServiceImpl implements IFileStorageExtService {

	private static final String FIREBASE_DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s";
	
	@Autowired 
	private GenericPropertyHelper propertyHelper;

	@Override
	public String uploadFile(String fileName, MultipartFile multipartFile) throws ThirdPartyException {
		try {
			return uploadFile(fileName, multipartFile.getContentType(),multipartFile.getBytes());
		} catch (ThirdPartyException | IOException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.FIREBASE);
		}
	}
	
	@Override
	public String uploadFile(String fileName, String contentType,byte[] content) throws ThirdPartyException {
		String token = UUID.randomUUID().toString();
		String fileStorageBucket=propertyHelper.getFirebaseFileStorageBucket();
		BlobId blobId = BlobId.of(fileStorageBucket, fileName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType)
				.setMetadata(Map.of("firebaseStorageDownloadTokens", token)).build();
		Blob blob;
		try {
			blob = StorageClient.getInstance().bucket(fileStorageBucket).getStorage().create(blobInfo,
					content);
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
		String fileStorageBucket=propertyHelper.getFirebaseFileStorageBucket();
		Blob file = StorageClient.getInstance().bucket(fileStorageBucket).get(fileName);
		return file != null ? file.delete() : false;
	}

	@Override
	public URL getTemporaryDownloadUrl(String fileName, long duration, TimeUnit unit) throws ThirdPartyException {
		String fileStorageBucket=propertyHelper.getFirebaseFileStorageBucket();
		Blob blob = StorageClient.getInstance().bucket(fileStorageBucket).get(fileName);
		return blob.signUrl(duration, unit);
	}

}