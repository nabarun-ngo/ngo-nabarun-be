package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document("jobs")
@Data
public class JobEntity {
	@Id
	private String id;
	private String name;
	@Indexed(name = "createdOn", expireAfterSeconds = 2592000)
	private Date createdOn;
	private String status;
	private Date start;
	private Date end;
	private String input;
	private String output;
	private String log;
	private String triggerId;
	private String memoryAtStart;
	private String memoryAtEnd;
	private int retryCount;
	private String errorMessage;
	private String errorCause;
	private String stackTrace;

	
}
