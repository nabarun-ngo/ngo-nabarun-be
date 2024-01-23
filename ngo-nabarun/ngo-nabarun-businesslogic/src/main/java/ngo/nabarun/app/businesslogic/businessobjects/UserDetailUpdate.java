package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserDetailUpdate {
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("firstName")
	private String firstName;
	
	@JsonProperty("middleName")
	private String middleName;
	
	@JsonProperty("lastName")
	private String lastName;
	
	@JsonProperty("dateOfBirth")
	private Date dateOfBirth;
	
	@JsonProperty("gender")
	private String gender;
	
	@JsonProperty("about")
	private String about;
	
	@JsonProperty("base64Image")
	private String base64Image;
	
	@JsonProperty("removePicture")
	private boolean removePicture;
	
	@JsonProperty("addPicture")
	private boolean addPicture;
	
	@JsonProperty("phones")
	private List<UserPhoneNumber> phoneNumbers;
	
	@JsonProperty("addresses")
	private  List<UserAddress> addresses;
	
	@JsonProperty("socialMediaLinks")
	private List<UserSocialMedia> socialMediaLinks;



}
