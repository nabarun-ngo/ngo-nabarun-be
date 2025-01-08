package ngo.nabarun.app.infra.dto;

import java.util.Date;

import lombok.Data;

@Data
public class NTokenDTO {
	private String id;
	private String token;
	private String userId;
	private Date createdOn;

}
