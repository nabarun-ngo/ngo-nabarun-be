package ngo.nabarun.app.infra.dto;

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

}
