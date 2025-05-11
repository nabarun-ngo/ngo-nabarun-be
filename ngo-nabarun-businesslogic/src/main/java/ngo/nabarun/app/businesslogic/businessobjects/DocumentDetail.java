package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.DocumentIndexType;

@Data
public class DocumentDetail {
	private String docId;
	private String originalFileName;
	private String documentIndexId;
	private boolean isImage;
	private boolean isGeneratedDoc;
	private String downloadURL;

	@Data
	public static class DocumentDetailUpload {
		private String base64Content;
		private String originalFileName;
		private String contentType;
		private List<DocumentMapping> documentMapping;
	}
	
	@Data
	public static class DocumentMapping {
		private String docIndexId;
		private DocumentIndexType docIndexType;
	}
}
