package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;

@Data
public class DocumentDetail {
	private String docId;
	private String originalFileName;
	private String documentRefId;
	private boolean isImage;
}
