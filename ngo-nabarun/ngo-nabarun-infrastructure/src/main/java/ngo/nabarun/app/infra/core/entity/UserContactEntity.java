package ngo.nabarun.app.infra.core.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ngo.nabarun.app.common.enums.DBContactType;

/**
 * MongoDB
 * DAO for storing user contact in DB
 */

@Document("user_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(exclude="user")
public class UserContactEntity{
	
	@Id
	private String id;
	
	private DBContactType contactType;
	
	@DocumentReference
    @JsonBackReference
    @JsonIgnore
	private UserProfileEntity user;
	
	private String emailType;
	private String emailValue;
	
	private String phoneType;
	private String phoneCode;
	private String phoneNumber;
	
	
	private String addressType;
	private String addressLine;
	private String addressHometown;
	private String addressDistrict;
	private String addressState;
	private String addressCountry;
	
	private String socialMediaType;
	private String socialMediaName;
	private String socialMediaURL;

}
