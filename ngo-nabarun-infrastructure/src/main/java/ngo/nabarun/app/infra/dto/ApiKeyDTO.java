package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.ApiKeyStatus;

@Data
public class ApiKeyDTO {
	private String id;
	private String apiKey;
	private List<String> scopes;
	private ApiKeyStatus status;
	private boolean expireable;
	private Date expiryDate;
	private Date createdOn;

}
