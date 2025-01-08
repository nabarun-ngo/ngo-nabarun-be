package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ApiKeyDetail {
	private String id;
	private String name;
	private List<String> scopes;
	private String apiKey;
	private boolean expireable;
	private Date expiryDate;

}
