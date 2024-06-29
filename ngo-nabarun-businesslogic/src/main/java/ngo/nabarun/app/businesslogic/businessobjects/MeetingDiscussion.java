package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDiscussion {
	private String id;
	private String agenda;
	private String minutes;

}
