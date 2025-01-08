package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;

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
		private byte[] content;
		private String base64Content;
		private String originalFileName;
		private String contentType;
	}
}
