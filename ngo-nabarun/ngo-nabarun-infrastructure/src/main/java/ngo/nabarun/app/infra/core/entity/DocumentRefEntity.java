package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing attachments info in DB
 */
@Document("attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DocumentRefEntity {
	
	@Id
	private String id;

	private String fileType;

	private String downloadUrl;

	private String originalFileName;
	
	private String remoteFileName;

	private String attachementIdentifier;

	private boolean deleted;

    @CreatedBy
	private String createdBy;

	@CreatedDate
	private Date createdOn;
	
	private String documentType;
	private String documentRefId;

}
