package ngo.nabarun.app.infra.dto;

import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.DocumentIndexType;

@Data
public class DocumentDTO {
	private String docId;
	private String fileType;
	private String originalFileName;
	private String remoteFileName;
	private DocumentIndexType documentType;
	private String documentRefId;
	private boolean isImage;
	private String documentURL;
	
	@Data
	public static class DocumentUploadDTO {
		private byte[] content;
		private String originalFileName;
		private String contentType;
		private List<DocumentMappingDTO> documentMapping;
	}
	
	@Data
	public static class DocumentMappingDTO {
		private String docIndexId;
		private DocumentIndexType docIndexType;
	}

}
