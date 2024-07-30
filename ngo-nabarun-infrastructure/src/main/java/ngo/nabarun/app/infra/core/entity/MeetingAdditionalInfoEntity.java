package ngo.nabarun.app.infra.core.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing additional_meeting_info  info in DB
 */
@Document("meeting_additional_info" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Deprecated
public class MeetingAdditionalInfoEntity {
	@Id
	private String id;
	private boolean attendeeDetail;
	private String attendeeEmail;
	private String attendeeName;

	private boolean discussionDetail;
	private String agenda;
	private String minutes;
	
	@DocumentReference
	private MeetingEntity meeting;
}
