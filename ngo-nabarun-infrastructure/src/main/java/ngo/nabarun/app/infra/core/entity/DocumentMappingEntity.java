package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Data;

/**
 * MongoDB DAO for storing attachments info in DB
 */
@Document("document_mappings")
@Data
public class DocumentMappingEntity {

	@Id
	private String id;
	private String documentId;
	private Date createdOn;
	private String documentType;
	private String documentRefId;

	@DocumentReference(lookup = "{'_id':?#{#self.documentId} }")
	private DocumentRefEntity documentRef;

}
