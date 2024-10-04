package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
/**
 * MongoDB
 * DAO for storing db_sequence info in DB
 */
@Document("dashboard_counts")
@Data
public class DashboardCountEntity {

	@Id
	private String id;
	private String userId;
	private String profileId;
	private String dbFieldKey;
	private String dbFieldValue;
	private Date lastUpdatedOn;

	
}
