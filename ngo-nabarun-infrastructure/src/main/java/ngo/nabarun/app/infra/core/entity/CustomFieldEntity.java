package ngo.nabarun.app.infra.core.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing additional_fields in DB
 */

@Document("additional_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomFieldEntity{
	
	@Id
	private String id;
	private String fieldKey;
	private String fieldName;
	private String fieldType;
	private String fieldValue;
	private String fieldDescription;
	private String source;
	private String sourceType;
	private String encryptionSalt;
	private String encryptionIV;

	private boolean hidden;
	private boolean encrypted;

	
}
