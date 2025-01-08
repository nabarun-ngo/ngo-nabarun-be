package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * MongoDB
 * DAO for storing earning info in DB
 */
@Document("earnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Earning {

	@Id
	private String id;

	private String source;

	private String description;

	private Double amount;

	private Date earningDate;

	@CreatedBy
	private String createdBy;

	@CreatedDate
	private Date createdOn;

	private boolean deleted;

	private boolean approved;
	
	private String transactionRefNumber;


}
