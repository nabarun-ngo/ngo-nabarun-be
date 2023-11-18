package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MongoDB
 * DAO for storing profiles in DB
 */

@Document("user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity{
	
	@Id
	private String id;
	
	private String title;
	
	private String firstName;
	
	private String middleName;
	
	private String lastName;
	
	private String avatarUrl;
	
	@DateTimeFormat(pattern="dd/MM/yyyy")
	private Date dateOfBirth;
	
	private String gender;
	
	private String roleString;
	
	@Indexed(unique = true)
	private String email;
	
	private String about;
	
	@DocumentReference(lookup = "{ 'user' : ?#{#self._id} }")
	@JsonManagedReference
	private List<UserContactEntity> contacts;

	
	@CreatedDate
	private Date createdOn;
	
	@CreatedBy
	private String createdBy;
	
	private String userId;

	private Boolean activeContributor;
	
	private Boolean displayPublic;
	private String profileStatus;
	
	

	private boolean deleted;
    
    
	private String phoneNumberString;
      

    

}
