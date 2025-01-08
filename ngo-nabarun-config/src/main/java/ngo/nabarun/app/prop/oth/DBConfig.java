package ngo.nabarun.app.prop.oth;

import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * MongoDB
 * DAO for storing db_config info in DB
 */
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
	
	private Date createdOn;
}
