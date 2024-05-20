package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Data;

/**
 * MongoDB DAO for storing request in DB
 */
@Document("workflow")
@Data
public class WorkflowEntity {

	@Id
	private String id;
	private String name;
	private String type;
	private String status;
	private String lastStatus;
	private boolean lastActionCompleted;
	private String description;
	private String remarks;
	private Date createdOn;
	private String createdBy;
	private Date resolvedOn;

	private String profileId;
	private String profileName;
	private String profileEmail;

	

	private boolean delegated;

	private String delegateProfileId;
	private String delegateProfileName;
	private String delegateProfileEmail;


	@DocumentReference(lookup = "{'source':?#{#self._id} }")
	private List<CustomFieldEntity> customFields;
}
