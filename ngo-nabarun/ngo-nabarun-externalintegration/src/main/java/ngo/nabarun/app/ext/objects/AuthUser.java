package ngo.nabarun.app.ext.objects;

import java.util.Date;
import java.util.Map;

import lombok.Data;

@Data
public class AuthUser {
	private String firstName;
	private String lastName;
	private String fullName;
	private String username;
	private String email;
	private boolean emailVerified;
	private boolean verifyEmail;

	private String userId;
	private String picture;

	private Date createdAt;
	private Date updatedAt;
	private Map<String, Object> appMetadata;
	private Map<String, Object> userMetadata;
	private String lastIp;
	private Date lastLogin;
	private Date lastPasswordReset;
	private int loginsCount;
	private boolean blocked;
	private String password;

}
