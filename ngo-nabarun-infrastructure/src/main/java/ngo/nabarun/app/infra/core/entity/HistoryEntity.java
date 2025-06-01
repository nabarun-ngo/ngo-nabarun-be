package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document("history")
@Data
public class HistoryEntity {
	
	private String id;	
	private String changes;	
	
	private String createdBy;
	private String createdById;
	private String createdByName;

	private String referenceId;
	private String referenceType;
	@Indexed(name = "createdOn", expireAfterSeconds = 3600*24*30*12*2)
	private Date createdOn;
	private String action;
	
}