package ngo.nabarun.app.businesslogic;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.RefDataType;

@Service
public interface ICommonBL {

	void uploadDocuments(MultipartFile[] files, String docIndexId, DocumentIndexType docIndexType) throws Exception;

	URL getDocumentUrl(String docId) throws Exception;

	boolean deleteDocument(String docId) throws Exception;
	
	void clearSystemCache(List<String> names);

	void uploadDocuments(List<DocumentDetailUpload> files,String docIndexId, DocumentIndexType docIndexType) throws Exception;

	Map<String, List<KeyValue>> getReferenceData(List<RefDataType> names, Map<String, String> attr) throws Exception;

	Map<String, List<KeyValue>> getReferenceData(List<RefDataType> names) throws Exception;

	Paginate<Map<String,String>> getNotifications(Integer pageIndex, Integer pageSize);

	List<AdditionalField> getReferenceFields(String identifier) throws Exception;

	void manageNotification(String userId, String action, Map<String, Object> payload) throws Exception;


}
