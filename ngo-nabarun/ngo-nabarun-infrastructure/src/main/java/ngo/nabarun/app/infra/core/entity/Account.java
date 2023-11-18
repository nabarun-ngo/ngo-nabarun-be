package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * MongoDB
 * DAO for storing accounts info in DB
 */
@Document("accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Account {

	@Id
	private String id;
	private String accountType;
	private String accountNumber;
	private Double currentBalance;
	private Double openingBalance;
	@DocumentReference
	private UserProfileEntity profile;
	private String accountStatus;
	private Date activatedOn;
	private Date createdOn;
}
