package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * MongoDB
 * DAO for storing events info in DB
 */

@Document("events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class SocialEventEntity {
	

	@Id
	private String id;
	
	private String title;
	
	private String description;
	
	private Date eventDate;
	
	private String eventLocation;
	
	private String eventState;
	
	private String coverPicture;
	
	private String createdById;
	private String createdByName;

	private Date createdOn;
	
	private Boolean draft;
	
	private Double eventBudget;
	private Double eventExpense;

	
	private boolean deleted;
	
}
