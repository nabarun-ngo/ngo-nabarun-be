package ngo.nabarun.app.ext.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.LoginMethod;

@Data
public class AuthUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String firstName;
	private String lastName;
	private String fullName;
	private String username;
	private List<LoginMethod> providers;
	private String email;
	private Boolean emailVerified;
	private Boolean verifyEmail;

	private String userId;
	private String picture;

	private Date createdAt;
	private Date updatedAt;
	//private Map<String, Object> appMetadata;
	private Map<String, Object> attributes= new HashMap<>();
	private String lastIp;
	private Date lastLogin;
	private Date lastPasswordReset;
	private int loginsCount;
	private boolean blocked;
	private String password;
	private String profileId;
	private boolean inactive;
	private Boolean resetPassword;


}
