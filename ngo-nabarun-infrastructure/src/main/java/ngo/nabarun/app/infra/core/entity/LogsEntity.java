package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

/**
 * MongoDB
 * DAO for storing meeting info in DB
 */
@Document("logs_info" )
@Data
public class LogsEntity {
	@Id
	private String id;
	private String type;
	private String methodName;
	private String inputs;
	private String outputs;
	private Date startTime;
	private Date endTime;
	private String error;

	private String corelationId;
	
}
