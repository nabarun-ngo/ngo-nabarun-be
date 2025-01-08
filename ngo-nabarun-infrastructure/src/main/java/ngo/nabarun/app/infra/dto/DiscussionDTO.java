package ngo.nabarun.app.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class DiscussionDTO {
	private String id;
	private String agenda;
	private String minutes;
}
