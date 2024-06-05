package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;

@Data
public class DocumentDetailUpload {
	private byte[] content;
	private String base64Content;
	private String originalFileName;
	private String contentType;
}
