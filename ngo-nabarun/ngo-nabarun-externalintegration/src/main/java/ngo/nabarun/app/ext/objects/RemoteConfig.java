package ngo.nabarun.app.ext.objects;

import lombok.Data;

@Data
public class RemoteConfig {

	private String name;
	
	private Object value;
		
	private String group;
	
	private String type;
	
	private String description;
}
