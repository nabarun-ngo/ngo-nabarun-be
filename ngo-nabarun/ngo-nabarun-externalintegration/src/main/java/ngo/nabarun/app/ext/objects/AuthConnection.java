package ngo.nabarun.app.ext.objects;

import lombok.Data;

@Data
public class AuthConnection {
	
	private String id;
	private String name;
	private String strategy;
	private String passwordPolicy;
	private boolean databaseConnection;

}
