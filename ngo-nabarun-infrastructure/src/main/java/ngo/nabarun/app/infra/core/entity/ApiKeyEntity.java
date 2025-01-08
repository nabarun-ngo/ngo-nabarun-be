package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document("apikeys")
@Data
public class ApiKeyEntity {
	@Id
	private String id;
	private String name;

	private String apiKey;
	private String scopes;
	private Date createdOn;
	private String status;
	private boolean expireable;
	@Indexed(name = "expireOn", expireAfterSeconds = 0)
	private Date expireOn;
}
