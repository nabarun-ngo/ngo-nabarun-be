package ngo.nabarun.app.infra.service;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.infra.dto.DocumentDTO;

@Service
public interface IDocumentInfraService {
	@Async
	DocumentDTO uploadDocument(MultipartFile files, String docIndexId, DocumentIndexType docIndexType) throws ThirdPartyException;

	URL getTempDocumentUrl(String docId,long duration,TimeUnit timeunit) throws ThirdPartyException;

	boolean hardDeleteDocument(String docId);
	
	List<DocumentDTO> getDocumentList(String docRefId,DocumentIndexType documentType);

	@Async
	DocumentDTO uploadDocument(String originalFileName, String contentType, String docIndexId,
			DocumentIndexType docIndexType, byte[] content) throws ThirdPartyException;
}
