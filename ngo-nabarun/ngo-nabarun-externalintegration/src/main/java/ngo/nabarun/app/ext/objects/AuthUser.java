package ngo.nabarun.app.ext.objects;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class AuthUser {
	private String firstName;
	private String lastName;
	private String fullName;
	private String username;
	private List<String> providers;
	private String email;
	private Boolean emailVerified;
	private Boolean verifyEmail;

	private String userId;
	private String picture;

	private Date createdAt;
	private Date updatedAt;
	//private Map<String, Object> appMetadata;
	//private Map<String, Object> userMetadata= new HashMap<>();
	private String lastIp;
	private Date lastLogin;
	private Date lastPasswordReset;
	private int loginsCount;
	private boolean blocked;
	private String password;
	private String profileId;
	private boolean inactive;


}
