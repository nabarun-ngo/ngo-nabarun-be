package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * MongoDB
 * DAO for storing db_config info in DB
 */
@Document("db_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DBConfig {

	
	private String property_key;
	private String property_value;
	private String property_value_type;
	private boolean encrypted;
	private String description;
	private String config_type;
	private boolean active;
	@CreatedDate
	private Date createdOn;
}
