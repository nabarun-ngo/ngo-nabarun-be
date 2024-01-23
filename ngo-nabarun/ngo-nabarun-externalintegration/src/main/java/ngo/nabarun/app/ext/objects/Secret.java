package ngo.nabarun.app.ext.objects;

import lombok.Data;

@Data
public class Secret {
	
	private String name;
	private SecretValue value;

}
