package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

/**
 * MongoDB
 * DAO for storing attachments info in DB
 */
@Document("document_references")
@Data
public class DocumentRefEntity {
	
	@Id
	private String id;

	private String fileType;

	private String downloadUrl;

	private String originalFileName;
	
	private String remoteFileName;

	private String attachementIdentifier;

	private boolean deleted;

	private String createdBy;

	private Date createdOn;
	
	private String documentType;
	private String documentRefId;

}
