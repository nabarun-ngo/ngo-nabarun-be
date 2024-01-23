package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;

@Data
public class DocumentDetailUpload {
	private String base64Content;
	private String originalFileName;
	private String contentType;
}
