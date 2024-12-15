package ngo.nabarun.app.businesslogic;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.RefDataType;
import ngo.nabarun.app.common.util.SecurityUtils.AuthenticatedUser;

@Service
public interface ICommonBL {

	void uploadDocuments(MultipartFile[] files, String docIndexId, DocumentIndexType docIndexType) throws Exception;

	DocumentDetail getDocument(String docId) throws Exception;

	boolean deleteDocument(String docId) throws Exception;
	
	void uploadDocuments(List<DocumentDetailUpload> files,String docIndexId, DocumentIndexType docIndexType) throws Exception;

	Map<String, List<KeyValue>> getReferenceData(List<RefDataType> names, Map<String, String> attr) throws Exception;

	Map<String, List<KeyValue>> getReferenceData(List<RefDataType> names) throws Exception;

	Paginate<Map<String,String>> getNotifications(Integer pageIndex, Integer pageSize);

	List<AdditionalField> getReferenceFields(String identifier) throws Exception;

	List<DocumentDetail> getDocuments(String id, DocumentIndexType type);

	void manageNotification(AuthenticatedUser user,String action,Map<String, Object> payload) throws Exception;

	void cronTrigger(List<ServiceDetail> triggerDetail);


}
