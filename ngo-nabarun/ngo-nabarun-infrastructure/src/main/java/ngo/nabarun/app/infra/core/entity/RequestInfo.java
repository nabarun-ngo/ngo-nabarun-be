package ngo.nabarun.app.infra.core.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



/**
 * MongoDB
 * DAO for storing request info in DB
 */
@Document("request_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequestInfo{
	
	@Id
	private String id;
	
	private String firstName;
	
	private String lastName;
	
	private String middleName;
	
	private String email;
	
	private String dialCode;
	
	private String contactNumber;
	
	private String hometown;
	
	private String reasonForJoining;
	
	private String anyOtherSocialWork;
	private String howDoUKnowAboutNabarun;
	
	private boolean emailVerified;
	
	
}
