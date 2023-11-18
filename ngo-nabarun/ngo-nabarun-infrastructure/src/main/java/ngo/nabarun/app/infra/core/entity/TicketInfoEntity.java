package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MongoDB
 * DAO for storing tickets in DB
 */
@Document("tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoEntity {

	@Id
	private String id;
	//verification/ or authentication -- enum
	private String type;
	private String scope;

	private String name;
	private String email;
	private String mobileNumber;
	//SMS,EMAIL
	private String communicationMethod;
	private String refId;


	private String oneTimePassword;
	private int incorrectOTPCount;
	private String token;
	private String baseTicketUrl;

	private String status;

	private Date expireOn;
	private Date createdOn;
	private String createdBy;

}
