package ngo.nabarun.app.infra.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.EventType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
	private String id;
	private String title;
	private String description;
	private Date eventDate;
	private String location;
	private String coverPic;
	//private String base64Image;
	//private boolean removePicture;
	private EventType type;
	private boolean draft;
	private Double budget;
	private String creatorId;
}
