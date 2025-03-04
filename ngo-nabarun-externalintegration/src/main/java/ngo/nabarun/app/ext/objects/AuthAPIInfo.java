package ngo.nabarun.app.ext.objects;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AuthAPIInfo  implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
    private String name;
    private String identifier;
    private List<AuthAPIScope> scopes;
    
    @Data
    public static class AuthAPIScope  implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	    private String description;
	    private String value;
    }
  
}
