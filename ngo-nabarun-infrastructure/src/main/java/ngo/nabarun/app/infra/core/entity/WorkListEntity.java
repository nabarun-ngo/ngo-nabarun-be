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
@Document("worklist")
@Data
public class WorkListEntity {

	@Id
	private String id;
	private String sourceId;
	private String sourceRefId;
	private String description;
	private String sourceStatus;
	private String sourceType;
	private String workType;
	private String pendingWithUserId;
	private String pendingWithUserName;

	private boolean groupWork;
	private String pendingWithRoles;
	private String pendingWithRoleGroups;
	private Date createdOn;
	private String decision;
	private String decisionMakerId;
	private String decisionMakerName;
	private String decisionMakerRoleGroup;
	private String remarks;
	private String currentAction;
	private boolean actionPerformed;
	private Boolean stepCompleted;
	private boolean finalStep;
	private Date decisionDate;

	@DocumentReference(lookup = "{'source':?#{#self._id} }")
	private List<CustomFieldEntity> customFields;


}
