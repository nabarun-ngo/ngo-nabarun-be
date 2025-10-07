package ngo.nabarun.application.dto.command;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.domain.user.enums.LoginMethod;
import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.model.Address;
import ngo.nabarun.domain.user.model.Link;
import ngo.nabarun.domain.user.model.PhoneNumber;
import ngo.nabarun.domain.user.model.Role;

public class UserCommand {
	
	@Data
	public static class UserSelfUpdateCommand {
		private String title;	
    	private String firstName;	
    	private String middleName;	
    	private String lastName;	
    	private Date dateOfBirth;	
    	private String gender;	
    	private String about;	
    	private String picture;	
    	private PhoneNumber primaryNumber;
    	private PhoneNumber secondaryNumber;
    	private Address presentAddress;
    	private Address permanentAddress;
    	private List<Link> socialMediaLinks;
    	private Boolean isPublicProfile;	
    	private Boolean isAddressSame;
	}
	
	@Data
	public static class UserUpdateCommand {
		private List<Role> roles;	
    	private Boolean isActiveDonor;	
    	private String userId;	
    	private UserStatus status;
    	private List<LoginMethod> loginMethod;
	}
	
	@Data
	public static class UserCreateCommand {
		private String firstName;
		private String lastName;
		private String email;
		private String phoneCode;
		private String phoneNumber;

	}

	@Data
	public static class UserSearchCommand {
		
	}
}
