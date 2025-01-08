package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.NoticeStatus;

@Data
public class NoticeDTO {
	
	private String id;
	private String title;
	private String description;
	private UserDTO createdBy;
	private String creatorRole;
	private Date noticeDate;
	private Date publishDate;
	private String type;
	private NoticeStatus status;
	private boolean templateNotice;
	private MeetingDTO meeting;
	private Boolean needMeeting;

	
	@Data
	public static class NoticeDTOFilter{
		private String title;
		private String id;
		private boolean template;
		private Date fromDate;
		private Date toDate;
		private List<NoticeStatus> status;
	}
}
