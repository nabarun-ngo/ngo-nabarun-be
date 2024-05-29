package ngo.nabarun.app.ext.objects;

import java.io.Serializable;

import lombok.Data;

@Data
public class AuthConnection implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;
	private String strategy;
	private String passwordPolicy;
	private boolean databaseConnection;

}
