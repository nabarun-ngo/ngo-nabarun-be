package ngo.nabarun.app.businesslogic;

import java.net.URL;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.businesslogic.businessobjects.AuthorizationDetail;
import ngo.nabarun.app.common.enums.DocumentIndexType;

@Service
public interface ICommonBL {

	void uploadDocuments(MultipartFile[] files, String docIndexId, DocumentIndexType docIndexType) throws Exception;

	URL getDocumentUrl(String docId) throws Exception;

	boolean deleteDocument(String docId) throws Exception;

	String generateAuthorizationUrl(AuthorizationDetail authDetail) throws Exception;
	
	void clearSystemCache(List<String> names);

}
