package ngo.nabarun.app.ext.objects;

import java.util.Map;

import lombok.Data;

@Data
public class SecretList {
	
	private Map<String,SecretValue> secrets;
	

}
