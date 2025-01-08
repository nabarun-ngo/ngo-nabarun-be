package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
/**
 * MongoDB
 * DAO for storing db_sequence info in DB
 */
@Document("db_sequence")
@Data
public class DBSequenceEntity {

	@Id
	private String id;
	private String name;
	private int seq;
	private Date lastSeqResetOn;
	private Date lastSeqUpdateOn;

	
}
