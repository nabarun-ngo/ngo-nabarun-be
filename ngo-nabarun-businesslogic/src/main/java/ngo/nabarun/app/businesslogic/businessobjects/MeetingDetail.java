package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.MeetingRefType;
import ngo.nabarun.app.common.enums.MeetingStatus;
import ngo.nabarun.app.common.enums.MeetingType;

@Data
public class MeetingDetail {
	private String id;
	private String extMeetingId;
	private String meetingSummary;
	private String meetingDescription;
	private String meetingLocation;
	private Date meetingDate;
	private String meetingStartTime;
	private String meetingEndTime;
	private String meetingRefId;
	private MeetingType meetingType;
	private MeetingStatus meetingStatus;
	private List<UserDetail> meetingAttendees;
	//private List<MeetingDiscussion> meetingDiscussions;
	private String meetingRemarks;
	private MeetingRefType meetingRefType;
	private String extAudioConferenceLink;
	private String extVideoConferenceLink;
	private String extHtmlLink;
	private String creatorEmail;

	private String extConferenceStatus;
	
	
	
	@Deprecated
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public class MeetingDiscussion {
		private String id;
		private String agenda;
		private String minutes;

	}
	
	@Data
	public class MeetingDetailCreate {
		
		private String meetingSummary;
		private String meetingDescription;
		private String meetingLocation;
		private Date meetingStartTime;
		private Date meetingEndTime;
		private String meetingRefId;
		private MeetingType meetingType;
		private List<UserDetail> meetingAttendees;
		//private List<MeetingDiscussion> meetingDiscussions;
		private String meetingRemarks;
		private MeetingRefType meetingRefType;

		private Boolean draft;
		//private AuthorizationDetail authorization;

	}
}
