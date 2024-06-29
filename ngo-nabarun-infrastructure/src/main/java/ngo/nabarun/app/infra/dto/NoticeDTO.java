package ngo.nabarun.app.infra.dto;

import java.util.Date;

import lombok.Data;

@Data
public class NoticeDTO {
	
	private String id;
	private String title;
	private String description;
	private String createdBy;
	private String creatorRole;
	private Date noticeDate;
	private Date publishDate;
	private String type;
	private boolean draft;
	private MeetingDTO meeting;
	
}
