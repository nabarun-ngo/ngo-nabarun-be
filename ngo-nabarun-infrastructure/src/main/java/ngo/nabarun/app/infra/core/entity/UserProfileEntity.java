package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * MongoDB
 * DAO for storing profiles in DB
 */

@Document("user_profiles")
@Data
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
	private String about;

	private String roleNames;
	private String roleCodes;
	@Indexed(unique = true)
	private String email;
	private String phoneNumber;
	private String altPhoneNumber;


	private Date createdOn;
	private String createdBy;
	
	private String userId;
	private Boolean activeContributor;
	private Boolean publicProfile;
	private String status;
	
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String hometown;
	private String district;
	private String state;
	private String country;
	
	private String facebookLink;
	private String instagramLink;
	private String twitterLink;
	private String linkedInLink;
	private String whatsappLink;

	private boolean deleted;
	
	private String permanentAddressLine1;
	private String permanentAddressLine2;
	private String permanentAddressLine3;
	private String permanentHometown;
	private String permanentDistrict;
	private String permanentState;
	private String permanentCountry;
	private Boolean presentPermanentSame;
	private Date donationPauseStartDate;
	private Date donationPauseEndDate;
	private String loginMethods;


}
