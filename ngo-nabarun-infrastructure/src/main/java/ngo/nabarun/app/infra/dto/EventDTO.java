package ngo.nabarun.app.infra.dto;

import java.util.Date;

import lombok.Data;
import ngo.nabarun.app.common.enums.EventType;

@Data
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
	private Double totalExpense;
	private UserDTO creator;
	
	@Data
	public static class EventDTOFilter{
		private String id;
		private String title;
		private Date fromDate;
		private Date toDate;
		private String location;
		private Boolean completed;
	}
}
