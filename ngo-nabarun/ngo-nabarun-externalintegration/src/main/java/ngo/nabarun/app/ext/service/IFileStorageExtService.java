package ngo.nabarun.app.ext.service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.ext.exception.ThirdPartyException;

@Service
public interface IFileStorageExtService {

	String uploadFile(String fileName,MultipartFile multipartFile) throws ThirdPartyException;
	boolean removeFileByFilename(String fileName);
	URL getTemporaryDownloadUrl(String fileName,long duration, TimeUnit unit) throws ThirdPartyException;
	String uploadFile(String fileName, String contentType, byte[] content) throws ThirdPartyException;

	
}