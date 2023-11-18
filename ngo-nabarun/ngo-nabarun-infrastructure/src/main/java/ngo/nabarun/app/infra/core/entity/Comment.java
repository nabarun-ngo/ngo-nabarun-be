package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing comments info in DB
 */

@Document("comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Comment {

	private String text;
	private Date commentedOn;
	private String commentedBy;
	private String commentedByName;
}
