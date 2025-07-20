package ngo.nabarun.app.infra.service;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO.DocumentMappingDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO.DocumentUploadDTO;

@Service
public interface IDocumentInfraService {
	
	DocumentDTO uploadDocument(MultipartFile file, List<DocumentMappingDTO> documentMapping) throws ThirdPartyException;

	URL getTempDocumentUrl(String docId,long duration,TimeUnit timeunit) throws ThirdPartyException;

	boolean hardDeleteDocument(String docId);
	
	List<DocumentDTO> getDocumentList(String docRefId,DocumentIndexType documentType);

	
	DocumentDTO uploadDocument(DocumentUploadDTO documentUploadDTO) throws ThirdPartyException;

	void createDocumentIndex(String documentId,List<DocumentMappingDTO> documentMapping);

	
}
