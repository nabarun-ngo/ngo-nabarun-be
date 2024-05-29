package ngo.nabarun.app.ext.objects;

import java.io.Serializable;

import lombok.Data;

@Data
public class AuthUserRole implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String roleId;
	private String roleName;
	//private String roleCode;
	private String roleDescription;

}
