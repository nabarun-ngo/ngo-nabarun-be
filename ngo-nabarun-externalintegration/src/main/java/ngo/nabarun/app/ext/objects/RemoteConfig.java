package ngo.nabarun.app.ext.objects;

import java.io.Serializable;

import lombok.Data;

@Data
public class RemoteConfig implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private Object value;
		
	private String group;
	
	private String type;
	
	private String description;
}
