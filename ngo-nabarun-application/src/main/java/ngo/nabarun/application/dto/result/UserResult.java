package ngo.nabarun.application.dto.result;

import java.util.Date;
import java.util.List;
import lombok.Data;
import ngo.nabarun.domain.user.enums.LoginMethod;
import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.model.Address;
import ngo.nabarun.domain.user.model.Link;
import ngo.nabarun.domain.user.model.PhoneNumber;
import ngo.nabarun.domain.user.model.Role;


@Data
public class UserResult {
	
	private String id;
	private String title;	
	private String firstName;	
	private String middleName;	
	private String lastName;	
	private Date dateOfBirth;	
	private String gender;	
	private String about;	
	private String picture;	
	private List<Role> roles;	
	private String email;	
	private PhoneNumber primaryNumber;
	private PhoneNumber secondaryNumber;
	private Address presentAddress;
	private Address permanentAddress;
	private List<Link> socialMediaLinks;
	private Date createdOn;
	private boolean activeDonor;	
	private boolean publicProfile;	
	private String userId;	
	private UserStatus status;
	private List<LoginMethod> loginMethod;
	private Boolean addressSame;
	private Boolean profileCompleted;
	
}